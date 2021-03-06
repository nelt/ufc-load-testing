package org.codingmatters.ufc.load.testing.service;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Handlers;
import io.undertow.Undertow;
import org.codingmatters.poom.poomjobs.domain.jobs.repositories.JobRepository;
import org.codingmatters.poom.poomjobs.domain.runners.repositories.RunnerRepository;
import org.codingmatters.poom.poomjobs.domain.values.jobs.JobQuery;
import org.codingmatters.poom.poomjobs.domain.values.jobs.JobValue;
import org.codingmatters.poom.poomjobs.domain.values.runners.RunnerQuery;
import org.codingmatters.poom.poomjobs.domain.values.runners.RunnerValue;
import org.codingmatters.poom.runner.manager.DefaultRunnerClientFactory;
import org.codingmatters.poom.runner.manager.RunnerInvokerListener;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poomjobs.client.PoomjobsJobRegistryAPIHandlersClient;
import org.codingmatters.poomjobs.client.PoomjobsRunnerRegistryAPIHandlersClient;
import org.codingmatters.poomjobs.service.PoomjobsJobRegistryAPI;
import org.codingmatters.poomjobs.service.PoomjobsRunnerRegistryAPI;
import org.codingmatters.poomjobs.service.api.PoomjobsJobRegistryAPIProcessor;
import org.codingmatters.poomjobs.service.api.PoomjobsRunnerRegistryAPIProcessor;
import org.codingmatters.rest.api.client.okhttp.OkHttpClientWrapper;
import org.codingmatters.rest.undertow.CdmHttpUndertowHandler;
import org.codingmatters.ufc.load.testing.service.ui.UIHandler;
import org.codingmatters.ufc.utils.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class JobServicesApp {
    static private final Logger log = LoggerFactory.getLogger(JobServicesApp.class);

    public static void main(String[] args) {
        try {
            new JobServicesApp(args).run();
            System.exit(0);
        } catch(RuntimeException e) {
            log.error("error while running job services", e);
            System.exit(1);
        }
    }

    private final int port;
    private final String host;

    private final JsonFactory factory = new JsonFactory();
    private final ObjectMapper mapper =new ObjectMapper(factory);

    private final Repository<JobValue, JobQuery> jobRepository = JobRepository.createInMemory();
    private final PoomjobsJobRegistryAPI jobRegistryAPI;

    private final Repository<RunnerValue, RunnerQuery> runnerRepository = RunnerRepository.createInMemory();
    private final PoomjobsRunnerRegistryAPI runnerRegistryAPI;

    private final ExecutorService registryClientPool;
    private final ScheduledExecutorService jobCleanerPool;
    private final JobCleaner jobCleaner;

    private final Undertow server;

    public JobServicesApp(String[] args) throws RuntimeException {
        Arguments arguments = Arguments.parse(args);
        if(!arguments.hasOption("port")) {
            throw new RuntimeException("need to specify a --port option");
        }
        if(!arguments.hasOption("port")) {
            throw new RuntimeException("need to specify a --host option");
        }

        int clientPoolSize = 5;
        if(arguments.hasOption("client-pool-size")) {
            clientPoolSize = Integer.parseInt(arguments.option("client-pool-size"));
        }

        AtomicInteger threadIndex = new AtomicInteger(1);
        this.registryClientPool = Executors.newFixedThreadPool(clientPoolSize, runnable -> new Thread(runnable, "client-pool-thread-" + threadIndex.getAndIncrement()));
        log.info("starting with client pool size : {}", clientPoolSize);

        try {
            this.port = Integer.parseInt(arguments.option("port"));
        } catch(NumberFormatException e) {
            throw new RuntimeException("option --port need to be an integer");
        }
        this.host = arguments.option("host");

        this.runnerRegistryAPI = new PoomjobsRunnerRegistryAPI(this.runnerRepository);

        PoomjobsRunnerRegistryAPIHandlersClient runnerRegistryClient = new PoomjobsRunnerRegistryAPIHandlersClient(
                this.runnerRegistryAPI.handlers(),
                this.registryClientPool
        );

        MetricRegistry metrics = this.setupMetrics();

        this.jobRegistryAPI = new PoomjobsJobRegistryAPI(
                this.jobRepository,
                new CoumpoundListener()
                        .with(new RunnerInvokerListener(runnerRegistryClient, new DefaultRunnerClientFactory(this.factory, OkHttpClientWrapper.build())))
                        .with(new MetricListener(metrics))
        );

        PoomjobsJobRegistryAPIHandlersClient jobRegistryClient = new PoomjobsJobRegistryAPIHandlersClient(this.jobRegistryAPI.handlers(), this.registryClientPool);

        this.server = Undertow.builder()
                .addHttpListener(this.port, this.host)
                .setHandler(Handlers.path()
                        .addPrefixPath("/jobs", new CdmHttpUndertowHandler(new PoomjobsJobRegistryAPIProcessor(
                                "",
                                this.factory,
                                this.jobRegistryAPI.handlers()
                        )))
                        .addPrefixPath("/runners", new CdmHttpUndertowHandler(new PoomjobsRunnerRegistryAPIProcessor(
                                "",
                                this.factory,
                                this.runnerRegistryAPI.handlers()
                        )))
                        .addPrefixPath("/ui", new UIHandler(jobRegistryClient))
                        .addPrefixPath("/metrics", new JobsMetricsHandler(metrics, this.mapper))
                )
                .build();

        if(arguments.hasOption("cleanup.rate") && ! "0".equals(arguments.option("cleanup.rate"))) {
            this.jobCleanerPool = Executors.newScheduledThreadPool(1);
            long cleanupRate = Long.parseLong(arguments.option("cleanup.rate"));
            long kept = 5000;
            if(arguments.hasOption("cleanup.kept")) {
                kept = Long.parseLong(arguments.option("cleanup.kept"));
            }
            this.jobCleaner = new JobCleaner(this.jobRepository, kept);
            this.jobCleanerPool.scheduleWithFixedDelay(this::cleanRepository, cleanupRate, cleanupRate, TimeUnit.SECONDS);
        } else {
            this.jobCleanerPool = null;
            this.jobCleaner = null;
        }
    }

    private void cleanRepository() {
        try {
            this.jobCleaner.clean();
        } catch (RepositoryException e) {
            log.error("error cleaning repo", e);
        }
    }

    private MetricRegistry setupMetrics() {
        MetricRegistry metrics = new MetricRegistry();
        if(System.getProperty("expose.jmx.jobs.metrics", "false").equals("true")) {
            final JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
            reporter.start();
        }
        return metrics;
    }

    public void run() throws RuntimeException {
        this.startServer();
        this.mainLoop();
        this.tearDown();
    }

    private void startServer() {
        this.server.start();
        log.info("started job and runner registries at host={} ; port={}", this.host, this.port);
    }

    private void mainLoop() {
        while(true) {
            try {
                Thread.sleep(10 * 1000L);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void tearDown() {
        log.info("job and runner registries are tearing down.");

        this.server.stop();

        this.registryClientPool.shutdown();
        try {
            this.registryClientPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {}
        if(! this.registryClientPool.isTerminated()) {
            this.registryClientPool.shutdownNow();
            try {
                this.registryClientPool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException("cannot stop runner registry pool properly", e);
            }
        }
    }
}

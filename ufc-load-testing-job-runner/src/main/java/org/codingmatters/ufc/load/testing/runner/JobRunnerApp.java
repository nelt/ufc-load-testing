package org.codingmatters.ufc.load.testing.runner;

import com.fasterxml.jackson.core.JsonFactory;
import okhttp3.OkHttpClient;
import org.codingmatters.poom.client.PoomjobsJobRegistryAPIClient;
import org.codingmatters.poom.client.PoomjobsJobRegistryAPIRequesterClient;
import org.codingmatters.poom.client.PoomjobsRunnerRegistryAPIClient;
import org.codingmatters.poom.client.PoomjobsRunnerRegistryAPIRequesterClient;
import org.codingmatters.poom.runner.GenericRunner;
import org.codingmatters.poom.runner.configuration.RunnerConfiguration;
import org.codingmatters.poom.runner.exception.RunnerInitializationException;
import org.codingmatters.rest.api.client.RequesterFactory;
import org.codingmatters.rest.api.client.okhttp.HttpClientWrapper;
import org.codingmatters.rest.api.client.okhttp.OkHttpClientWrapper;
import org.codingmatters.rest.api.client.okhttp.OkHttpRequesterFactory;
import org.codingmatters.ufc.utils.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JobRunnerApp {
    static private final Logger log = LoggerFactory.getLogger(JobRunnerApp.class);

    public static void main(String[] args) {
        try {
            new JobRunnerApp(args).run();
            System.exit(0);
        } catch(RuntimeException e) {
            log.error("error while running runner", e);
            System.exit(1);
        }
    }

    private final LinkedList<GenericRunner> runners = new LinkedList<>();

    private final PoomjobsJobRegistryAPIClient jobRegistryApi;
    private final PoomjobsRunnerRegistryAPIClient runnerRegistryApi;
    private final ExecutorService jobWorker;
    private final JsonFactory jsonFactory = new JsonFactory();
    private final RequesterFactory requesterFactory;
    private final HttpClientWrapper client;

    private JobRunnerApp(String [] args) {
        Arguments arguments = Arguments.parse(args);
        log.info("runner started with arguments : {}", arguments);

        this.checkArguments(arguments);
        String registry = arguments.option("registry");

        this.client = OkHttpClientWrapper.build();
        this.requesterFactory = new OkHttpRequesterFactory(this.client, () -> registry);

        this.jobRegistryApi = new PoomjobsJobRegistryAPIRequesterClient(
                this.requesterFactory,
                this.jsonFactory,
                registry
        );
        this.runnerRegistryApi = new PoomjobsRunnerRegistryAPIRequesterClient(
                this.requesterFactory,
                this.jsonFactory,
                registry
        );

        int runnerCount = 1;
        if(arguments.hasOption("runner-count")) {
            runnerCount = Integer.parseInt(arguments.option("runner-count"));
        }
        this.jobWorker = Executors.newFixedThreadPool(runnerCount * 2);

        int portBase = Integer.parseInt(arguments.option("port-base"));
        int port = portBase;

        for(int i = 0 ; i < runnerCount ; i++) {

            while(true) {
                try (ServerSocket socket = new ServerSocket(port)) {
                    port = socket.getLocalPort();
                    break;
                } catch (IOException e) {
                    port++;
                }
            }

            log.info("creating runner configuration for port {}", port);
            this.runners.add(new GenericRunner(
                    RunnerConfiguration.builder()
                            .jobRegistryUrl(registry)
                            .endpointHost(arguments.option("host"))
                            .endpointPort(port)

                            .callbackBaseUrl(String.format("http://%s:%s", arguments.option("host"), port))
                            .ttl(2000L)
                            .processorFactory(new JobProcessorFactory(
                                    arguments.hasOption("min-process-time") ? Long.parseLong(arguments.option("min-process-time")) : 5 * 1000L,
                                    arguments.hasOption("max-process-time") ? Long.parseLong(arguments.option("max-process-time")) : 30 * 1000L
                            ))
                            .jobCategory("TEST")
                            .jobName("TEST")

                            .jobRegistryAPIClient(this.jobRegistryApi)
                            .runnerRegistryAPIClient(this.runnerRegistryApi)
                            .jobWorker(this.jobWorker)

                            .build()
            ));

            port++;
        }
    }

    private void checkArguments(Arguments arguments) throws RuntimeException {
        if(! arguments.hasOption("registry")) throw new RuntimeException("usage : need to provide a --registry option");
        if(! arguments.hasOption("host")) throw new RuntimeException("usage : need to provide a --host option");
        if(! arguments.hasOption("port") && arguments.option("port") != null && arguments.option("port").matches("\\d+"))
            throw new RuntimeException("usage : need to provide a --port option with an int value");
    }

    public void run() throws RuntimeException {
        this.start();
        this.mainLoop();
        this.tearDown();
    }

    private void start() {
        for (GenericRunner runner : this.runners) {
            try {
                runner.start();
            } catch (RunnerInitializationException e) {
                throw new RuntimeException("error starting runner", e);
            }
            log.info("generic runner starting, with id {}", runner.id());
        }
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
        for (GenericRunner runner : this.runners) {
            runner.stop();
        }
        this.jobWorker.shutdown();
        try {
            this.jobWorker.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("error shutting down job worker gracefully", e);
        }
        if(! this.jobWorker.isTerminated()) {
            this.jobWorker.shutdownNow();

            try {
                this.jobWorker.awaitTermination(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("error forcing shutdown of job worker ", e);
            }
        }
    }
}

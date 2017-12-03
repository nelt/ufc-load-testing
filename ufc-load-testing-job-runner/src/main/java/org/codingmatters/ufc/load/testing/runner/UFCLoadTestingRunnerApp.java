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
import org.codingmatters.rest.api.client.okhttp.OkHttpRequesterFactory;
import org.codingmatters.ufc.utils.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UFCLoadTestingRunnerApp {
    static private final Logger log = LoggerFactory.getLogger(UFCLoadTestingRunnerApp.class);

    public static void main(String[] args) {
        try {
            new UFCLoadTestingRunnerApp(args).run();
            System.exit(0);
        } catch(RuntimeException e) {
            log.error("error while running runner", e);
            System.exit(1);
        }
    }

    private final GenericRunner genericRunner;
    private final PoomjobsJobRegistryAPIClient jobRegistryApi;
    private final PoomjobsRunnerRegistryAPIClient runnerRegistryApi;
    private final ExecutorService jobWorker;
    private final JsonFactory jsonFactory = new JsonFactory();
    private final RequesterFactory requesterFactory;
    private final OkHttpClient client;

    private UFCLoadTestingRunnerApp(String [] args) {
        Arguments arguments = Arguments.parse(args);

        this.checkArguments(arguments);

        this.client = new OkHttpClient.Builder().build();
        this.requesterFactory = new OkHttpRequesterFactory(this.client);

        this.jobRegistryApi = new PoomjobsJobRegistryAPIRequesterClient(
                this.requesterFactory,
                this.jsonFactory,
                arguments.option("--registry")
        );
        this.runnerRegistryApi = new PoomjobsRunnerRegistryAPIRequesterClient(
                this.requesterFactory,
                this.jsonFactory,
                arguments.option("--registry")
        );
        this.jobWorker = Executors.newFixedThreadPool(1);

        this.genericRunner = new GenericRunner(
                RunnerConfiguration.builder()
                        .jobRegistryUrl(arguments.option("--registry"))
                        .endpointHost(arguments.option("--host"))
                        .endpointPort(Integer.parseInt(arguments.option("--port")))

                        .callbackBaseUrl(null)
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
        );
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
        try {
            this.genericRunner.start();
        } catch (RunnerInitializationException e) {
            throw new RuntimeException("error starting generic runner", e);
        }
        log.info("generic runner starting, with id {}", this.genericRunner.id());
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
        this.genericRunner.stop();
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

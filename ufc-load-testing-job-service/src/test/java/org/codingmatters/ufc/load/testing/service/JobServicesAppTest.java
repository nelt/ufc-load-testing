package org.codingmatters.ufc.load.testing.service;

import com.fasterxml.jackson.core.JsonFactory;
import okhttp3.OkHttpClient;
import org.codingmatters.poom.client.PoomjobsJobRegistryAPIClient;
import org.codingmatters.poom.client.PoomjobsJobRegistryAPIRequesterClient;
import org.codingmatters.poom.client.PoomjobsRunnerRegistryAPIClient;
import org.codingmatters.poom.client.PoomjobsRunnerRegistryAPIRequesterClient;
import org.codingmatters.rest.api.client.okhttp.OkHttpRequesterFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JobServicesAppTest {

    private int port;
    private ExecutorService executor;
    private OkHttpClient client;

    @Before
    public void setUp() throws Exception {
        try(ServerSocket socket = new ServerSocket(0)) {
            this.port = socket.getLocalPort();
        }
        this.executor = Executors.newFixedThreadPool(1);
        this.client = new OkHttpClient.Builder().build();
    }

    @After
    public void tearDown() throws Exception {
        this.executor.shutdown();
        this.executor.awaitTermination(3, TimeUnit.SECONDS);
        if(! this.executor.isTerminated()) {
            this.executor.shutdownNow();
            this.executor.awaitTermination(3, TimeUnit.SECONDS);
        }
    }

    @Test
    public void servicesRunning() throws Exception {
        this.executor.submit(() -> new JobServicesApp(new String [] {
                "--port", "" + this.port,
                "--host", "localhost"
        }).run());

        PoomjobsJobRegistryAPIClient jobRegistry = new PoomjobsJobRegistryAPIRequesterClient(
                new OkHttpRequesterFactory(this.client),
                new JsonFactory(),
                "http://localhost:" + this.port
        );

        PoomjobsRunnerRegistryAPIClient runnerRegistry = new PoomjobsRunnerRegistryAPIRequesterClient(
                new OkHttpRequesterFactory(this.client),
                new JsonFactory(),
                "http://localhost:" + this.port
        );

        Thread.sleep(1000L);

        assertThat(jobRegistry.jobCollection().get(req -> req.range("0-10")).status200(), is(notNullValue()));
        assertThat(runnerRegistry.runnerCollection().get(req -> req.range("0-10")).status200(), is(notNullValue()));
    }
}
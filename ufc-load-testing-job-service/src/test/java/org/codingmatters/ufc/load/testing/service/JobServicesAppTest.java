package org.codingmatters.ufc.load.testing.service;

import com.fasterxml.jackson.core.JsonFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.codingmatters.poomjobs.client.PoomjobsJobRegistryAPIClient;
import org.codingmatters.poomjobs.client.PoomjobsJobRegistryAPIRequesterClient;
import org.codingmatters.poomjobs.client.PoomjobsRunnerRegistryAPIClient;
import org.codingmatters.poomjobs.client.PoomjobsRunnerRegistryAPIRequesterClient;
import org.codingmatters.rest.api.client.okhttp.HttpClientWrapper;
import org.codingmatters.rest.api.client.okhttp.OkHttpClientWrapper;
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
    private HttpClientWrapper client;

    @Before
    public void setUp() throws Exception {
        try(ServerSocket socket = new ServerSocket(0)) {
            this.port = socket.getLocalPort();
        }
        this.executor = Executors.newFixedThreadPool(1);
        this.client = OkHttpClientWrapper.build();
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
                new OkHttpRequesterFactory(this.client, () -> "http://localhost:" + this.port),
                new JsonFactory(),
                "http://localhost:" + this.port
        );

        PoomjobsRunnerRegistryAPIClient runnerRegistry = new PoomjobsRunnerRegistryAPIRequesterClient(
                new OkHttpRequesterFactory(this.client, () -> "http://localhost:" + this.port),
                new JsonFactory(),
                "http://localhost:" + this.port
        );

        Thread.sleep(1000L);

        assertThat(jobRegistry.jobCollection().get(req -> req.range("0-10")).status200(), is(notNullValue()));
        assertThat(runnerRegistry.runnerCollection().get(req -> req.range("0-10")).status200(), is(notNullValue()));

        Response resp = client.execute(new Request.Builder().url("http://localhost:" + this.port + "/metrics").get().build());
        System.out.println(resp);
    }
}
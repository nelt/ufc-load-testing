package org.codingmatters.ufc.load.testing.service;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.codingmatters.ufc.load.testing.service.metrics.MetricsPackager;

import java.io.OutputStream;

public class JobsMetricsHandler implements HttpHandler {

    private final MetricRegistry metrics;
    private final ObjectMapper mapper;

    public JobsMetricsHandler(MetricRegistry metrics, ObjectMapper mapper) {
        this.metrics = metrics;
        this.mapper = mapper;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        exchange.startBlocking();


        try(OutputStream out = exchange.getOutputStream()) {
            new MetricsPackager(this.metrics, this.mapper).write(out);
        }
    }
}

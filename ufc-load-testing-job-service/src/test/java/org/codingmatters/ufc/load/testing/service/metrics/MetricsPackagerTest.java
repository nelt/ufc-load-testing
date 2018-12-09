package org.codingmatters.ufc.load.testing.service.metrics;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codingmatters.ufc.load.testing.service.MetricListener;
import org.junit.Test;

import static org.junit.Assert.*;

public class MetricsPackagerTest {

    private ObjectMapper mapper = new ObjectMapper();
    private MetricRegistry metric = new MetricRegistry();

    @Test
    public void counter() throws Exception {
        this.metric.counter(MetricListener.metricName("job.done")).inc();
        new MetricsPackager(this.metric, this.mapper).write(System.out);
    }
}
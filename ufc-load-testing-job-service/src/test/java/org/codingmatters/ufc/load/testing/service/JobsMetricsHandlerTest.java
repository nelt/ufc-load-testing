package org.codingmatters.ufc.load.testing.service;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;

import static org.junit.Assert.*;

public class JobsMetricsHandlerTest {

    @Test
    public void name() {
        MetricRegistry metrics = new MetricRegistry();

        metrics.counter("cnt").inc();

        metrics.histogram("histo").update(518);

        System.out.println(metrics.counter("cnt").getCount());

        System.out.println(metrics.histogram("histo").getSnapshot().getMin());
        System.out.println(metrics.histogram("histo").getSnapshot().getMax());
        System.out.println(metrics.histogram("histo").getSnapshot().getStdDev());
        System.out.println(metrics.histogram("histo").getSnapshot().getMean());
        System.out.println(metrics.histogram("histo").getSnapshot().getMedian());
        System.out.println(metrics.histogram("histo").getSnapshot().get75thPercentile());
        System.out.println(metrics.histogram("histo").getSnapshot().get95thPercentile());
        System.out.println(metrics.histogram("histo").getSnapshot().get98thPercentile());
        System.out.println(metrics.histogram("histo").getSnapshot().get99thPercentile());
        System.out.println(metrics.histogram("histo").getSnapshot().get999thPercentile());
    }
}
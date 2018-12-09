package org.codingmatters.ufc.load.testing.service.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codingmatters.ufc.load.testing.service.MetricListener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import static org.codingmatters.ufc.load.testing.service.MetricListener.metricName;

public class MetricsPackager {

    private final MetricRegistry metrics;
    private final ObjectMapper mapper;

    public MetricsPackager(MetricRegistry metrics, ObjectMapper mapper) {
        this.metrics = metrics;
        this.mapper = mapper;
    }

    public void write(OutputStream out) throws IOException {
        HashMap values = new HashMap();
        for (MetricListener.Counter counter : MetricListener.Counter.values()) {
            Counter cntr = this.metrics.getCounters().get(metricName(counter.key()));
            values.put(counter.key(), cntr != null ? cntr.getCount() : 0);
        }

        for (MetricListener.Histogram histogram : MetricListener.Histogram.values()) {
            if(this.metrics.histogram(metricName(histogram.key())) != null) {
                Snapshot histo = this.metrics.histogram(metricName(histogram.key())).getSnapshot();

                HashMap histoVals = new HashMap();
                values.put(histogram.key(), histoVals);

                histoVals.put("min", histo.getMin());
                histoVals.put("max", histo.getMax());
                histoVals.put("mean", histo.getMean());
                histoVals.put("median", histo.getMedian());
                histoVals.put("stddev", histo.getStdDev());
                histoVals.put("75p", histo.get75thPercentile());
                histoVals.put("95p", histo.get95thPercentile());
                histoVals.put("99p", histo.get99thPercentile());
                histoVals.put("999p", histo.get999thPercentile());
            }
        }

        this.mapper.writeValue(out, values);
    }
}

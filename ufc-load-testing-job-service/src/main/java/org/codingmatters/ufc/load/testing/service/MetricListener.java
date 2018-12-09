package org.codingmatters.ufc.load.testing.service;

import com.codahale.metrics.MetricRegistry;
import org.codingmatters.poom.poomjobs.domain.values.jobs.JobValue;
import org.codingmatters.poom.poomjobs.domain.values.jobs.jobvalue.Status;
import org.codingmatters.poom.poomjobs.domain.values.jobs.optional.OptionalJobValue;
import org.codingmatters.poom.servives.domain.entities.Entity;
import org.codingmatters.poomjobs.service.PoomjobsJobRepositoryListener;

import java.time.Duration;

import static com.codahale.metrics.MetricRegistry.name;

public class MetricListener implements PoomjobsJobRepositoryListener {

    public enum Counter {
        CREATED("job.created"),
        UPDATED("job.updated"),
        DONE("job.done"),
        PENDING("job.pending"),
        RUNNING("job.running"),
        SUCCESS("job.done.success"),
        FAILURE("job.done.failure"),
        ;

        private final String key;

        Counter(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }
    public enum Histogram {
        WAIT_TIME("job.waited"),
        EXEC_TIME("job.execution.time"),
        TOTAL_TIME("job.total.time")

        ;

        private final String key;

        Histogram(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    private final MetricRegistry metrics;

    public MetricListener(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    @Override
    public void jobCreated(Entity<JobValue> entity) {
        this.metrics.counter(metricName(Counter.CREATED.key())).inc();
        this.metrics.counter(metricName(Counter.PENDING.key())).inc();
    }

    @Override
    public void jobUpdated(Entity<JobValue> entity) {
        OptionalJobValue value = entity.value().opt();

        this.metrics.counter(metricName(Counter.UPDATED.key())).inc();

        if(value.status().run().get().equals(Status.Run.RUNNING)) {
            this.metrics.counter(metricName(Counter.PENDING.key())).dec();
            this.metrics.counter(metricName(Counter.RUNNING.key())).inc();
            long waitTime = Duration.between(value.processing().submitted().get(), value.processing().started().get()).toMillis();
            this.metrics.histogram(metricName(Histogram.WAIT_TIME.key())).update(waitTime);
        }

        if(value.status().run().get().equals(Status.Run.DONE)) {
            this.metrics.counter(metricName(Counter.DONE.key())).inc();
            this.metrics.counter(metricName(Counter.RUNNING.key())).dec();

            Status.Exit exit = value.status().exit().orElse(Status.Exit.SUCCESS);
            if(exit.equals(Status.Exit.SUCCESS)) {
                this.metrics.counter(metricName(Counter.SUCCESS.key())).inc();
            } else {
                this.metrics.counter(metricName(Counter.FAILURE.key())).inc();
            }

            long executionTime = Duration.between(value.processing().started().get(), value.processing().finished().get()).toMillis();
            this.metrics.histogram(metricName(Histogram.EXEC_TIME.key())).update(executionTime);

            long totalTime = Duration.between(value.processing().submitted().get(), value.processing().finished().get()).toMillis();
            this.metrics.histogram(metricName(Histogram.TOTAL_TIME.key())).update(totalTime);
        }
    }

    static public String metricName(String ... localName) {
        return name("ufc.load.testing.job.service", localName);
    }
}

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

    private final MetricRegistry metrics;

    public MetricListener(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    @Override
    public void jobCreated(Entity<JobValue> entity) {
        this.metrics.counter(metricName("job.creation")).inc();
        this.metrics.counter(metricName("job", entity.value().opt().arguments().get(0).orElse("unknown"), "creation")).inc();
    }

    @Override
    public void jobUpdated(Entity<JobValue> entity) {
        OptionalJobValue value = entity.value().opt();

        this.metrics.counter(metricName("job.update")).inc();

        if(value.status().run().get().equals(Status.Run.DONE)) {
            long waitTime = Duration.between(value.processing().submitted().get(), value.processing().started().get()).toMillis();
            this.metrics.histogram(metricName("job.wait.time")).update(waitTime);
        }


        if(value.status().run().get().equals(Status.Run.DONE)) {
            this.metrics.counter(metricName("job.done")).inc();

            String exit = value.status().exit().get().name().toLowerCase();
            this.metrics.counter(metricName("job.done", exit)).inc();

            long executionTime = Duration.between(value.processing().started().get(), value.processing().finished().get()).toMillis();
            this.metrics.histogram(metricName("job.execution.time")).update(executionTime);

            long totalTime = Duration.between(value.processing().started().get(), value.processing().finished().get()).toMillis();
            this.metrics.histogram(metricName("job.total.time")).update(totalTime);
        }
    }

    static private String metricName(String ... localName) {
        return name("ufc.load.testing.job.service", localName);
    }
}

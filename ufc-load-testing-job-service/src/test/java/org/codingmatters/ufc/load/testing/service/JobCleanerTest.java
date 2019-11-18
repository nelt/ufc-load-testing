package org.codingmatters.ufc.load.testing.service;

import org.codingmatters.poom.poomjobs.domain.jobs.repositories.JobRepository;
import org.codingmatters.poom.poomjobs.domain.values.jobs.JobQuery;
import org.codingmatters.poom.poomjobs.domain.values.jobs.JobValue;
import org.codingmatters.poom.poomjobs.domain.values.jobs.jobvalue.Accounting;
import org.codingmatters.poom.poomjobs.domain.values.jobs.jobvalue.Processing;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.services.domain.repositories.inmemory.InMemoryRepository;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class JobCleanerTest {

    private Repository<JobValue, JobQuery> jobRepository = JobRepository.createInMemory();

    @Test
    public void given__when__then() throws Exception {
        int jobCount = 100;
        for (int i = 0; i < jobCount ; i++) {
            this.jobRepository.create(JobValue.builder()
                    .category("" + i)
                    .build());
        }

        new JobCleaner(this.jobRepository, 50L).clean();

        assertThat(this.jobRepository.all(0, 0).total(), is(50L));
        assertThat(this.jobRepository.all(0, 0).valueList().get(0).category(), is("50"));
        assertThat(this.jobRepository.all(49, 49).valueList().get(0).category(), is("99"));
    }
}
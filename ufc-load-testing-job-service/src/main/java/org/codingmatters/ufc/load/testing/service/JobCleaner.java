package org.codingmatters.ufc.load.testing.service;

import org.codingmatters.poom.poomjobs.domain.values.jobs.JobCriteria;
import org.codingmatters.poom.poomjobs.domain.values.jobs.JobQuery;
import org.codingmatters.poom.poomjobs.domain.values.jobs.JobValue;
import org.codingmatters.poom.services.domain.exceptions.RepositoryException;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poom.servives.domain.entities.Entity;
import org.codingmatters.poom.servives.domain.entities.PagedEntityList;

public class JobCleaner {
    private final Repository<JobValue, JobQuery> jobRepository;
    private final long kept;

    public JobCleaner(Repository<JobValue, JobQuery> jobRepository, long kept) {
        this.jobRepository = jobRepository;
        this.kept = kept;
    }

    public void clean() throws RepositoryException {
        long total = this.jobRepository.all(0, 0).total();
        long toDelete = total - this.kept;

        if(toDelete > 0) {
            for (Entity<JobValue> jobValueEntity : this.jobRepository.all(0, toDelete - 1)) {
                this.jobRepository.delete(jobValueEntity);
            }
        }
    }
}

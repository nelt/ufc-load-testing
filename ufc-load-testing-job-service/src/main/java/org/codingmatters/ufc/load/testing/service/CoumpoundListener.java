package org.codingmatters.ufc.load.testing.service;

import org.codingmatters.poom.poomjobs.domain.values.jobs.JobValue;
import org.codingmatters.poom.servives.domain.entities.Entity;
import org.codingmatters.poomjobs.service.PoomjobsJobRepositoryListener;

import java.util.LinkedList;
import java.util.List;

public class CoumpoundListener implements PoomjobsJobRepositoryListener {

    private final List<PoomjobsJobRepositoryListener> listeners = new LinkedList<>();

    public CoumpoundListener with(PoomjobsJobRepositoryListener listener) {
        synchronized (this.listeners) {
            this.listeners.add(listener);
        }
        return this;
    }

    @Override
    public void jobCreated(Entity<JobValue> entity) {
        synchronized (this.listeners) {
            this.listeners.forEach(listener -> listener.jobCreated(entity));
        }
    }

    @Override
    public void jobUpdated(Entity<JobValue> entity) {
        synchronized (this.listeners) {
            this.listeners.forEach(listener -> listener.jobUpdated(entity));
        }
    }
}

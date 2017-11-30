package org.codingmatters.ufc.ltw;

import org.codingmatters.poom.runner.JobProcessor;
import org.codingmatters.poom.runner.exception.JobProcessingException;
import org.codingmatters.poomjobs.api.types.Job;

public class JobProrcessorFactory implements JobProcessor.Factory {
    @Override
    public JobProcessor createFor(Job job) {
        return new Processor(job);
    }

    class Processor implements JobProcessor {

        private final Job job;

        public Processor(Job job) {
            this.job = job;
        }

        @Override
        public Job process() throws JobProcessingException {
            return null;
        }
    }
}

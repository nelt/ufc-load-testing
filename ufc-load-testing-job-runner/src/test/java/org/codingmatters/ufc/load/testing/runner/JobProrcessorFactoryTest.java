package org.codingmatters.ufc.load.testing.runner;

import org.codingmatters.poom.runner.JobProcessor;
import org.codingmatters.poomjobs.api.types.Job;
import org.junit.Test;

public class JobProrcessorFactoryTest {

    @Test
    public void run() throws Exception {
        JobProcessorFactory factory = new JobProcessorFactory(10L, 30L);
        for(int i = 0 ; i < 10 ; i++) {
            JobProcessor processor = factory.createFor(Job.builder().build());
            Job job = processor.process();
            System.out.println(job.result());
        }
    }
}
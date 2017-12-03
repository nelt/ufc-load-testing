package org.codingmatters.ufc.load.testing.runner;

import org.codingmatters.poom.runner.JobProcessor;
import org.codingmatters.poom.runner.exception.JobProcessingException;
import org.codingmatters.poomjobs.api.types.Job;
import org.codingmatters.poomjobs.api.types.job.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class JobProcessorFactory implements JobProcessor.Factory {
    static private final Logger log = LoggerFactory.getLogger(JobProcessorFactory.class);
    static private final Random RANDOM = new Random(System.currentTimeMillis());

    private final long minTimeRunning;
    private final long maxTimeRunning;

    public JobProcessorFactory(long minTimeRunning, long maxTimeRunning) {
        this.minTimeRunning = minTimeRunning;
        this.maxTimeRunning = maxTimeRunning;
    }

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
            long runningTime = ThreadLocalRandom.current().nextLong(minTimeRunning, maxTimeRunning);
            long startTime = System.currentTimeMillis();

            while(System.currentTimeMillis() - startTime < runningTime) {
                this.consumeResource();
            }

            return Job.from(this.job)
                    .status(status -> status.run(Status.Run.DONE).exit(Status.Exit.SUCCESS))
                    .result("ran for " + (System.currentTimeMillis() - startTime) + "ms, max was " + runningTime + "ms.")
                    .build();
        }

        private void consumeResource() {
            try {
                KeyPairGenerator  keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(512);
                byte[] publicKey = keyGen.genKeyPair().getPublic().getEncoded();

                File keyFile = File.createTempFile("fake-load", ".key");
                keyFile.deleteOnExit();
                try {
                    try (FileOutputStream out = new FileOutputStream(keyFile)) {
                        out.write(publicKey);
                        out.flush();
                    }

                    for (int i = 0; i < 10; i++) {
                        try (FileInputStream in = new FileInputStream(keyFile)) {
                            byte[] buffer = new byte[50];
                            for (int read = in.read(buffer); read != -1; read = in.read(buffer)) {
                            }
                        }
                    }
                } finally {
                    keyFile.delete();
                }
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }

        }
    }
}

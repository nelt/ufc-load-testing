package org.codingmatters.ufc.load.testing.service.ui;

import io.undertow.Handlers;
import io.undertow.Undertow;
import org.codingmatters.poom.poomjobs.domain.jobs.repositories.JobRepository;
import org.codingmatters.poom.poomjobs.domain.values.jobs.JobQuery;
import org.codingmatters.poom.poomjobs.domain.values.jobs.JobValue;
import org.codingmatters.poom.poomjobs.domain.values.jobs.jobvalue.Status;
import org.codingmatters.poom.services.domain.repositories.Repository;
import org.codingmatters.poomjobs.client.PoomjobsJobRegistryAPIClient;
import org.codingmatters.poomjobs.client.PoomjobsJobRegistryAPIHandlersClient;
import org.codingmatters.poomjobs.service.PoomjobsJobRegistryAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;

public class UIHandlerTest {
    static private final Logger log = LoggerFactory.getLogger(UIHandler.class);

    public static void main(String[] args) throws Exception {
        int port = 8181;
        String host = "localhost";

        Repository<JobValue, JobQuery> jobRepository = JobRepository.createInMemory();
        for(int i = 0 ; i < 46 ; i++) {
            LocalDateTime aDate = LocalDateTime.now().minus(i, ChronoUnit.HOURS);
            Status.Run run = null;
            Status.Exit exit = null;

            switch (i % 4) {
                case 0:
                    run = Status.Run.DONE;
                    exit = Status.Exit.SUCCESS;
                    break;
                case 1:
                    run = Status.Run.DONE;
                    exit = Status.Exit.FAILURE;
                    break;
                case 2:
                    run = Status.Run.RUNNING;
                    exit = null;
                    break;
                case 3:
                    run = Status.Run.PENDING;
                    exit = null;
                    break;

            }
            Status status = Status.builder()
                    .run(run)
                    .exit(exit)
                    .build();
            jobRepository.create(JobValue.builder()
                    .name("NAME")
                    .category("CATEG")
                    .status(status)
                    .processing(processing -> processing
                                        .submitted(aDate.minus(3, ChronoUnit.MINUTES))
                                        .started(aDate.minus(2, ChronoUnit.MINUTES))
                                        .finished(aDate.minus(1, ChronoUnit.MINUTES))
                    )
                    .arguments("A1", "A2")
                    .result("yop !!")
                    .build());
        }

        PoomjobsJobRegistryAPI jobRegistry = new PoomjobsJobRegistryAPI(jobRepository);
        PoomjobsJobRegistryAPIClient jobRegistryClient = new PoomjobsJobRegistryAPIHandlersClient(
                jobRegistry.handlers(),
                Executors.newFixedThreadPool(5)
        );

        Undertow server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(Handlers.path()
                        .addPrefixPath("/ui", new UIHandler(jobRegistryClient))
                )
                .build();
        try {
            server.start();

            while (true) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                }
            }
        } finally {
            log.info("stopping ui");
            server.stop();
        }
    }

}
package org.codingmatters.ufc.load.testing.service.ui;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.codingmatters.poom.client.PoomjobsJobRegistryAPIClient;
import org.codingmatters.poomjobs.api.JobCollectionPostResponse;
import org.codingmatters.poomjobs.api.ValueList;
import org.codingmatters.poomjobs.api.optional.OptionalJobCollectionGetResponse;
import org.codingmatters.poomjobs.api.types.Job;
import org.codingmatters.ufc.load.testing.service.ui.utils.PagingProcessor;
import org.codingmatters.ufc.load.testing.service.ui.view.Page;
import org.codingmatters.ufc.load.testing.service.ui.view.page.Filter;
import org.codingmatters.ufc.load.testing.service.ui.view.page.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class UIHandler implements HttpHandler {
    static private final Logger log = LoggerFactory.getLogger(UIHandler.class);

    private final PoomjobsJobRegistryAPIClient jobRegistryAPI;
    private final MustacheFactory mf = new DefaultMustacheFactory();
    private final int pageSize = 10;

    public UIHandler(PoomjobsJobRegistryAPIClient jobRegistryAPI) {
        this.jobRegistryAPI = jobRegistryAPI;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        Info.Builder info = null;

        String create = exchange.getQueryParameters().getOrDefault("create", new LinkedList<>(Arrays.asList("None"))).getFirst();
        log.info("create ? {}", create);
        if("Job".equals(create)) {
            info = this.createJob(exchange);
        }

        long page;
        try {
            page = Long.parseLong(exchange.getQueryParameters().getOrDefault("page", new LinkedList<>(Arrays.asList("1"))).getFirst());
        } catch (NumberFormatException e) {
            log.error("error parsing page argument : {}", exchange.getQueryParameters().get("page"));
            page = 1;
        }
        long start = (page - 1) * this.pageSize;
        long end = page * this.pageSize;

        String filter = exchange.getQueryParameters().getOrDefault("filter", new LinkedList<>(Arrays.asList("pending"))).getFirst();
        if(! Arrays.asList("all", "pending", "running", "done").contains(filter.toLowerCase())) {
            log.error("unknown filter : {}", filter);
            filter = "pending";
        }

        String status = filter.equals("all") ? null : filter.toUpperCase();

        log.info("job page request : page={} ; start={} ; end={}", page, start, end);

        OptionalJobCollectionGetResponse collection = this.jobRegistryAPI.jobCollection().get(req -> req
                .range(String.format("%s-%s", start, end))
                .runStatus(status)
        ).opt();
        ValueList<Job> list = collection.status200().payload().orElse(collection.status206().payload().orElse(new ValueList.Builder<Job>().build()));

        String contentRange = collection.status200().contentRange().orElse(collection.status206().contentRange().orElse("0-0/0"));


        exchange.startBlocking();
        Mustache mustache = this.mf.compile("templates/main.mustache");
        mustache.execute(
                new PrintWriter(exchange.getOutputStream()),
                Page.builder()
                        .title("Jobs...")
                        .jobs(list.toArray(new Job[list.size()]))
                        .paging(new PagingProcessor(this.pageSize, contentRange).paging())
                        .filter(this.from(filter))
                        .info(info != null ? info.build() : null)
                        .build()
        ).flush();
    }

    private Info.Builder createJob(HttpServerExchange exchange) throws IOException {
        Collection<String> args = exchange.getQueryParameters().getOrDefault("argument", new LinkedList<>());
        log.info("job creation request for {} job", args.iterator().next());
        JobCollectionPostResponse response = this.jobRegistryAPI.jobCollection().post(req -> req
                .accountId("from-ui")
                .payload(payload -> payload.category("TEST").name("TEST").arguments(args))
        );
        log.info("job creation response : {}", response);
        return Info.builder().level(Info.Level.success).message(
                String.format("created %s job", args.iterator().next())
        );

    }

    private Filter from(String filter) {
        return Filter.builder()
                .value(filter)
                .all(filter.equalsIgnoreCase("all"))
                .running(filter.equalsIgnoreCase("running"))
                .pending(filter.equalsIgnoreCase("pending"))
                .done(filter.equalsIgnoreCase("done"))
                .build();
    }

}

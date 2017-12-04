package org.codingmatters.ufc.load.testing.service.ui.utils;

import org.codingmatters.ufc.load.testing.service.ui.view.page.Paging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PagingProcessor {
    static private final Pattern CONTENT_RANGE_PATTER = Pattern.compile(
            "Job (\\d+)-(\\d+)/(\\d+)"
    );

    private final long pageSize;
    private final String contentRange;

    public PagingProcessor(long pageSize, String contentRange) {
        this.pageSize = pageSize;
        this.contentRange = contentRange;
    }

    public Paging paging() {

        Matcher matcher = CONTENT_RANGE_PATTER.matcher(contentRange);
        if(! matcher.matches()) {
            return Paging.builder().pageSize(this.pageSize)
                    .currentPage(0L).pageCount(0L).jobCount(0L)
                    .build();
        } else {
            long start = Long.parseLong(matcher.group(1));
            long total = Long.parseLong(matcher.group(3));

            long pageCount = Double.valueOf(Math.ceil(((double) total) / this.pageSize)).longValue();
            long currentPage = 1 + start / this.pageSize;

            boolean hasNext = currentPage < pageCount;
            boolean hasPrev = currentPage > 1;

            Paging.Builder paging = Paging.builder()
                    .pageSize(this.pageSize)
                    .currentPage(currentPage)
                    .pageCount(pageCount)
                    .jobCount(total);
            if(hasNext) paging.nextPage(currentPage + 1);
            if(hasPrev) paging.prevPage(currentPage - 1);

            return paging.build();
        }
    }
}

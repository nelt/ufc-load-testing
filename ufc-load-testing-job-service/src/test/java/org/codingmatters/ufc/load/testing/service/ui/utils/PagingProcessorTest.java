package org.codingmatters.ufc.load.testing.service.ui.utils;

import org.codingmatters.ufc.load.testing.service.ui.view.page.Paging;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PagingProcessorTest {
    @Test
    public void unparseable() throws Exception {
        assertThat(
                new PagingProcessor(10, "yopyo tagada").paging(),
                is(Paging.builder()
                        .pageSize(10L)
                        .currentPage(0L)
                        .pageCount(0L)
                        .jobCount(0L)
                        .build()
                )
        );
    }

    @Test
    public void pageOneOfOne() throws Exception {
        assertThat(
                new PagingProcessor(10, "Job 0-9/10").paging(),
                is(Paging.builder()
                        .pageSize(10L)
                        .currentPage(1L)
                        .pageCount(1L)
                        .jobCount(10L)
                        .build()
                )
        );
    }

    @Test
    public void pageOneOfTwo() throws Exception {
        assertThat(
                new PagingProcessor(10, "Job 0-9/15").paging(),
                is(Paging.builder()
                        .pageSize(10L)
                        .currentPage(1L)
                        .pageCount(2L)
                        .jobCount(15L)
                        .nextPage(2L)
                        .build()
                )
        );
    }

    @Test
    public void pageTwoOfTwo() throws Exception {
        assertThat(
                new PagingProcessor(10, "Job 10-14/15").paging(),
                is(Paging.builder()
                        .pageSize(10L)
                        .currentPage(2L)
                        .pageCount(2L)
                        .jobCount(15L)
                        .prevPage(1L)
                        .build()
                )
        );
    }
}
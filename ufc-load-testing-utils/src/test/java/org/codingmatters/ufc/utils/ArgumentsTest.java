package org.codingmatters.ufc.utils;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ArgumentsTest {

    @Test
    public void none() throws Exception {
        assertThat(Arguments.parse().arguments(), is(empty()));
        assertThat(Arguments.parse().hasOptions(), is(false));

        assertThat(Arguments.parse(new String []{}).arguments(), is(empty()));
        assertThat(Arguments.parse(new String []{}).hasOptions(), is(false));
    }

    @Test
    public void oneArg() throws Exception {
        assertThat(Arguments.parse("one").arguments(), contains("one"));
        assertThat(Arguments.parse("one").hasOptions(), is(false));
    }

    @Test
    public void manyArgs() throws Exception {
        assertThat(Arguments.parse("one", "two", "three").arguments(), contains("one", "two", "three"));
        assertThat(Arguments.parse("one", "two", "three").hasOptions(), is(false));
    }

    @Test
    public void oneOptionWithValue() throws Exception {
        assertThat(Arguments.parse("-o", "v").hasOptions(), is(true));
        assertThat(Arguments.parse("-o", "v").hasOption("o"), is(true));
        assertThat(Arguments.parse("-o", "v").option("o"), is("v"));

        assertThat(Arguments.parse("--o", "v").hasOption("o"), is(true));
        assertThat(Arguments.parse("--o", "v").option("o"), is("v"));
    }

    @Test
    public void manyOptionsWithValue() throws Exception {
        assertThat(Arguments.parse("-o1", "v1", "-o2", "v2").hasOptions(), is(true));
        assertThat(Arguments.parse("-o1", "v1", "-o2", "v2").hasOption("o1"), is(true));
        assertThat(Arguments.parse("-o1", "v1", "-o2", "v2").option("o1"), is("v1"));
        assertThat(Arguments.parse("-o1", "v1", "-o2", "v2").hasOption("o2"), is(true));
        assertThat(Arguments.parse("-o1", "v1", "-o2", "v2").option("o2"), is("v2"));
    }

    @Test
    public void oneOptionWithNoValue() throws Exception {
        assertThat(Arguments.parse("-o1").hasOptions(), is(true));
        assertThat(Arguments.parse("-o1").hasOption("o1"), is(true));
        assertThat(Arguments.parse("-o1").option("o1"), is(nullValue()));
    }

    @Test
    public void oneOptionWithNoValueFollowedByOneWitAValue() throws Exception {
        assertThat(Arguments.parse("-o1", "-o2", "v2").hasOptions(), is(true));
        assertThat(Arguments.parse("-o1", "-o2", "v2").hasOption("o1"), is(true));
        assertThat(Arguments.parse("-o1", "-o2", "v2").option("o1"), is(nullValue()));
        assertThat(Arguments.parse("-o1", "-o2", "v2").hasOption("o2"), is(true));
        assertThat(Arguments.parse("-o1", "-o2", "v2").option("o2"), is("v2"));
    }

    @Test
    public void mixed() throws Exception {
        assertThat(Arguments.parse("one", "-o1", "-o2", "v2", "two", "three", "-o3", "v3").hasOptions(), is(true));
        assertThat(Arguments.parse("one", "-o1", "-o2", "v2", "two", "three", "-o3", "v3").hasOption("o1"), is(true));
        assertThat(Arguments.parse("one", "-o1", "-o2", "v2", "two", "three", "-o3", "v3").option("o1"), is(nullValue()));
        assertThat(Arguments.parse("one", "-o1", "-o2", "v2", "two", "three", "-o3", "v3").hasOption("o2"), is(true));
        assertThat(Arguments.parse("one", "-o1", "-o2", "v2", "two", "three", "-o3", "v3").option("o2"), is("v2"));
        assertThat(Arguments.parse("one", "-o1", "-o2", "v2", "two", "three", "-o3", "v3").hasOption("o3"), is(true));
        assertThat(Arguments.parse("one", "-o1", "-o2", "v2", "two", "three", "-o3", "v3").option("o3"), is("v3"));
        assertThat(Arguments.parse("one", "-o1", "-o2", "v2", "two", "three", "-o3", "v3").arguments(), contains("one", "two", "three"));
    }
}
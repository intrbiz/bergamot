package com.intrbiz.bergamot.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TestTimeInterval
{
    @Test
    public void testPlainParse()
    {
        TimeInterval interval = TimeInterval.fromString("5");
        assertThat(interval, is(notNullValue()));
        assertThat(interval.getValue(), is(equalTo(5L)));
        assertThat(interval.getUnit(), is(equalTo(TimeUnit.MINUTES)));
        assertThat(interval.toString(), is(equalTo("5")));
    }

    @Test
    public void testSecondsParse()
    {
        TimeInterval interval = TimeInterval.fromString("10s");
        assertThat(interval, is(notNullValue()));
        assertThat(interval.getValue(), is(equalTo(10L)));
        assertThat(interval.getUnit(), is(equalTo(TimeUnit.SECONDS)));
        assertThat(interval.toString(), is(equalTo("10s")));
    }
    
    @Test
    public void testMinutesParse()
    {
        TimeInterval interval = TimeInterval.fromString("65m");
        assertThat(interval, is(notNullValue()));
        assertThat(interval.getValue(), is(equalTo(65L)));
        assertThat(interval.getUnit(), is(equalTo(TimeUnit.MINUTES)));
        assertThat(interval.toString(), is(equalTo("65")));
    }
    
    @Test
    public void testHoursParse()
    {
        TimeInterval interval = TimeInterval.fromString("2h");
        assertThat(interval, is(notNullValue()));
        assertThat(interval.getValue(), is(equalTo(2L)));
        assertThat(interval.getUnit(), is(equalTo(TimeUnit.HOURS)));
        assertThat(interval.toString(), is(equalTo("2h")));
    }
    
    @Test
    public void testMillisParse()
    {
        TimeInterval interval = TimeInterval.fromString("150ms");
        assertThat(interval, is(notNullValue()));
        assertThat(interval.getValue(), is(equalTo(150L)));
        assertThat(interval.getUnit(), is(equalTo(TimeUnit.MILLISECONDS)));
        assertThat(interval.toString(), is(equalTo("150ms")));
    }
}

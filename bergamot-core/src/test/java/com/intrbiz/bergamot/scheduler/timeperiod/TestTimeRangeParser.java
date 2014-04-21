package com.intrbiz.bergamot.scheduler.timeperiod;

import org.junit.Test;

import com.intrbiz.bergamot.timerange.TimeRangeParser;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class TestTimeRangeParser
{
    @Test
    public void testParseHourRange()
    {
        assertThat(TimeRangeParser.parseTimeRange("00:00-24:00").toString(), is(equalTo("00:00-24:00")));
        assertThat(TimeRangeParser.parseTimeRange("00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("00:00-24:00, 01:30-02:30, 03:00-03:30")));
    }
    
    @Test
    public void testParseDayOfWeekRange()
    {
        assertThat(TimeRangeParser.parseTimeRange("monday 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("monday 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("tuesday 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("tuesday 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("wednesday 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("wednesday 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("thursday 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("thursday 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("friday 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("friday 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("saturday 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("saturday 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("sunday 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("sunday 00:00-24:00, 01:30-02:30, 03:00-03:30")));
    }
    
    @Test
    public void testParseDayOfMonthRange()
    {
        assertThat(TimeRangeParser.parseTimeRange("day 2 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("day 2 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("day -2 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("day -2 00:00-24:00, 01:30-02:30, 03:00-03:30")));
    }
    
    @Test
    public void testParseDayOfMonthRangeWithMonth()
    {
        assertThat(TimeRangeParser.parseTimeRange("january 1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("january 1 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("february -1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("february -1 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("march 1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("march 1 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("april 1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("april 1 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("may 1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("may 1 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("june 1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("june 1 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("july 1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("july 1 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("august 1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("august 1 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("september 1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("september 1 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("october 1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("october 1 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("november 1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("november 1 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("december 1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("december 1 00:00-24:00, 01:30-02:30, 03:00-03:30")));
    }
    
    @Test
    public void testParseDateRange()
    {
        assertThat(TimeRangeParser.parseTimeRange("2014-04-20 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("2014-04-20 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("2014-04-1 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("2014-04-01 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("2014-4-9 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("2014-04-09 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("2014-4-10 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("2014-04-10 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("2014-10-10 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("2014-10-10 00:00-24:00, 01:30-02:30, 03:00-03:30")));
    }
    
    @Test
    public void testParseDayOfWeekInMonthRange()
    {
        assertThat(TimeRangeParser.parseTimeRange("monday 1 may 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("monday 1 may 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("monday -1 may 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("monday -1 may 00:00-24:00, 01:30-02:30, 03:00-03:30")));
        assertThat(TimeRangeParser.parseTimeRange("friday 2 december 00:00-24:00, 01:30-02:30, 03:00-03:30").toString(), is(equalTo("friday 2 december 00:00-24:00, 01:30-02:30, 03:00-03:30")));
    }
}

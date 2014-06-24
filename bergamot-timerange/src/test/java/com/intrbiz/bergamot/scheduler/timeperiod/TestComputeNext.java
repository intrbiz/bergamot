package com.intrbiz.bergamot.scheduler.timeperiod;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;

import com.intrbiz.bergamot.model.timeperiod.HourRange;
import com.intrbiz.bergamot.timerange.TimeRangeParser;

public class TestComputeNext
{
    private Clock now;
    
    @Before
    public void setup()
    {
        //                                          2014-06-24T21:03:32
        this.now = Clock.fixed(Instant.ofEpochMilli(1403643812944L), ZoneId.of("UTC"));
    }
    
    @Test
    public void testHourRange()
    {
        assertThat((new HourRange(19, 20)).computeNextStartTime(now),                       is(equalTo(LocalDateTime.of(2014, 6, 25, 19, 0))));
        assertThat((new HourRange(10, 11)).computeNextStartTime(now),                       is(equalTo(LocalDateTime.of(2014, 6, 25, 10, 0))));
        assertThat((new HourRange(23, 24)).computeNextStartTime(now),                       is(equalTo(LocalDateTime.of(2014, 6, 24, 23, 0))));
        assertThat(TimeRangeParser.parseTimeRange("00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 6, 25,  0, 0))));
        
    }
    
    @Test
    public void testParseDayOfWeekRange()
    {
        assertThat(TimeRangeParser.parseTimeRange("monday    09:00-10:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 6, 30,  9, 0))));
        assertThat(TimeRangeParser.parseTimeRange("tuesday   10:00-11:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 7,  1, 10, 0))));
        assertThat(TimeRangeParser.parseTimeRange("tuesday   23:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 6, 24, 23, 0))));
        assertThat(TimeRangeParser.parseTimeRange("wednesday 11:00-12:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 6, 25, 11, 0))));
        assertThat(TimeRangeParser.parseTimeRange("thursday  12:00-13:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 6, 26, 12, 0))));
        assertThat(TimeRangeParser.parseTimeRange("friday    14:00-15:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 6, 27, 14, 0))));
        assertThat(TimeRangeParser.parseTimeRange("saturday  18:00-20:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 6, 28, 18, 0))));
        assertThat(TimeRangeParser.parseTimeRange("sunday    21:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 6, 29, 21, 0))));
    }
    
    @Test
    public void testParseDayOfMonthRange()
    {
        assertThat(TimeRangeParser.parseTimeRange("day  2 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 7,  2,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("day -2 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 6, 29,  0, 0))));
    }
    
    @Test
    public void testParseDayOfMonthRangeWithMonth()
    {
        assertThat(TimeRangeParser.parseTimeRange("january   1 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2015,  1,  1,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("february -1 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2015,  2, 28,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("march     1 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2015,  3,  1,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("april     1 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2015,  4,  1,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("may       1 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2015,  5,  1,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("june      1 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2015,  6,  1,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("july      1 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014,  7,  1,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("august    1 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014,  8,  1,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("september 1 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014,  9,  1,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("october   1 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 10,  1,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("november  1 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 11,  1,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("december  1 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 12,  1,  0, 0))));
    }
    
    @Test
    public void testParseDateRange()
    {
        assertThat(TimeRangeParser.parseTimeRange("2014-04-20 00:00-24:00").computeNextStartTime(now), is(nullValue()));
        assertThat(TimeRangeParser.parseTimeRange("2014-04-1  00:00-24:00").computeNextStartTime(now), is(nullValue()));
        assertThat(TimeRangeParser.parseTimeRange("2014-7-9   00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014,  7,  9,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("2014-7-10  00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014,  7, 10,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("2014-10-10 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 10, 10,  0, 0))));
    }
    
    @Test
    public void testParseDayOfWeekInMonthRange()
    {
        assertThat(TimeRangeParser.parseTimeRange("monday  1 may      00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2015,  5,  4,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("monday -1 may      00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2015,  5, 25,  0, 0))));
        assertThat(TimeRangeParser.parseTimeRange("friday  2 december 00:00-24:00").computeNextStartTime(now), is(equalTo(LocalDateTime.of(2014, 12, 12,  0, 0))));
    }
}

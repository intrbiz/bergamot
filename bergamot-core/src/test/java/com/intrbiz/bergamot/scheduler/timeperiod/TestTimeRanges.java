package com.intrbiz.bergamot.scheduler.timeperiod;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.intrbiz.bergamot.model.timeperiod.DayOfWeekQualifier;
import com.intrbiz.bergamot.model.timeperiod.HourRange;
import com.intrbiz.bergamot.model.timeperiod.util.DayOfWeek;

public class TestTimeRanges
{
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd/MM/yyyy HH:mm:ss");

    private static final Calendar setupCalendar(String date)
    {
        Date d;
        try
        {
            d = dateFormat.parse(date);

            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            return cal;
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
    
    // some dates to test with

    private Calendar monday_21_04_2014_at_00_00_00 = setupCalendar("Monday 21/04/2014 00:00:00");
    private Calendar monday_21_04_2014_at_01_00_00 = setupCalendar("Monday 21/04/2014 01:00:00");
    private Calendar monday_21_04_2014_at_02_00_00 = setupCalendar("Monday 21/04/2014 02:00:00");
    private Calendar monday_21_04_2014_at_03_00_00 = setupCalendar("Monday 21/04/2014 03:00:00");
    private Calendar monday_21_04_2014_at_04_00_00 = setupCalendar("Monday 21/04/2014 04:00:00");
    private Calendar monday_21_04_2014_at_05_00_00 = setupCalendar("Monday 21/04/2014 05:00:00");
    private Calendar monday_21_04_2014_at_06_00_00 = setupCalendar("Monday 21/04/2014 06:00:00");
    private Calendar monday_21_04_2014_at_07_00_00 = setupCalendar("Monday 21/04/2014 07:00:00");
    private Calendar monday_21_04_2014_at_08_00_00 = setupCalendar("Monday 21/04/2014 08:00:00");
    private Calendar monday_21_04_2014_at_09_00_00 = setupCalendar("Monday 21/04/2014 09:00:00");
    private Calendar monday_21_04_2014_at_10_00_00 = setupCalendar("Monday 21/04/2014 10:00:00");
    private Calendar monday_21_04_2014_at_11_00_00 = setupCalendar("Monday 21/04/2014 11:00:00");
    private Calendar monday_21_04_2014_at_12_00_00 = setupCalendar("Monday 21/04/2014 12:00:00");
    private Calendar monday_21_04_2014_at_13_00_00 = setupCalendar("Monday 21/04/2014 13:00:00");
    private Calendar monday_21_04_2014_at_14_00_00 = setupCalendar("Monday 21/04/2014 14:00:00");
    private Calendar monday_21_04_2014_at_15_00_00 = setupCalendar("Monday 21/04/2014 15:00:00");
    private Calendar monday_21_04_2014_at_16_00_00 = setupCalendar("Monday 21/04/2014 16:00:00");
    private Calendar monday_21_04_2014_at_17_00_00 = setupCalendar("Monday 21/04/2014 17:00:00");
    private Calendar monday_21_04_2014_at_18_00_00 = setupCalendar("Monday 21/04/2014 18:00:00");
    private Calendar monday_21_04_2014_at_19_00_00 = setupCalendar("Monday 21/04/2014 19:00:00");
    private Calendar monday_21_04_2014_at_20_00_00 = setupCalendar("Monday 21/04/2014 20:00:00");
    private Calendar monday_21_04_2014_at_21_00_00 = setupCalendar("Monday 21/04/2014 21:00:00");
    private Calendar monday_21_04_2014_at_22_00_00 = setupCalendar("Monday 21/04/2014 22:00:00");
    private Calendar monday_21_04_2014_at_23_00_00 = setupCalendar("Monday 21/04/2014 23:00:00");
    
    @Test
    public void test24HourRange()
    {
        HourRange range = new HourRange(0,0,24,00);
        assertTrue("Monday 21/04/2014 at 00:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_00_00_00));
        assertTrue("Monday 21/04/2014 at 01:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_01_00_00));
        assertTrue("Monday 21/04/2014 at 02:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_02_00_00));
        assertTrue("Monday 21/04/2014 at 03:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_03_00_00));
        assertTrue("Monday 21/04/2014 at 04:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_04_00_00));
        assertTrue("Monday 21/04/2014 at 05:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_05_00_00));
        assertTrue("Monday 21/04/2014 at 06:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_06_00_00));
        assertTrue("Monday 21/04/2014 at 07:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_07_00_00));
        assertTrue("Monday 21/04/2014 at 08:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_08_00_00));
        assertTrue("Monday 21/04/2014 at 09:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_09_00_00));
        assertTrue("Monday 21/04/2014 at 10:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_10_00_00));
        assertTrue("Monday 21/04/2014 at 11:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_11_00_00));
        assertTrue("Monday 21/04/2014 at 12:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_12_00_00));
        assertTrue("Monday 21/04/2014 at 13:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_13_00_00));
        assertTrue("Monday 21/04/2014 at 14:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_14_00_00));
        assertTrue("Monday 21/04/2014 at 15:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_15_00_00));
        assertTrue("Monday 21/04/2014 at 16:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_16_00_00));
        assertTrue("Monday 21/04/2014 at 17:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_17_00_00));
        assertTrue("Monday 21/04/2014 at 18:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_18_00_00));
        assertTrue("Monday 21/04/2014 at 19:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_19_00_00));
        assertTrue("Monday 21/04/2014 at 20:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_20_00_00));
        assertTrue("Monday 21/04/2014 at 21:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_21_00_00));
        assertTrue("Monday 21/04/2014 at 22:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_22_00_00));
        assertTrue("Monday 21/04/2014 at 23:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_23_00_00));
    }
    
    @Test
    public void test00_04HourRange()
    {
        HourRange range = new HourRange(0,0,4,0);
        assertTrue("Monday 21/04/2014 at 00:00:00 is in 00:00-04:00",      range.isInTimeRange(this.monday_21_04_2014_at_00_00_00));
        assertTrue("Monday 21/04/2014 at 01:00:00 is in 00:00-04:00",      range.isInTimeRange(this.monday_21_04_2014_at_01_00_00));
        assertTrue("Monday 21/04/2014 at 02:00:00 is in 00:00-04:00",      range.isInTimeRange(this.monday_21_04_2014_at_02_00_00));
        assertTrue("Monday 21/04/2014 at 03:00:00 is in 00:00-04:00",      range.isInTimeRange(this.monday_21_04_2014_at_03_00_00));
        assertFalse("Monday 21/04/2014 at 04:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_04_00_00));
        assertFalse("Monday 21/04/2014 at 05:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_05_00_00));
        assertFalse("Monday 21/04/2014 at 06:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_06_00_00));
        assertFalse("Monday 21/04/2014 at 07:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_07_00_00));
        assertFalse("Monday 21/04/2014 at 08:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_08_00_00));
        assertFalse("Monday 21/04/2014 at 09:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_09_00_00));
        assertFalse("Monday 21/04/2014 at 10:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_10_00_00));
        assertFalse("Monday 21/04/2014 at 11:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_11_00_00));
        assertFalse("Monday 21/04/2014 at 12:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_12_00_00));
        assertFalse("Monday 21/04/2014 at 13:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_13_00_00));
        assertFalse("Monday 21/04/2014 at 14:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_14_00_00));
        assertFalse("Monday 21/04/2014 at 15:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_15_00_00));
        assertFalse("Monday 21/04/2014 at 16:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_16_00_00));
        assertFalse("Monday 21/04/2014 at 17:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_17_00_00));
        assertFalse("Monday 21/04/2014 at 18:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_18_00_00));
        assertFalse("Monday 21/04/2014 at 19:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_19_00_00));
        assertFalse("Monday 21/04/2014 at 20:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_20_00_00));
        assertFalse("Monday 21/04/2014 at 21:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_21_00_00));
        assertFalse("Monday 21/04/2014 at 22:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_22_00_00));
        assertFalse("Monday 21/04/2014 at 23:00:00 is not in 00:00-04:00", range.isInTimeRange(this.monday_21_04_2014_at_23_00_00));
    }

    @Test
    public void testMonday24x7()
    {
        DayOfWeekQualifier range = new DayOfWeekQualifier(DayOfWeek.MONDAY, new HourRange(0, 0, 24, 00));
        assertTrue("Monday 21/04/2014 at 00:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_00_00_00));
        assertTrue("Monday 21/04/2014 at 01:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_01_00_00));
        assertTrue("Monday 21/04/2014 at 02:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_02_00_00));
        assertTrue("Monday 21/04/2014 at 03:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_03_00_00));
        assertTrue("Monday 21/04/2014 at 04:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_04_00_00));
        assertTrue("Monday 21/04/2014 at 05:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_05_00_00));
        assertTrue("Monday 21/04/2014 at 06:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_06_00_00));
        assertTrue("Monday 21/04/2014 at 07:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_07_00_00));
        assertTrue("Monday 21/04/2014 at 08:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_08_00_00));
        assertTrue("Monday 21/04/2014 at 09:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_09_00_00));
        assertTrue("Monday 21/04/2014 at 10:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_10_00_00));
        assertTrue("Monday 21/04/2014 at 11:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_11_00_00));
        assertTrue("Monday 21/04/2014 at 12:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_12_00_00));
        assertTrue("Monday 21/04/2014 at 13:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_13_00_00));
        assertTrue("Monday 21/04/2014 at 14:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_14_00_00));
        assertTrue("Monday 21/04/2014 at 15:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_15_00_00));
        assertTrue("Monday 21/04/2014 at 16:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_16_00_00));
        assertTrue("Monday 21/04/2014 at 17:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_17_00_00));
        assertTrue("Monday 21/04/2014 at 18:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_18_00_00));
        assertTrue("Monday 21/04/2014 at 19:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_19_00_00));
        assertTrue("Monday 21/04/2014 at 20:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_20_00_00));
        assertTrue("Monday 21/04/2014 at 21:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_21_00_00));
        assertTrue("Monday 21/04/2014 at 22:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_22_00_00));
        assertTrue("Monday 21/04/2014 at 23:00:00 is in 00:00-23:59", range.isInTimeRange(this.monday_21_04_2014_at_23_00_00));
    }

}

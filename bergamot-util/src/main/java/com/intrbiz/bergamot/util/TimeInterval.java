package com.intrbiz.bergamot.util;

import java.util.concurrent.TimeUnit;

/**
 * An interval of time, can be specified as:
 * 
 *  5   - 5 minutes
 *  5m  - 5 minutes
 *  5s  - 5 seconds
 *  5h  - 5 hours
 *  5ms - 5 milliseconds
 * 
 * Note: can be a compound interval, eg: 1m30s is not valid
 * 
 */
public final class TimeInterval
{
    private final long value;
    
    private final TimeUnit unit;
    
    public TimeInterval(long value, TimeUnit unit)
    {
        if (! (unit == TimeUnit.HOURS || unit ==  TimeUnit.MINUTES || unit ==  TimeUnit.SECONDS || unit ==  TimeUnit.MILLISECONDS)) 
            throw new IllegalArgumentException("Invalid TimeUnit");
        this.value = value;
        this.unit = unit;
    }
    
    public long getValue()
    {
        return this.value;
    }
    
    public TimeUnit getUnit()
    {
        return this.unit;
    }
    
    public long toMillis()
    {
        return this.unit.toMillis(this.value);
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        result = prime * result + (int) (value ^ (value >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TimeInterval other = (TimeInterval) obj;
        if (unit != other.unit) return false;
        if (value != other.value) return false;
        return true;
    }

    public static TimeInterval milliseconds(long value)
    {
        return new TimeInterval(value, TimeUnit.MILLISECONDS);
    }
    
    public static TimeInterval seconds(long value)
    {
        return new TimeInterval(value, TimeUnit.SECONDS);
    }
    
    public static TimeInterval minutes(long value)
    {
        return new TimeInterval(value, TimeUnit.MINUTES);
    }
    
    public static TimeInterval hours(long value)
    {
        return new TimeInterval(value, TimeUnit.HOURS);
    }
    
    public static TimeInterval fromString(String time)
    {
        if (time.endsWith("ms") || time.endsWith("MS"))
        {
            return new TimeInterval(Long.parseLong(time.substring(0, time.length() -2)), TimeUnit.MILLISECONDS);   
        }
        else if (time.endsWith("m") || time.endsWith("M"))
        {
            return new TimeInterval(Long.parseLong(time.substring(0, time.length() -1)), TimeUnit.MINUTES);
        }
        else if (time.endsWith("h") || time.endsWith("H"))
        {
            return new TimeInterval(Long.parseLong(time.substring(0, time.length() -1)), TimeUnit.HOURS);
        }
        else if (time.endsWith("s") || time.endsWith("S"))
        {
            return new TimeInterval(Long.parseLong(time.substring(0, time.length() -1)), TimeUnit.SECONDS);
        }
        // default value is in minutes
        return new TimeInterval(Long.parseLong(time), TimeUnit.MINUTES);
    }
    
    public String toString()
    {
        if (this.unit == TimeUnit.MILLISECONDS)
        {
            return this.value + "ms";
        }
        else if (this.unit == TimeUnit.SECONDS)
        {
            return this.value + "s";
        }
        else if (this.unit == TimeUnit.HOURS)
        {
            return this.value + "h";
        }
        return Long.toString(this.value);
    }
}

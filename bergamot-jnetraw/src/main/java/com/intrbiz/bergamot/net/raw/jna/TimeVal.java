package com.intrbiz.bergamot.net.raw.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * TimeVal
 */
public class TimeVal extends Structure
{
    public long tv_sec;
    
    public long tv_usec;

    public TimeVal(long tv_sec, long tv_usec)
    {
        super();
        this.tv_sec = tv_sec;
        this.tv_usec = tv_usec;
    }
    
    public TimeVal()
    {
        this(0L, 0L);
    }

    @Override
    protected List<String> getFieldOrder()
    {
        return Arrays.asList("tv_sec", "tv_usec");
    }

    public static class ByReference extends TimeVal implements Structure.ByReference
    {
        public ByReference()
        {
            super();
        }

        public ByReference(long tv_sec, long tv_usec)
        {
            super(tv_sec, tv_usec);
        }
    }
}

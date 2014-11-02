package com.intrbiz.bergamot.pinger;

import java.util.Arrays;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;

/**
 * A sliding window of ping samples
 */
public class PingWindow
{
    private long[] samples;

    private int pos = 0;
    
    private int size = 0;
    
    public PingWindow(int capacity)
    {
        this.samples = new long[capacity];
    }
    
    public int getCapacity()
    {
        return this.samples.length;
    }
    
    public int getPosition()
    {
        return this.pos;
    }
    
    public int getSize()
    {
        return this.size;
    }

    public void add(long rtt)
    {
        this.samples[this.pos] = rtt;
        this.pos = (this.pos + 1) % this.samples.length;
        if (this.size < this.samples.length) this.size++;
    }

    public long[] getWindow()
    {
        int length = this.size;
        long[] ret = new long[length];
        int start  = length < this.samples.length ? 0 : this.pos;
        for (int i = 0; i < length; i++)
        {
            ret[i] = this.samples[(start + i) % this.samples.length];
        }
        return ret;
    }
    
    public void forEach(LongConsumer c)
    {
        int length = this.size;
        int start  = length < this.samples.length ? 0 : this.pos;
        for (int i = 0; i < length; i++)
        {
            c.accept(this.samples[ (start + i) % this.samples.length ]);
        }
    }
    
    public LongStream stream()
    {
        return Arrays.stream(this.getWindow());
    }
    
    public PingSnapshot getSnapshot()
    {
        return new PingSnapshot(this.getWindow());
    }
}

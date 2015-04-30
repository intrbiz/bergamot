package com.intrbiz.bergamot.agent.util;

import java.util.concurrent.TimeUnit;

public class SampleRingBuffer
{
    private final int capacity;
    
    private final double interval;
    
    private final long[] samples;
    
    private int size = 0;
    
    private int position = 0;
    
    public SampleRingBuffer(int capacity, long interval, TimeUnit intervalUnit)
    {
        this.capacity = capacity;
        this.interval = intervalUnit.toMillis(interval);
        this.samples = new long[capacity];
    }
    
    public void addSample(long sample)
    {
        if (this.size < this.capacity) this.size++;
        this.samples[this.position] = sample;
        this.position = (this.position + 1) % this.capacity;
    }
    
    private int translate(int index)
    {
        return (this.size < this.capacity) ? (index % this.capacity) : (this.position + index) % this.capacity;
    }
    
    public long getSample(int index)
    {
        return this.samples[this.translate(index)];
    }
    
    public long interval()
    {
        return (long) this.interval;
    }
    
    public int size()
    {
        return this.size;
    }
    
    public int capacity()
    {
        return this.capacity;
    }
    
    public double peakRate()
    {
        double peakRate = 0;
        if (this.size > 1)
        {
            long previous = this.samples[translate(0)];
            for (int i = 1; i < this.size; i++)
            {
                long current = this.samples[translate(i)];
                long delta = current - previous;
                if (delta > 0) peakRate = Math.max(peakRate, ((double) delta) / this.interval);
                previous = current;
            }
        }
        return peakRate;
    }
    
    public double peakRateSeconds()
    {
        return this.peakRate() * 1000D;
    }
    
    public double instantRate()
    {
        if (this.size > 1)
        {
            int latest = this.size - 1;
            long delta = this.samples[translate(latest)] - this.samples[translate(latest -1)];
            if (delta > 0) return ((double) delta) / this.interval;
        }
        return 0;
    }
    
    public double instantRateSeconds()
    {
        return this.instantRate() * 1000D;
    }
    
    public double averageRate()
    {
        if (this.size > 1)
        {
            long delta = this.samples[translate(this.size - 1)] - this.samples[translate(0)];
            if (delta > 0) return ((double) delta) / (this.interval * ((double) (this.size - 1)));
        }
        return 0;
    }
    
    public double averageRateSeconds()
    {
        return this.averageRate() * 1000D;
    }
}

package com.intrbiz.bergamot.pinger;

import java.text.DecimalFormat;

/**
 * A statistical snapshot of the round trip time of a target
 */
public class PingSnapshot
{
    private final long[] snapshot;
    
    public PingSnapshot(long[] snapshot)
    {
        super();
        this.snapshot = snapshot;
    }
    
    /**
     * Get the quickest RTT in this snapshot (in milliseconds)
     */
    public double getMinRTTMillis()
    {
        return ((double) this.getMinRTT()) / 1000D;
    }

    /**
     * Get the mean RTT of this snapshot (in milliseconds)
     */
    public double getMeanRTTMillis()
    {
        return ((double) this.getMeanRTT()) / 1000D;
    }
    
    /**
     * Get the longest RTT in this snapshot (in milliseconds)
     */
    public double getMaxRTTMillis()
    {
        return ((double) this.getMaxRTT()) / 1000D;
    }
    
    /**
     * Get the quickest RTT in this snapshot (in microseconds)
     */
    public long getMinRTT()
    {
        if (this.snapshot.length == 0) return 0;
        long min = -1; 
        for (long rtt : this.snapshot)
        {
            if (rtt != -1 && (min == -1 || min > rtt))
            {
                min = rtt;
            }
        }
        return min == -1 ? 0 : min;
    }
    
    /**
     * Get the mean RTT of this snapshot (in microseconds)
     */
    public long getMeanRTT()
    {
        long sum = 0;
        long count = 0;
        for (long rtt : this.snapshot)
        {
            if (rtt != -1)
            {
                sum += rtt;
                count++;
            }
        }
        return count == 0 ? 0 : (sum / count);
    }
    
    /**
     * Get the longest RTT in this snapshot (in microseconds)
     */
    public long getMaxRTT()
    {
        if (this.snapshot.length == 0) return 0;
        long max = -1;
        for (long rtt : this.snapshot)
        {
            if (rtt != -1 && (max == -1 || max < rtt))
            {
                max = rtt;
            }
        }
        return max == -1 ? 0 : max;
    }
    
    /**
     * Get the packet loss % of this snapshot
     */
    public double getLoss()
    {
        if (this.snapshot.length == 0) return 0;
        long lost = 0;
        for (long rtt : this.snapshot)
        {
            if (rtt == -1)
            {
                lost++;
            }
        }
        return (((double) lost) / ((double) this.snapshot.length)) * 100D;
    }
    
    /**
     * Do we have zero non timed out replies in this snapshot
     */
    public boolean isCompletelyDown()
    {
        long count = 0;
        for (long rtt : this.snapshot)
        {
            if (rtt != -1)
            {
                count++;
            }
        }
        return count == 0;
    }
    
    public String toString()
    {
        DecimalFormat df = new DecimalFormat("0.00");
        return "samples: " + this.snapshot.length + ", RTT (min, mean, max): " + df.format(this.getMinRTTMillis()) + "ms, " + df.format(this.getMeanRTTMillis()) + "ms, " + df.format(this.getMaxRTTMillis()) + "ms, loss: " + df.format(this.getLoss()) + "%";
    }
}

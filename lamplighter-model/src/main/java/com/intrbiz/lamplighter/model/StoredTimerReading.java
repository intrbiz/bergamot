package com.intrbiz.lamplighter.model;

import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.lamplighter.data.LamplighterDB;

@SQLTable(schema = LamplighterDB.class, name = "timer_reading", since = @SQLVersion({4, 0, 0}))
public class StoredTimerReading extends StoredReading
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "count", since = @SQLVersion({4, 0, 0}))
    private long count;
    
    @SQLColumn(index = 2, name = "mean_rate", since = @SQLVersion({4, 0, 0}))
    private double meanRate;
    
    @SQLColumn(index = 3, name = "one_minute_rate", since = @SQLVersion({4, 0, 0}))
    private double oneMinuteRate;
 
    @SQLColumn(index = 4, name = "five_minute_rate", since = @SQLVersion({4, 0, 0}))
    private double fiveMinuteRate;
   
    @SQLColumn(index = 5, name = "fifteen_minute_rate", since = @SQLVersion({4, 0, 0}))
    private double fifteenMinuteRate;
    
    @SQLColumn(index = 6, name = "median", since = @SQLVersion({4, 0, 0}))
    private double median;

    @SQLColumn(index = 7, name = "mean", since = @SQLVersion({4, 0, 0}))
    private double mean;

    @SQLColumn(index = 8, name = "min", since = @SQLVersion({4, 0, 0}))
    private double min;

    @SQLColumn(index = 9, name = "max", since = @SQLVersion({4, 0, 0}))
    private double max;

    @SQLColumn(index = 10, name = "std_dev", since = @SQLVersion({4, 0, 0}))
    private double stdDev;

    @SQLColumn(index = 11, name = "the_75th_percentile", since = @SQLVersion({4, 0, 0}))
    private double the75thPercentile;

    @SQLColumn(index = 12, name = "the_95th_percentile", since = @SQLVersion({4, 0, 0}))
    private double the95thPercentile;

    @SQLColumn(index = 13, name = "the_98th_percentile", since = @SQLVersion({4, 0, 0}))
    private double the98thPercentile;

    @SQLColumn(index = 14, name = "the_99th_percentile", since = @SQLVersion({4, 0, 0}))
    private double the99thPercentile;

    @SQLColumn(index = 15, name = "the_999th_percentile", since = @SQLVersion({4, 0, 0}))
    private double the999thPercentile;
    
    public StoredTimerReading()
    {
        super();
    }

    public StoredTimerReading(UUID siteId, UUID readingId, Timestamp collectedAt, long count, double meanRate, double oneMinuteRate, double fiveMinuteRate, double fifteenMinuteRate, double median, double mean, double min, double max, double stdDev, double the75thPercentile, double the95thPercentile, double the98thPercentile, double the99thPercentile, double the999thPercentile)
    {
        super();
        this.siteId = siteId;
        this.readingId = readingId;
        this.collectedAt = collectedAt;
        this.count = count;
        this.meanRate = meanRate;
        this.oneMinuteRate = oneMinuteRate;
        this.fiveMinuteRate = fiveMinuteRate;
        this.fifteenMinuteRate = fifteenMinuteRate;
        this.median = median;
        this.mean = mean;
        this.min = min;
        this.max = max;
        this.stdDev = stdDev;
        this.the75thPercentile = the75thPercentile;
        this.the95thPercentile = the95thPercentile;
        this.the98thPercentile = the98thPercentile;
        this.the99thPercentile = the99thPercentile;
        this.the999thPercentile = the999thPercentile;
    }



    public long getCount()
    {
        return count;
    }

    public void setCount(long count)
    {
        this.count = count;
    }

    public double getMeanRate()
    {
        return meanRate;
    }

    public void setMeanRate(double meanRate)
    {
        this.meanRate = meanRate;
    }

    public double getOneMinuteRate()
    {
        return oneMinuteRate;
    }

    public void setOneMinuteRate(double oneMinuteRate)
    {
        this.oneMinuteRate = oneMinuteRate;
    }

    public double getFiveMinuteRate()
    {
        return fiveMinuteRate;
    }

    public void setFiveMinuteRate(double fiveMinuteRate)
    {
        this.fiveMinuteRate = fiveMinuteRate;
    }

    public double getFifteenMinuteRate()
    {
        return fifteenMinuteRate;
    }

    public void setFifteenMinuteRate(double fifteenMinuteRate)
    {
        this.fifteenMinuteRate = fifteenMinuteRate;
    }

    public double getMin()
    {
        return min;
    }

    public void setMin(double min)
    {
        this.min = min;
    }

    public double getMax()
    {
        return max;
    }

    public void setMax(double max)
    {
        this.max = max;
    }

    public double getMedian()
    {
        return median;
    }

    public void setMedian(double median)
    {
        this.median = median;
    }

    public double getMean()
    {
        return mean;
    }

    public void setMean(double mean)
    {
        this.mean = mean;
    }

    public double getStdDev()
    {
        return stdDev;
    }

    public void setStdDev(double stdDev)
    {
        this.stdDev = stdDev;
    }

    public double getThe75thPercentile()
    {
        return the75thPercentile;
    }

    public void setThe75thPercentile(double the75thPercentile)
    {
        this.the75thPercentile = the75thPercentile;
    }

    public double getThe95thPercentile()
    {
        return the95thPercentile;
    }

    public void setThe95thPercentile(double the95thPercentile)
    {
        this.the95thPercentile = the95thPercentile;
    }

    public double getThe98thPercentile()
    {
        return the98thPercentile;
    }

    public void setThe98thPercentile(double the98thPercentile)
    {
        this.the98thPercentile = the98thPercentile;
    }

    public double getThe99thPercentile()
    {
        return the99thPercentile;
    }

    public void setThe99thPercentile(double the99thPercentile)
    {
        this.the99thPercentile = the99thPercentile;
    }

    public double getThe999thPercentile()
    {
        return the999thPercentile;
    }

    public void setThe999thPercentile(double the999thPercentile)
    {
        this.the999thPercentile = the999thPercentile;
    }
}

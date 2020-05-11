package com.intrbiz.lamplighter.model;

import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.lamplighter.data.LamplighterDB;

@SQLTable(schema = LamplighterDB.class, name = "meter_reading", since = @SQLVersion({4, 0, 0}))
public class StoredMeterReading extends StoredReading
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
    
    public StoredMeterReading()
    {
        super();
    }
    
    public StoredMeterReading(UUID siteId, UUID readingId, Timestamp collectedAt, long count, double meanRate, double oneMinuteRate, double fiveMinuteRate, double fifteenMinuteRate)
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
}

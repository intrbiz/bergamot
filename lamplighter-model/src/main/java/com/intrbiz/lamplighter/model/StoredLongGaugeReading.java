package com.intrbiz.lamplighter.model;

import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.lamplighter.data.LamplighterDB;

@SQLTable(schema = LamplighterDB.class, name = "long_gauge_reading", since = @SQLVersion({ 1, 0, 0 }))
public class StoredLongGaugeReading extends StoredReading
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "value", since = @SQLVersion({ 1, 0, 0 }))
    private long value;
    
    @SQLColumn(index = 2, name = "warning", since = @SQLVersion({ 1, 0, 0 }))
    private long warning;
    
    @SQLColumn(index = 3, name = "critical", since = @SQLVersion({ 1, 0, 0 }))
    private long critical;
    
    @SQLColumn(index = 4, name = "min", since = @SQLVersion({ 1, 0, 0 }))
    private long min;
    
    @SQLColumn(index = 5, name = "max", since = @SQLVersion({ 1, 0, 0 }))
    private long max;
    
    public StoredLongGaugeReading()
    {
        super();
    }
    
    public StoredLongGaugeReading(UUID siteId, UUID readingId, Timestamp collectedAt, long value, long warning, long critical, long min, long max)
    {
        super();
        this.siteId = siteId;
        this.readingId = readingId;
        this.collectedAt = collectedAt;
        this.value = value;
        this.warning = warning;
        this.critical = critical;
        this.min = min;
        this.max = max;
    }

    public long getValue()
    {
        return value;
    }

    public void setValue(long value)
    {
        this.value = value;
    }

    public long getWarning()
    {
        return warning;
    }

    public void setWarning(long warning)
    {
        this.warning = warning;
    }

    public long getCritical()
    {
        return critical;
    }

    public void setCritical(long critical)
    {
        this.critical = critical;
    }

    public long getMin()
    {
        return min;
    }

    public void setMin(long min)
    {
        this.min = min;
    }

    public long getMax()
    {
        return max;
    }

    public void setMax(long max)
    {
        this.max = max;
    }
}

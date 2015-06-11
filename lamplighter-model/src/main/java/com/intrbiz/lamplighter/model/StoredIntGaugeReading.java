package com.intrbiz.lamplighter.model;

import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.lamplighter.data.LamplighterDB;

@SQLTable(schema = LamplighterDB.class, name = "int_gauge_reading", since = @SQLVersion({ 1, 0, 0 }))
public class StoredIntGaugeReading extends StoredReading
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "value", since = @SQLVersion({ 1, 0, 0 }))
    private int value;
    
    @SQLColumn(index = 2, name = "warning", since = @SQLVersion({ 1, 0, 0 }))
    private int warning;
    
    @SQLColumn(index = 3, name = "critical", since = @SQLVersion({ 1, 0, 0 }))
    private int critical;
    
    @SQLColumn(index = 4, name = "min", since = @SQLVersion({ 1, 0, 0 }))
    private int min;
    
    @SQLColumn(index = 5, name = "max", since = @SQLVersion({ 1, 0, 0 }))
    private int max;
    
    public StoredIntGaugeReading()
    {
        super();
    }
    
    public StoredIntGaugeReading(UUID siteId, UUID readingId, Timestamp collectedAt, int value, int warning, int critical, int min, int max)
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

    public int getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    public int getWarning()
    {
        return warning;
    }

    public void setWarning(int warning)
    {
        this.warning = warning;
    }

    public int getCritical()
    {
        return critical;
    }

    public void setCritical(int critical)
    {
        this.critical = critical;
    }

    public int getMin()
    {
        return min;
    }

    public void setMin(int min)
    {
        this.min = min;
    }

    public int getMax()
    {
        return max;
    }

    public void setMax(int max)
    {
        this.max = max;
    }
}

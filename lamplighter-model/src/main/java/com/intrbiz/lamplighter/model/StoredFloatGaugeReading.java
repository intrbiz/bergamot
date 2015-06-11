package com.intrbiz.lamplighter.model;

import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.lamplighter.data.LamplighterDB;

@SQLTable(schema = LamplighterDB.class, name = "float_gauge_reading", since = @SQLVersion({ 1, 0, 0 }))
public class StoredFloatGaugeReading extends StoredReading
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "value", since = @SQLVersion({ 1, 0, 0 }))
    private float value;
    
    @SQLColumn(index = 2, name = "warning", since = @SQLVersion({ 1, 0, 0 }))
    private float warning;
    
    @SQLColumn(index = 3, name = "critical", since = @SQLVersion({ 1, 0, 0 }))
    private float critical;
    
    @SQLColumn(index = 4, name = "min", since = @SQLVersion({ 1, 0, 0 }))
    private float min;
    
    @SQLColumn(index = 5, name = "max", since = @SQLVersion({ 1, 0, 0 }))
    private float max;
    
    public StoredFloatGaugeReading()
    {
        super();
    }
    
    public StoredFloatGaugeReading(UUID siteId, UUID readingId, Timestamp collectedAt, float value, float warning, float critical, float min, float max)
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

    public float getValue()
    {
        return value;
    }

    public void setValue(float value)
    {
        this.value = value;
    }

    public float getWarning()
    {
        return warning;
    }

    public void setWarning(float warning)
    {
        this.warning = warning;
    }

    public float getCritical()
    {
        return critical;
    }

    public void setCritical(float critical)
    {
        this.critical = critical;
    }

    public float getMin()
    {
        return min;
    }

    public void setMin(float min)
    {
        this.min = min;
    }

    public float getMax()
    {
        return max;
    }

    public void setMax(float max)
    {
        this.max = max;
    }
}

package com.intrbiz.lamplighter.model;

import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.lamplighter.data.LamplighterDB;

@SQLTable(schema = LamplighterDB.class, name = "double_gauge_reading", since = @SQLVersion({ 1, 0, 0 }))
public class StoredDoubleGaugeReading extends StoredReading
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "value", since = @SQLVersion({ 1, 0, 0 }))
    private double value;
    
    @SQLColumn(index = 2, name = "warning", since = @SQLVersion({ 1, 0, 0 }))
    private double warning;
    
    @SQLColumn(index = 3, name = "critical", since = @SQLVersion({ 1, 0, 0 }))
    private double critical;
    
    @SQLColumn(index = 4, name = "min", since = @SQLVersion({ 1, 0, 0 }))
    private double min;
    
    @SQLColumn(index = 5, name = "max", since = @SQLVersion({ 1, 0, 0 }))
    private double max;
    
    public StoredDoubleGaugeReading()
    {
        super();
    }
    
    public StoredDoubleGaugeReading(UUID siteId, UUID readingId, Timestamp collectedAt, double value, double warning, double critical, double min, double max)
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

    public double getValue()
    {
        return value;
    }

    public void setValue(double value)
    {
        this.value = value;
    }

    public double getWarning()
    {
        return warning;
    }

    public void setWarning(double warning)
    {
        this.warning = warning;
    }

    public double getCritical()
    {
        return critical;
    }

    public void setCritical(double critical)
    {
        this.critical = critical;
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
}

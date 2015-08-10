package com.intrbiz.bergamot.model.state;

import java.util.EnumSet;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.BergamotObject;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.message.state.CheckStatsMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * The stats of a check
 */
@SQLTable(schema = BergamotDB.class, name = "check_stats", since = @SQLVersion({ 1, 2, 0 }))
public class CheckStats extends BergamotObject<CheckStatsMO> implements Cloneable
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "check_id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey
    private UUID checkId;
    
    // stats
    
    @SQLColumn(index = 16, name = "last_runtime", since = @SQLVersion({ 1, 0, 0 }))
    private double lastRuntime;

    @SQLColumn(index = 17, name = "average_runtime", since = @SQLVersion({ 1, 0, 0 }))
    private double averageRuntime;

    @SQLColumn(index = 18, name = "last_check_execution_latency", since = @SQLVersion({ 1, 0, 0 }))
    private double lastCheckExecutionLatency;

    @SQLColumn(index = 19, name = "average_check_execution_latency", since = @SQLVersion({ 1, 0, 0 }))
    private double averageCheckExecutionLatency;

    @SQLColumn(index = 20, name = "last_check_processing_latency", since = @SQLVersion({ 1, 0, 0 }))
    private double lastCheckProcessingLatency;

    @SQLColumn(index = 21, name = "average_check_processing_latency", since = @SQLVersion({ 1, 0, 0 }))
    private double averageCheckProcessingLatency;

    public CheckStats()
    {
        super();
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }
    
    public double getLastRuntime()
    {
        return lastRuntime;
    }

    public void setLastRuntime(double lastRuntime)
    {
        this.lastRuntime = lastRuntime;
    }

    public double getLastCheckExecutionLatency()
    {
        return lastCheckExecutionLatency;
    }

    public void setLastCheckExecutionLatency(double lastCheckExecutionLatency)
    {
        this.lastCheckExecutionLatency = lastCheckExecutionLatency;
    }

    public double getAverageCheckExecutionLatency()
    {
        return averageCheckExecutionLatency;
    }

    public void setAverageCheckExecutionLatency(double averageCheckExecutionLatency)
    {
        this.averageCheckExecutionLatency = averageCheckExecutionLatency;
    }

    public double getLastCheckProcessingLatency()
    {
        return lastCheckProcessingLatency;
    }

    public void setLastCheckProcessingLatency(double lastCheckProcessingLatency)
    {
        this.lastCheckProcessingLatency = lastCheckProcessingLatency;
    }

    public double getAverageCheckProcessingLatency()
    {
        return averageCheckProcessingLatency;
    }

    public void setAverageCheckProcessingLatency(double averageCheckProcessingLatency)
    {
        this.averageCheckProcessingLatency = averageCheckProcessingLatency;
    }

    public double getAverageRuntime()
    {
        return averageRuntime;
    }

    public void setAverageRuntime(double averageRuntime)
    {
        this.averageRuntime = averageRuntime;
    }

    @Override
    public CheckStatsMO toMO(Contact contact, EnumSet<MOFlag> options)
    {
        CheckStatsMO mo = new CheckStatsMO();
        mo.setAverageCheckExecutionLatency(this.getAverageCheckExecutionLatency());
        mo.setAverageCheckProcessingLatency(this.getAverageCheckProcessingLatency());
        mo.setAverageRuntime(this.getAverageRuntime());
        mo.setLastRuntime(this.getLastRuntime());
        mo.setLastCheckExecutionLatency(this.getLastCheckExecutionLatency());
        mo.setLastCheckProcessingLatency(this.getLastCheckProcessingLatency());
        return mo;
    }
    
    public String toString()
    {
        return "CheckStats { check => " + this.checkId + ", last_runtime => " + this.lastRuntime + "}";
    }
    
    public CheckStats clone()
    {
        try
        {
            return (CheckStats) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }
}

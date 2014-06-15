package com.intrbiz.bergamot.model.state;

import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.CheckCfg;
import com.intrbiz.bergamot.config.model.RealCheckCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.BergamotObject;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * The state of a check
 */
@SQLTable(schema = BergamotDB.class, name = "check_state", since = @SQLVersion({ 1, 0, 0 }))
public class CheckState extends BergamotObject<CheckStateMO>
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "check_id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey
    private UUID checkId;

    /**
     * Is the check ok?
     */
    @SQLColumn(index = 2, name = "ok", since = @SQLVersion({ 1, 0, 0 }))
    private boolean ok = true;

    /**
     * Why is the check ok or not ok?
     */
    @SQLColumn(index = 3, name = "status", since = @SQLVersion({ 1, 0, 0 }))
    private Status status = Status.PENDING;

    /**
     * What was the output of the last check
     */
    @SQLColumn(index = 4, name = "output", since = @SQLVersion({ 1, 0, 0 }))
    private String output = "Pending";

    /**
     * When did the last check happen
     */
    @SQLColumn(index = 5, name = "last_check_time", since = @SQLVersion({ 1, 0, 0 }))
    private Timestamp lastCheckTime = new Timestamp(System.currentTimeMillis());

    /**
     * What was the Id of the last check
     */
    @SQLColumn(index = 6, name = "last_check_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID lastCheckId;

    /**
     * The number of attempts since the last hard state change
     */
    @SQLColumn(index = 7, name = "attempt", since = @SQLVersion({ 1, 0, 0 }))
    private int attempt = 1;

    /**
     * Has a hard state transition happened
     */
    @SQLColumn(index = 8, name = "hard", since = @SQLVersion({ 1, 0, 0 }))
    private boolean hard = true;

    /**
     * Is the state in transition
     */
    @SQLColumn(index = 9, name = "transitioning", since = @SQLVersion({ 1, 0, 0 }))
    private boolean transitioning = false;

    /**
     * Is the state flapping between ok and not ok, but never reaching a hard state
     */
    @SQLColumn(index = 10, name = "flapping", since = @SQLVersion({ 1, 0, 0 }))
    private boolean flapping = false;

    /**
     * When was the last hard state change
     */
    @SQLColumn(index = 11, name = "last_state_change", since = @SQLVersion({ 1, 0, 0 }))
    private Timestamp lastStateChange = new Timestamp(System.currentTimeMillis());

    // history

    /**
     * A bitmap of the ok history
     */
    @SQLColumn(index = 12, name = "ok_history", since = @SQLVersion({ 1, 0, 0 }))
    private long okHistory = 0x1L;

    /**
     * Was the last hard state ok?
     */
    @SQLColumn(index = 13, name = "last_hard_ok", since = @SQLVersion({ 1, 0, 0 }))
    private boolean lastHardOk = true;

    /**
     * What was the last hard status?
     */
    @SQLColumn(index = 14, name = "last_hard_status", since = @SQLVersion({ 1, 0, 0 }))
    private Status lastHardStatus = Status.PENDING;

    /**
     * What was the output of the last hard state
     */
    @SQLColumn(index = 15, name = "last_hard_output", since = @SQLVersion({ 1, 0, 0 }))
    private String lastHardOutput = "Pending";
    
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

    public CheckState()
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

    public boolean isOk()
    {
        return ok;
    }

    public void setOk(boolean ok)
    {
        this.ok = ok;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public String getOutput()
    {
        return output;
    }

    public void setOutput(String output)
    {
        this.output = output;
    }

    public Timestamp getLastCheckTime()
    {
        return lastCheckTime;
    }

    public void setLastCheckTime(Timestamp lastCheckTime)
    {
        this.lastCheckTime = lastCheckTime;
    }

    public UUID getLastCheckId()
    {
        return lastCheckId;
    }

    public void setLastCheckId(UUID lastCheckId)
    {
        this.lastCheckId = lastCheckId;
    }

    public int getAttempt()
    {
        return attempt;
    }

    public void setAttempt(int attempt)
    {
        this.attempt = attempt;
    }

    public boolean isHard()
    {
        return hard;
    }

    public void setHard(boolean hard)
    {
        this.hard = hard;
    }

    public Timestamp getLastStateChange()
    {
        return lastStateChange;
    }

    public void setLastStateChange(Timestamp lastStateChange)
    {
        this.lastStateChange = lastStateChange;
    }

    public long getOkHistory()
    {
        return okHistory;
    }

    public void setOkHistory(long okHistory)
    {
        this.okHistory = okHistory;
    }

    public void pushOkHistory(boolean ok)
    {
        this.okHistory = ((this.okHistory << 1) & 0x7FFFFFFFFFFFFFFFL) | (ok ? 1L : 0L);
    }

    public boolean isTransitioning()
    {
        return transitioning;
    }

    public void setTransitioning(boolean transitioning)
    {
        this.transitioning = transitioning;
    }

    public boolean isFlapping()
    {
        return flapping;
    }

    public void setFlapping(boolean flapping)
    {
        this.flapping = flapping;
    }

    public boolean isLastHardOk()
    {
        return lastHardOk;
    }

    public void setLastHardOk(boolean lastHardOk)
    {
        this.lastHardOk = lastHardOk;
    }

    public Status getLastHardStatus()
    {
        return lastHardStatus;
    }

    public void setLastHardStatus(Status lastHardStatus)
    {
        this.lastHardStatus = lastHardStatus;
    }

    public String getLastHardOutput()
    {
        return lastHardOutput;
    }

    public void setLastHardOutput(String lastHardOutput)
    {
        this.lastHardOutput = lastHardOutput;
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
    public CheckStateMO toMO(boolean stub)
    {
        CheckStateMO mo = new CheckStateMO();
        mo.setAttempt(this.getAttempt());
        mo.setFlapping(this.isFlapping());
        mo.setHard(this.isHard());
        mo.setLastCheckId(this.getLastCheckId());
        mo.setLastCheckTime(this.getLastCheckTime().getTime());
        mo.setLastStateChange(this.getLastStateChange().getTime());
        mo.setOk(this.isOk());
        mo.setOutput(this.getOutput());
        mo.setStatus(this.getStatus().toString());
        mo.setTransitioning(this.isTransitioning());
        mo.setLastHardOk(this.isLastHardOk());
        mo.setLastHardStatus(this.getLastHardStatus().toString());
        mo.setLastHardOutput(this.getLastHardOutput());
        mo.setAverageCheckExecutionLatency(this.getAverageCheckExecutionLatency());
        mo.setAverageCheckProcessingLatency(this.getAverageCheckProcessingLatency());
        mo.setAverageRuntime(this.getAverageRuntime());
        mo.setLastRuntime(this.getLastRuntime());
        mo.setLastCheckExecutionLatency(this.getLastCheckExecutionLatency());
        mo.setLastCheckProcessingLatency(this.getLastCheckProcessingLatency());
        return mo;
    }
    
    public void configure(CheckCfg<?> cfg)
    {
        if (cfg.getInitialState() != null)
        {
            this.setStatus(Status.valueOf(cfg.getInitialState().getStatus().toUpperCase()));
            this.setOk(this.getStatus().isOk());
            this.setOutput(Util.coalesce(cfg.getInitialState().getOutput(), ""));
            this.setLastHardStatus(this.getStatus());
            this.setLastHardOk(this.isOk());
            this.setLastHardOutput(this.getOutput());
        }
        // set the attempt
        if (cfg instanceof RealCheckCfg)
        {
            this.setAttempt(((RealCheckCfg<?>)cfg).getState().getRecoversAfter());
        }
    }
    
    public String toString()
    {
        return "CheckState { check => " + this.checkId + ", ok => " + this.ok + " }";
    }
}

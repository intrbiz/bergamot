package com.intrbiz.bergamot.model.message.state;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

@JsonTypeName("bergamot.state.check")
public class CheckStateMO extends MessageObject
{
    @JsonProperty("ok")
    private boolean ok;

    @JsonProperty("status")
    private String status;

    @JsonProperty("output")
    private String output;

    @JsonProperty("last_check_time")
    private long lastCheckTime;

    @JsonProperty("last_check_id")
    private UUID lastCheckId;

    @JsonProperty("attempt")
    private int attempt;

    @JsonProperty("hard")
    private boolean hard;

    @JsonProperty("transitioning")
    private boolean transitioning;

    @JsonProperty("flapping")
    private boolean flapping;

    @JsonProperty("last_state_change")
    private long lastStateChange;

    @JsonProperty("last_hard_ok")
    private boolean lastHardOk;

    @JsonProperty("last_hard_status")
    private String lastHardStatus;

    @JsonProperty("last_hard_output")
    private String lastHardOutput;

    @JsonProperty("in_downtime")
    private boolean inDowntime;
    
    @JsonProperty("suppressed")
    private boolean suppressed;

    public CheckStateMO()
    {
        super();
    }

    public boolean isOk()
    {
        return ok;
    }

    public void setOk(boolean ok)
    {
        this.ok = ok;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
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

    public long getLastCheckTime()
    {
        return lastCheckTime;
    }

    public void setLastCheckTime(long lastCheckTime)
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

    public long getLastStateChange()
    {
        return lastStateChange;
    }

    public void setLastStateChange(long lastStateChange)
    {
        this.lastStateChange = lastStateChange;
    }

    public boolean isLastHardOk()
    {
        return lastHardOk;
    }

    public void setLastHardOk(boolean lastHardOk)
    {
        this.lastHardOk = lastHardOk;
    }

    public String getLastHardStatus()
    {
        return lastHardStatus;
    }

    public void setLastHardStatus(String lastHardStatus)
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

    public boolean isInDowntime()
    {
        return this.inDowntime;
    }

    public void setInDowntime(boolean inDowntime)
    {
        this.inDowntime = inDowntime;
    }

    public boolean isSuppressed()
    {
        return suppressed;
    }

    public void setSuppressed(boolean suppressed)
    {
        this.suppressed = suppressed;
    }
}

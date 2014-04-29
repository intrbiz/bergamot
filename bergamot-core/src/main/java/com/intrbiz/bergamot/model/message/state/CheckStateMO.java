package com.intrbiz.bergamot.model.message.state;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.message.MessageObject;

@JsonTypeName("bergamot.state.check")
public class CheckStateMO extends MessageObject
{
    @JsonProperty("ok")
    private boolean ok;

    @JsonProperty("status")
    private Status status;

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
}

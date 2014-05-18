package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;

public abstract class CheckMO extends NamedObjectMO
{
    @JsonProperty("state")
    protected CheckStateMO state;

    @JsonProperty("suppressed")
    protected boolean suppressed;

    @JsonProperty("enabled")
    protected boolean enabled;

    public CheckMO()
    {
        super();
    }
    
    @JsonIgnore
    public abstract String getType();

    public CheckStateMO getState()
    {
        return state;
    }

    public void setState(CheckStateMO state)
    {
        this.state = state;
    }

    public boolean isSuppressed()
    {
        return suppressed;
    }

    public void setSuppressed(boolean suppressed)
    {
        this.suppressed = suppressed;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}

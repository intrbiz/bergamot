package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.alert-encompasses")
public class AlertEncompassesMO extends MessageObject
{
    @JsonProperty("check")
    private CheckMO check;

    @JsonProperty("raised")
    private long raised;

    public AlertEncompassesMO()
    {
        super();
    }

    public CheckMO getCheck()
    {
        return check;
    }

    public void setCheck(CheckMO check)
    {
        this.check = check;
    }

    public long getRaised()
    {
        return raised;
    }

    public void setRaised(long raised)
    {
        this.raised = raised;
    }
}

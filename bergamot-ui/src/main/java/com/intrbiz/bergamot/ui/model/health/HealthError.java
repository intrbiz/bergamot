package com.intrbiz.bergamot.ui.model.health;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthError
{
    @JsonProperty("message")
    private String message;

    public HealthError()
    {
    }

    public HealthError(String message)
    {
        super();
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}

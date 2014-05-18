package com.intrbiz.bergamot.model.message.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.Message;

/**
 * A message requiring work to be performed
 * 
 * All tasks have at least and engine and a name
 */
public abstract class Task extends Message
{

    @JsonProperty("engine")
    private String engine;

    @JsonProperty("name")
    private String name;

    public Task()
    {
        super();
    }

    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}

package com.intrbiz.bergamot.model.task;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.result.Result;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.model.util.Parameterised;

/**
 * Execute this check please
 */
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonTypeName("bergamot.check")
public class Check extends Task implements Parameterised
{
    @JsonProperty("checkable_type")
    private String checkableType;

    @JsonProperty("checkable_id")
    private UUID checkableId;

    @JsonProperty("engine")
    private String engine = "nagios";

    @JsonProperty("name")
    private String name;

    @JsonProperty("parameters")
    private List<Parameter> parameters = new LinkedList<Parameter>();

    @JsonProperty("timeout")
    private long timeout = 30_000L;

    @JsonProperty("scheduled")
    private long scheduled;

    public Check()
    {
        super();
    }

    public String getDefaultRoute()
    {
        return this.getType() + "." + this.getEngine();
    }

    public String getCheckableType()
    {
        return checkableType;
    }

    public void setCheckableType(String checkableType)
    {
        this.checkableType = checkableType;
    }

    public UUID getCheckableId()
    {
        return checkableId;
    }

    public void setCheckableId(UUID checkableId)
    {
        this.checkableId = checkableId;
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

    public List<Parameter> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters)
    {
        this.parameters = parameters;
    }

    public void addParameter(String name, String value)
    {
        this.parameters.add(new Parameter(name, value));
    }

    public void setParameter(String name, String value)
    {
        this.removeParameter(name);
        this.addParameter(name, value);
    }

    public void removeParameter(String name)
    {
        for (Iterator<Parameter> i = this.parameters.iterator(); i.hasNext();)
        {
            if (name.equals(i.next().getName()))
            {
                i.remove();
                break;
            }
        }
    }

    public void clearParameters()
    {
        this.parameters.clear();
    }

    public String getParameter(String name)
    {
        return this.getParameter(name, null);
    }

    public String getParameter(String name, String defaultValue)
    {
        for (Parameter parameter : this.parameters)
        {
            if (name.equals(parameter.getName())) return parameter.getValue();
        }
        return defaultValue;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public long getScheduled()
    {
        return scheduled;
    }

    public void setScheduled(long scheduled)
    {
        this.scheduled = scheduled;
    }

    /**
     * Create a Result with the details of this check
     * 
     * @return
     */
    @JsonIgnore
    public Result createResult()
    {
        Result result = new Result();
        result.setId(this.getId());
        result.setCheckableType(this.getCheckableType());
        result.setCheckableId(this.getCheckableId());
        result.setCheck(this);
        result.setExecuted(System.currentTimeMillis());
        return result;
    }
}

package com.intrbiz.bergamot.model.message;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contact message object
 */
@JsonTypeName("bergamot.command")
public class CommandMO extends SecuredObjectMO
{
    @JsonProperty("engine")
    private String engine;

    @JsonProperty("executor")
    private String executor;

    @JsonProperty("parameters")
    private List<ParameterMO> parameters = new LinkedList<ParameterMO>();
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("application")
    private String application;
    
    @JsonProperty("script")
    private String script;

    public CommandMO()
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

    public String getExecutor()
    {
        return executor;
    }

    public void setExecutor(String executor)
    {
        this.executor = executor;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getApplication()
    {
        return application;
    }

    public void setApplication(String application)
    {
        this.application = application;
    }

    public String getScript()
    {
        return script;
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    public List<ParameterMO> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<ParameterMO> parameters)
    {
        this.parameters = parameters;
    }

    public void addParameter(String name, String value)
    {
        this.parameters.add(new ParameterMO(name, value));
    }

    public void setParameter(String name, String value)
    {
        this.removeParameter(name);
        this.addParameter(name, value);
    }

    public void removeParameter(String name)
    {
        for (Iterator<ParameterMO> i = this.parameters.iterator(); i.hasNext();)
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
        for (ParameterMO parameter : this.parameters)
        {
            if (name.equals(parameter.getName())) return parameter.getValue();
        }
        return defaultValue;
    }
}

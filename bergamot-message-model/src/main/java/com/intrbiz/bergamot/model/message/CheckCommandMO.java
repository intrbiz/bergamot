package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contact message object
 */
@JsonTypeName("bergamot.command")
public class CheckCommandMO extends NamedObjectMO implements ParameterisedMO
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("command")
    private CommandMO command;

    @JsonProperty("parameters")
    private List<ParameterMO> parameters = new LinkedList<ParameterMO>();
    
    @JsonProperty("script")
    private String script;

    public CheckCommandMO()
    {
        super();
    }

    public CommandMO getCommand()
    {
        return command;
    }

    public void setCommand(CommandMO command)
    {
        this.command = command;
    }

    @Override
    public List<ParameterMO> getParameters()
    {
        return parameters;
    }

    @Override
    public void setParameters(List<ParameterMO> parameters)
    {
        this.parameters = parameters;
    }

    public String getScript()
    {
        return script;
    }

    public void setScript(String script)
    {
        this.script = script;
    }
}

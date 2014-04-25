package com.intrbiz.bergamot.compat.config.model;

import java.util.LinkedList;
import java.util.List;

import com.intrbiz.bergamot.compat.config.builder.metadata.ParameterName;
import com.intrbiz.bergamot.compat.config.builder.metadata.TypeName;
import com.intrbiz.bergamot.compat.config.parser.model.ObjectParameter;
import com.intrbiz.bergamot.model.util.Parameter;

@TypeName("command")
public class CommandCfg extends ConfigObject<CommandCfg>
{
    private String commandName;

    private String commandLine;
    
    // extended
    
    private String engine;
    
    private List<Parameter> parameters;

    public CommandCfg()
    {
    }

    public String getCommandName()
    {
        return commandName;
    }

    @ParameterName("command_name")
    public void setCommandName(String commandName)
    {
        this.commandName = commandName;
    }

    public String getCommandLine()
    {
        return commandLine;
    }
    
    public String resolveCommandLine()
    {
        return this.resolveProperty((p) -> { return p.getCommandLine(); });
    }

    @ParameterName("command_line")
    public void setCommandLine(String commandLine)
    {
        this.commandLine = commandLine;
    }    

    public String resolveCommandName()
    {
        return this.resolveProperty((p) -> { return p.getCommandName(); });
    }
    
    public String getEngine()
    {
        return engine;
    }

    @ParameterName("engine")
    public void setEngine(String engine)
    {
        this.engine = engine;
    }
    
    public String resolveEngine()
    {
        return this.resolveProperty((p) -> { return p.getEngine(); });
    }

    public List<Parameter> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters)
    {
        this.parameters = parameters;
    }
    
    public List<Parameter> resolveParameters()
    {
        return this.resolveProperty((p) -> { return p.getParameters(); });
    }

    @Override
    public boolean unhandledObjectParameter(ObjectParameter parameter)
    {
        if ("parameter".equals(parameter.getName()))
        {
            if (this.parameters == null) this.parameters = new LinkedList<Parameter>();
            System.out.println("Got parameter: " + parameter.getValue());
            this.parameters.add(Parameter.parse(parameter.getValue()));
            return true;
        }
        return super.unhandledObjectParameter(parameter);
    }
    
    
}

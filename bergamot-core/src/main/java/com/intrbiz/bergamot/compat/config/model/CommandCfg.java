package com.intrbiz.bergamot.compat.config.model;

import com.intrbiz.bergamot.compat.config.builder.metadata.ParameterName;
import com.intrbiz.bergamot.compat.config.builder.metadata.TypeName;

@TypeName("command")
public class CommandCfg extends ConfigObject<CommandCfg>
{
    private String commandName;

    private String commandLine;

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
}

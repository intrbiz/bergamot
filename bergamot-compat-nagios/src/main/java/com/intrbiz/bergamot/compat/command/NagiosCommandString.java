package com.intrbiz.bergamot.compat.command;

import java.util.ArrayList;
import java.util.List;

import com.intrbiz.Util;
import com.intrbiz.bergamot.compat.macro.MacroFrame;

/**
 * Parse Nagios check_command definitions
 */
public class NagiosCommandString
{
    private final String commandName;

    private final List<String> arguments;

    public NagiosCommandString(String commandName, List<String> arguments)
    {
        this.commandName = commandName;
        this.arguments = arguments;
    }
    
    public String getCommandName()
    {
        return this.commandName;
    }
    
    public List<String> getArguments()
    {
        return this.arguments;
    }

    public int arguments()
    {
        return this.arguments.size();
    }

    public String argument(int index)
    {
        return this.arguments.get(index);
    }

    public String toString()
    {
        return this.commandName + "(" + Util.join(", ", this.arguments) + ")";
    }
    
    public MacroFrame asMacroFrame(MacroFrame... prototypes)
    {
        MacroFrame frame = new MacroFrame(prototypes);
        int i = 1;
        for (String argument : this.arguments)
        {
            frame.put("ARG" + (i++), argument);
        }
        return frame;
    }

    public static NagiosCommandString parse(String command)
    {
        if (Util.isEmpty(command)) return null;
        // parse
        String[] parts = command.split("!");
        String commandName = parts[0];
        List<String> arguments = new ArrayList<String>(parts.length);
        for (int i = 1; i < parts.length; i++)
        {
            arguments.add(parts[i]);
        }
        return new NagiosCommandString(commandName, arguments);
    }
}

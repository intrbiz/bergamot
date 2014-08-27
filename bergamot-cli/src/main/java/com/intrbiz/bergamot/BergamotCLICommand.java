package com.intrbiz.bergamot;

import java.util.List;

import com.intrbiz.Util;

public abstract class BergamotCLICommand
{
    public BergamotCLICommand()
    {
        super();
    }
    
    public abstract String name();
    
    public abstract String usage();
    
    public abstract String help();
    
    public boolean admin()
    {
        return false;
    }
    
    public String shortHelp()
    {
        String lines[] = Util.coalesceEmpty(this.help(), "").split("\n");
        if (lines == null || lines.length == 0) return "";
        return lines[0];
    }
    
    public abstract int execute(BergamotCLI cli, List<String> args) throws Exception;
}

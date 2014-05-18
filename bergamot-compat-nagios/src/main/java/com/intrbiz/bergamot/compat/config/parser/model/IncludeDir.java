package com.intrbiz.bergamot.compat.config.parser.model;

public class IncludeDir extends Directive
{
    private final String file;
    
    public IncludeDir(String file)
    {
        this.file = file;
    }

    public String getFile()
    {
        return file;
    }
    
    public String toString()
    {
        return "include_dir " + this.file + "\r\n";
    }
}

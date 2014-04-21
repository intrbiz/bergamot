package com.intrbiz.bergamot.compat.config.parser.model;

public class IncludeFile extends Directive
{
    private final String file;
    
    public IncludeFile(String file)
    {
        this.file = file;
    }

    public String getFile()
    {
        return file;
    }
    
    public String toString()
    {
        return "include_file " + this.file + "\r\n";
    }
}

package com.intrbiz.bergamot.model;

import java.io.Serializable;


public class Change implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String summary;
    
    private String configuration;
    
    public Change()
    {
        super();
    }
    
    public Change(String summary, String configuration)
    {
        super();
        this.summary = summary;
        this.configuration = configuration;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(String configuration)
    {
        this.configuration = configuration;
    }
}

package com.intrbiz.bergamot.ui.model.health;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReleaseHealth
{
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("code_name")
    private String codename;

    public ReleaseHealth(String name, String version, String codename)
    {
        super();
        this.name = name;
        this.version = version;
        this.codename = codename;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getCodename()
    {
        return codename;
    }

    public void setCodename(String codename)
    {
        this.codename = codename;
    }
}

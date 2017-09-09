package com.intrbiz.bergamot.ui.model.health;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatabaseModuleHealth
{
    @JsonProperty("class_name")
    private String className;

    @JsonProperty("name")
    private String name;

    @JsonProperty("version")
    private String version;

    public DatabaseModuleHealth()
    {
        super();
    }

    public DatabaseModuleHealth(String className, String name, String version)
    {
        super();
        this.className = className;
        this.name = name;
        this.version = version;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
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
}

package com.intrbiz.bergamot.ui.model.health;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthCheck
{
    @JsonProperty("release")
    public ReleaseHealth release;
    
    @JsonProperty("database")
    private DatabaseHealth database;

    public HealthCheck()
    {
        super();
    }

    public ReleaseHealth getRelease()
    {
        return release;
    }

    public void setRelease(ReleaseHealth release)
    {
        this.release = release;
    }

    public DatabaseHealth getDatabase()
    {
        return database;
    }

    public void setDatabase(DatabaseHealth database)
    {
        this.database = database;
    }
}

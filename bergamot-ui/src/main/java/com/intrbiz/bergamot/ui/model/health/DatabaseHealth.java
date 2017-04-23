package com.intrbiz.bergamot.ui.model.health;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatabaseHealth
{    
    @JsonProperty("modules")
    private List<DatabaseModuleHealth> modules = new LinkedList<DatabaseModuleHealth>();
    
    public DatabaseHealth()
    {
    }

    public List<DatabaseModuleHealth> getModules()
    {
        return modules;
    }

    public void setModules(List<DatabaseModuleHealth> modules)
    {
        this.modules = modules;
    }
}

package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A grouping of checks by category and then application
 */
public class Category<T>
{
    private String name;
    
    private List<T> checks = new LinkedList<T>();
    
    private Map<String, Application<T>> applications = new TreeMap<String, Application<T>>();
    
    public Category(String name)
    {
        super();
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public Collection<T> getChecks()
    {
        return checks;
    }
    
    public void addCheck(T check)
    {
        this.checks.add(check);
    }

    public Collection<Application<T>> getApplications()
    {
        return applications.values();
    }

    public Application<T> getOrAddApplication(String name)
    {
        Application<T> application = this.applications.get(name.toLowerCase());
        if (application == null)
        {
            application = new Application<T>(name);
            this.applications.put(name.toLowerCase(), application);
        }
        return application;
    }
}

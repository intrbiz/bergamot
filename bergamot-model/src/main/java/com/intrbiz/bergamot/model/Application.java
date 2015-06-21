package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A grouping of checks by application
 */
public class Application<T>
{
    private String name;
    
    private List<T> checks = new LinkedList<T>();
    
    public Application(String name)
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
}

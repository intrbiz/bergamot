package com.intrbiz.bergamot.model;

import java.util.HashSet;
import java.util.Set;

import com.intrbiz.bergamot.model.virtual.VirtualCheckOperator;

/**
 * A virtual check is conditional upon the state of 
 * other checks
 */
public abstract class VirtualCheck extends Check
{
    /**
     * Checks which this check references
     */
    protected Set<Check> references = new HashSet<Check>();
    
    private VirtualCheckOperator condition;
    
    public VirtualCheck()
    {
        super();
    }
    
    public Set<Check> getReferences()
    {
        return references;
    }

    public void setReferences(Set<Check> references)
    {
        this.references = references;
    }
    
    public void addReference(Check check)
    {
        this.references.add(check);
    }

    public VirtualCheckOperator getCondition()
    {
        return condition;
    }

    public void setCondition(VirtualCheckOperator condition)
    {
        this.condition = condition;
    }
}

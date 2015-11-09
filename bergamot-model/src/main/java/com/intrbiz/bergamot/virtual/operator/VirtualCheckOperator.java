package com.intrbiz.bergamot.virtual.operator;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

public abstract class VirtualCheckOperator implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    public abstract boolean computeOk(VirtualCheckExpressionContext context);
    
    public abstract Status computeStatus(VirtualCheckExpressionContext context);
    
    public final Set<CheckReference> computeDependencies()
    {
        Set<CheckReference> checks = new HashSet<CheckReference>();
        this.computeDependencies(checks);
        return checks;
    }
    
    
    /**
     * Are all dependent checks in a hard state?
     */
    public boolean isAllDependenciesHard(VirtualCheckExpressionContext context)
    {
        for (CheckReference check : this.computeDependencies())
        {
            if (! check.resolve(context).getState().isHard())
                return false;
        }
        return true;
    }
    
    public abstract void computeDependencies(Set<CheckReference> checks);
}

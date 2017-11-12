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
    
    public final Set<CheckReference<?>> computeDependencies(VirtualCheckExpressionContext context)
    {
        Set<CheckReference<?>> checks = new HashSet<CheckReference<?>>();
        this.computeDependencies(context, checks);
        return checks;
    }
    
    public final Set<String> computePoolDependencies(VirtualCheckExpressionContext context)
    {
        Set<String> pools = new HashSet<String>();
        this.computePoolDependencies(context, pools);
        return pools;
    }
    
    /**
     * Are all dependent checks in a hard state?
     */
    public abstract boolean isAllDependenciesHard(VirtualCheckExpressionContext context);
    
    public abstract void computeDependencies(VirtualCheckExpressionContext context, Set<CheckReference<?>> checks);
    
    public abstract void computePoolDependencies(VirtualCheckExpressionContext context, Set<String> pools);
}

package com.intrbiz.bergamot.virtual.operator;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

public abstract class ValuesOperator implements Serializable
{
    private static final long serialVersionUID = 1L;

    public abstract List<ValueOperator> getValues(VirtualCheckExpressionContext context);
    
    public boolean isAllDependenciesHard(VirtualCheckExpressionContext context)
    {
        for (ValueOperator value : this.getValues(context))
        {
            if (! value.isAllDependenciesHard(context))
                return false;
        }
        return true;
    }
    
    public abstract void computeDependencies(VirtualCheckExpressionContext context, Set<CheckReference<?>> checks);
    
    public abstract void computePoolDependencies(VirtualCheckExpressionContext context, Set<String> pools);
}

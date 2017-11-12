package com.intrbiz.bergamot.virtual.operator;

import java.util.List;
import java.util.Set;

import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

public class ListValue extends ValuesOperator
{
    private static final long serialVersionUID = 1L;
    
    private List<ValueOperator> values;

    public ListValue(List<ValueOperator> values)
    {
        super();
        this.values = values;
    }

    @Override
    public List<ValueOperator> getValues(VirtualCheckExpressionContext context)
    {
        return this.values;
    }

    @Override
    public void computeDependencies(VirtualCheckExpressionContext context, Set<CheckReference<?>> checks)
    {
        for (ValueOperator value : this.values)
        {
            checks.add(value.getCheck());
        }
    }

    @Override
    public void computePoolDependencies(VirtualCheckExpressionContext context, Set<String> pools)
    {
    }

    @Override
    public String toString()
    {
        return this.values.toString();
    }
}

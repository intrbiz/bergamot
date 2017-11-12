package com.intrbiz.bergamot.virtual.operator;

import java.util.Set;

import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

public abstract class ValuesFunction extends VirtualCheckOperator
{
    private static final long serialVersionUID = 1L;

    protected final ValuesOperator values;

    public ValuesFunction(ValuesOperator values)
    {
        super();
        this.values = values;
    }

    public ValuesOperator getValues()
    {
        return values;
    }

    @Override
    public boolean isAllDependenciesHard(VirtualCheckExpressionContext context)
    {
        return this.values.isAllDependenciesHard(context);
    }

    @Override
    public void computeDependencies(VirtualCheckExpressionContext context, Set<CheckReference<?>> checks)
    {
        this.values.computeDependencies(context, checks);
    }

    @Override
    public void computePoolDependencies(VirtualCheckExpressionContext context, Set<String> pools)
    {
        this.values.computePoolDependencies(context, pools);
    }

    public String toString()
    {
        return this.values.toString();
    }
}

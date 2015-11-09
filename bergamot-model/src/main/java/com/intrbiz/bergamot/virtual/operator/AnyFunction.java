package com.intrbiz.bergamot.virtual.operator;

import java.util.List;
import java.util.Set;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

public class AnyFunction extends VirtualCheckOperator
{
    private static final long serialVersionUID = 1L;

    private final List<ValueOperator> checks;

    public AnyFunction(List<ValueOperator> checks)
    {
        super();
        this.checks = checks;
    }

    public List<ValueOperator> getChecks()
    {
        return checks;
    }

    @Override
    public boolean computeOk(VirtualCheckExpressionContext context)
    {
        boolean ret = true;
        for (ValueOperator check : this.checks)
        {
            ret = ret | check.computeOk(context);
        }
        return ret;
    }

    public Status computeStatus(VirtualCheckExpressionContext context)
    {
        Status ret = Status.OK;
        for (ValueOperator check : this.checks)
        {
            ret = Status.best(ret, check.computeStatus(context));
        }
        return ret;
    }

    @Override
    public void computeDependencies(Set<CheckReference> checks)
    {
        for (ValueOperator check : this.checks)
        {
            check.computeDependencies(checks);
        }
    }

    public String toString()
    {
        return "any of " + this.checks.toString();
    }
}

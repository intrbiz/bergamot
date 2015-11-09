package com.intrbiz.bergamot.virtual.operator;

import java.util.List;
import java.util.Set;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

public class OneOfFunction extends VirtualCheckOperator
{
    private static final long serialVersionUID = 1L;

    private final List<ValueOperator> checks;
    
    private final Status as;

    public OneOfFunction(List<ValueOperator> checks, Status as)
    {
        super();
        this.checks = checks;
        this.as = as == null ? Status.CRITICAL : as;
    }

    public List<ValueOperator> getChecks()
    {
        return checks;
    }

    @Override
    public boolean computeOk(VirtualCheckExpressionContext context)
    {
        int okCount = 0;
        for (ValueOperator check : this.checks)
        {
            if (check.computeOk(context))
                okCount++;
        }
        return okCount == 1;
    }

    public Status computeStatus(VirtualCheckExpressionContext context)
    {
        // we are making an implicit decision with this operator, so:
        return this.computeOk(context) ? Status.OK : this.as;
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
        return "one of " + this.checks.toString() + " as " + this.as.toString();
    }
}

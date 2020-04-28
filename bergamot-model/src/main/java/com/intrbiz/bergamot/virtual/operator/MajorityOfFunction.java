package com.intrbiz.bergamot.virtual.operator;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class MajorityOfFunction extends ValuesFunction
{    
    private static final long serialVersionUID = 1L;
    
    private final Status warningAs;
    
    private final Status criticalAs;

    public MajorityOfFunction(ValuesOperator values, Status warningAs, Status criticalAs)
    {
        super(values);
        this.warningAs  = warningAs == null ? Status.WARNING : warningAs;
        this.criticalAs = criticalAs == null ? Status.CRITICAL : criticalAs;
    }

    public Status getWarningAs()
    {
        return this.warningAs;
    }

    public Status getCriticalAs()
    {
        return this.criticalAs;
    }

    @Override
    public boolean computeOk(VirtualCheckExpressionContext context)
    {
        return this.computeStatus(context).isOk();
    }

    public Status computeStatus(VirtualCheckExpressionContext context)
    {
        int totalCount = 0;
        int okCount = 0;
        for (ValueOperator check : this.values.getValues(context))
        {
            totalCount++;
            if (check.computeOk(context))
                okCount++;
        }
        // Decide the status
        if (totalCount > 2)
        {
            if (okCount == totalCount)
            {
                // All members are healthy
                return Status.OK;
            }
            else if (okCount > (totalCount / 2))
            {
                // We have quorum but have lost members
                return this.warningAs;
            }
        }
        // We do not have strict quorum
        return this.criticalAs;
    }

    public String toString()
    {
        return "majority of " + this.values.toString() + " as " + this.warningAs.toString() + ", " + this.criticalAs.toString();
    }
}

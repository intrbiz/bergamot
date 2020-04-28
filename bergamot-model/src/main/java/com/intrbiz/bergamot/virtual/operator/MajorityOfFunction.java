package com.intrbiz.bergamot.virtual.operator;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class MajorityOfFunction extends ValuesFunction
{    
    private static final long serialVersionUID = 1L;
    
    private final Status as;

    public MajorityOfFunction(ValuesOperator values, Status as)
    {
        super(values);
        this.as = as == null ? Status.CRITICAL : as;
    }
    
    public Status getAs()
    {
        return this.as;
    }

    @Override
    public boolean computeOk(VirtualCheckExpressionContext context)
    {
        int totalCount = 0;
        int okCount = 0;
        for (ValueOperator check : this.values.getValues(context))
        {
            totalCount++;
            if (check.computeOk(context))
                okCount++;
        }
        return isQuorum(okCount, totalCount);
    }
    
    /**
     * Check for strict quorum.
     * @param okCount the number of nodes ok
     * @param totalCount the number of nodes in total
     * @return true if and only if quorum exists
     */
    public static boolean isQuorum(int okCount, int totalCount)
    {
        return totalCount > 2 && okCount > (totalCount / 2);
    }

    public Status computeStatus(VirtualCheckExpressionContext context)
    {
        // we are making an implicit decision with this operator, so:
        return this.computeOk(context) ? Status.OK : this.as;
    }

    public String toString()
    {
        return "majority of " + this.values.toString() + " as " + this.as.toString();
    }
}

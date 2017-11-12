package com.intrbiz.bergamot.virtual.operator;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class AllFunction extends ValuesFunction
{
    private static final long serialVersionUID = 1L;

    public AllFunction(ValuesOperator values)
    {
        super(values);
    }

    @Override
    public boolean computeOk(VirtualCheckExpressionContext context)
    {
        boolean ret = true;
        for (ValueOperator check : this.values.getValues(context))
        {
            ret = ret & check.computeOk(context);
        }
        return ret;
    }

    public Status computeStatus(VirtualCheckExpressionContext context)
    {
        Status ret = Status.OK;
        for (ValueOperator check : this.values.getValues(context))
        {
            ret = Status.worst(ret, check.computeStatus(context));
        }
        return ret;
    }

    public String toString()
    {
        return "all of " + this.values.toString();
    }
}

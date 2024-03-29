package com.intrbiz.bergamot.virtual.operator;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class OneOfFunction extends ValuesFunction
{    
    private static final long serialVersionUID = 1L;
    
    public enum Count {
            one(1),
            two(2),
            three(3),
            four(4),
            five(5),
            six(6),
            seven(7),
            eight(8),
            nine(9);
        
        private final int count;
        
        private Count(int count)
        {
            this.count = count;
        }
        
        public int getCount()
        {
            return count;
        }
    }
    
    private final Count count;
    
    private final Status as;

    public OneOfFunction(ValuesOperator values, String count, Status as)
    {
        super(values);
        this.count = Count.valueOf(count);
        this.as = as == null ? Status.CRITICAL : as;
    }
    
    public Count getCount()
    {
        return count;
    }
    
    public Status getAs()
    {
        return this.as;
    }

    @Override
    public boolean computeOk(VirtualCheckExpressionContext context)
    {
        int okCount = 0;
        for (ValueOperator check : this.values.getValues(context))
        {
            if (check.computeOk(context))
                okCount++;
        }
        return okCount == this.count.getCount();
    }

    public Status computeStatus(VirtualCheckExpressionContext context)
    {
        // we are making an implicit decision with this operator, so:
        return this.computeOk(context) ? Status.OK : this.as;
    }

    public String toString()
    {
        return this.count + " of " + this.values.toString() + " as " + this.as.toString();
    }
}

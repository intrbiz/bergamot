package com.intrbiz.bergamot.virtual.operator;

import java.util.List;
import java.util.Set;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

public class CountFunction extends VirtualCheckOperator
{
    private static final long serialVersionUID = 1L;

    private final Status status;

    private final List<ValueOperator> checks;

    private final String test;

    private final int value;
    
    private final Status as;

    public CountFunction(Status status, List<ValueOperator> checks, String test, int value, Status as)
    {
        super();
        this.status = status;
        this.checks = checks;
        this.test = test;
        this.value = value;
        this.as = as == null ? Status.CRITICAL : as;
    }

    public Status getStatus()
    {
        return status;
    }

    public String getTest()
    {
        return test;
    }

    public int getValue()
    {
        return value;
    }

    public List<ValueOperator> getChecks()
    {
        return checks;
    }

    @Override
    public boolean computeOk(VirtualCheckExpressionContext context)
    {
        // count the number of checks matching
        int count = 0;
        for (ValueOperator check : this.checks)
        {
            if (this.status == null)
            {
                if (check.computeOk(context))
                    count ++;
            }
            else
            {
                if (check.computeStatus(context) == this.status)
                    count ++;
            }
        }
        // apply the comparison
        switch (this.test)
        {
            case "=="  : case "eq"  : return count == this.value;
            case "!="  : case "ne"  : return count != this.value;
            case "<"   : case "lt"  : return count <  this.value;
            case "<="  : case "lteq": return count <= this.value;
            case ">"   : case "gt"  : return count >  this.value;
            case ">="  : case "gteq": return count >= this.value;
        }
        throw new RuntimeException("Unexpected equality");
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
        return "count " + (this.status == null ? "" : this.status.toString()) + " of " + this.checks.toString() + " is " + this.test + " " + this.value + " as " + this.as;
    }
}

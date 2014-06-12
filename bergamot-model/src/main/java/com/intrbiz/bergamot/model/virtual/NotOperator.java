package com.intrbiz.bergamot.model.virtual;

import java.util.Set;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Status;

public class NotOperator extends VirtualCheckOperator
{
    private static final long serialVersionUID = 1L;
    
    private final VirtualCheckOperator operand;
    
    public NotOperator(VirtualCheckOperator operand)
    {
        super();
        this.operand = operand;
    }

    public VirtualCheckOperator geOperand()
    {
        return this.operand;
    }

    @Override
    public boolean computeOk()
    {
        return ! this.operand.computeOk();
    }

    public Status computeStatus()
    {
        // we are making an implicit decision with this operator, so:
        return this.computeOk() ? Status.OK : Status.CRITICAL;
    }

    @Override
    public void computeDependencies(Set<Check<?,?>> checks)
    {
        this.operand.computeDependencies(checks);
    }
    
    public String toString()
    {
        return "! " + this.operand.toString();
    }
}

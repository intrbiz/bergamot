package com.intrbiz.bergamot.model.virtual;

import java.util.Set;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Status;

public class BracketOperator extends VirtualCheckOperator
{
    private final VirtualCheckOperator operand;
    
    public BracketOperator(VirtualCheckOperator operand)
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
        return this.operand.computeOk();
    }

    public Status computeStatus()
    {
        return this.operand.computeStatus();
    }

    @Override
    public void computeDependencies(Set<Check<?>> checks)
    {
        this.operand.computeDependencies(checks);
    }
    
    public String toString()
    {
        return "( " + this.operand.toString() + " )";
    }
}

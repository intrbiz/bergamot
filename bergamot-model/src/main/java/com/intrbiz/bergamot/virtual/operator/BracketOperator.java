package com.intrbiz.bergamot.virtual.operator;

import java.util.Set;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

public class BracketOperator extends VirtualCheckOperator
{
    private static final long serialVersionUID = 1L;
    
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
    public boolean computeOk(VirtualCheckExpressionContext context)
    {
        return this.operand.computeOk(context);
    }

    public Status computeStatus(VirtualCheckExpressionContext context)
    {
        return this.operand.computeStatus(context);
    }

    @Override
    public void computeDependencies(Set<CheckReference> checks)
    {
        this.operand.computeDependencies(checks);
    }
    
    public String toString()
    {
        return "( " + this.operand.toString() + " )";
    }
}

package com.intrbiz.bergamot.virtual.operator;

import java.util.Set;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

public class AndOperator extends VirtualCheckOperator
{
    private static final long serialVersionUID = 1L;
    
    private final VirtualCheckOperator left;
    
    private final VirtualCheckOperator right;
    
    public AndOperator(VirtualCheckOperator left, VirtualCheckOperator right)
    {
        super();
        this.left = left;
        this.right = right;
    }

    public VirtualCheckOperator getLeft()
    {
        return left;
    }

    public VirtualCheckOperator getRight()
    {
        return right;
    }

    @Override
    public boolean computeOk(VirtualCheckExpressionContext context)
    {
        return this.left.computeOk(context) && this.right.computeOk(context);
    }

    public Status computeStatus(VirtualCheckExpressionContext context)
    {
        return Status.worst(this.left.computeStatus(context), this.right.computeStatus(context));
    }

    @Override
    public void computeDependencies(Set<CheckReference> checks)
    {
        this.left.computeDependencies(checks);
        this.right.computeDependencies(checks);
    }
    
    public String toString()
    {
        return this.left.toString() + " && " + this.right.toString();
    }
}

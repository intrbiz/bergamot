package com.intrbiz.bergamot.virtual.operator;

import java.util.Set;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

public class XorOperator extends VirtualCheckOperator
{
    private static final long serialVersionUID = 1L;
    
    private final VirtualCheckOperator left;
    
    private final VirtualCheckOperator right;
    
    public XorOperator(VirtualCheckOperator left, VirtualCheckOperator right)
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
        return this.left.computeOk(context) ^ this.right.computeOk(context);
    }

    public Status computeStatus(VirtualCheckExpressionContext context)
    {
        // we are making an implicit decision with this operator, so:
        return this.computeOk(context) ? Status.OK : Status.CRITICAL;
    }

    @Override
    public void computeDependencies(Set<CheckReference> checks)
    {
        this.left.computeDependencies(checks);
        this.right.computeDependencies(checks);
    }
    
    public String toString()
    {
        return this.left.toString() + " ^ " + this.right.toString();
    }
}

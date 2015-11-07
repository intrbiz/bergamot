package com.intrbiz.bergamot.virtual.operator;

import java.util.Set;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Status;

public class OrOperator extends VirtualCheckOperator
{
    private static final long serialVersionUID = 1L;
    
    private final VirtualCheckOperator left;
    
    private final VirtualCheckOperator right;
    
    public OrOperator(VirtualCheckOperator left, VirtualCheckOperator right)
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
    public boolean computeOk()
    {
        return this.left.computeOk() || this.right.computeOk();
    }

    public Status computeStatus()
    {
        return Status.best(this.left.computeStatus(), this.right.computeStatus());
    }

    @Override
    public void computeDependencies(Set<Check<?,?>> checks)
    {
        this.left.computeDependencies(checks);
        this.right.computeDependencies(checks);
    }
    
    public String toString()
    {
        return this.left.toString() + " || " + this.right.toString();
    }
}

package com.intrbiz.bergamot.virtual.operator;

import java.util.Set;

import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;
import com.intrbiz.bergamot.virtual.reference.CheckReference;

public class ValueOperator extends VirtualCheckOperator
{
    private static final long serialVersionUID = 1L;
    
    private final CheckReference check;
    
    public ValueOperator(CheckReference check)
    {
        super();
        this.check = check;
    }
    
    public CheckReference getCheck()
    {
        return this.check;
    }
    
    public void computeDependencies(Set<CheckReference> checks)
    {
        checks.add(this.check);
    }

    @Override
    public boolean computeOk(VirtualCheckExpressionContext context)
    {
        return check.resolve(context).getState().isOk();
    }

    public Status computeStatus(VirtualCheckExpressionContext context)
    {
        return this.check.resolve(context).getState().getStatus();
    }
    
    public String toString()
    {
       return this.check.toString();
    }
}

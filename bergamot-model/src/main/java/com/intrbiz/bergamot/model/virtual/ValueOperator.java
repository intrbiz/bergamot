package com.intrbiz.bergamot.model.virtual;

import java.util.Set;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Status;

public class ValueOperator extends VirtualCheckOperator
{
    private final Check<?> check;
    
    public ValueOperator(Check<?> check)
    {
        super();
        this.check = check;
    }
    
    public Check<?> getCheck()
    {
        return this.check;
    }
    
    public void computeDependencies(Set<Check<?>> checks)
    {
        checks.add(this.check);
    }

    @Override
    public boolean computeOk()
    {
        return check.getState().isOk();
    }

    public Status computeStatus()
    {
        return this.check.getState().getStatus();
    }
    
    public String toString()
    {
       return this.check.getType() + " " + this.check.getId();
    }
}

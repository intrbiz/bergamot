package com.intrbiz.bergamot.model;

import com.intrbiz.express.value.ValueExpression;

/**
 * A virtual check is conditional upon the state of 
 * other checks
 */
public abstract class VirtualCheck extends Check
{
    private ValueExpression condition;
    
    public VirtualCheck()
    {
        super();
    }

    public ValueExpression getCondition()
    {
        return condition;
    }

    public void setCondition(ValueExpression condition)
    {
        this.condition = condition;
    }
}

package com.intrbiz.bergamot.config.model;

import javax.xml.bind.annotation.XmlElement;

import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;

public abstract class VirtualCheckCfg<P extends VirtualCheckCfg<P>> extends CheckCfg<P>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Currently the condition can only be validated at runtime
     */
    private String condition;

    public VirtualCheckCfg()
    {
        super();
    }

    @XmlElement(name = "condition")
    @ResolveWith(CoalesceEmptyString.class)
    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }
}

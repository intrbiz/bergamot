package com.intrbiz.bergamot.config.model;

import javax.xml.bind.annotation.XmlElementRef;

import com.intrbiz.bergamot.config.resolver.BeanResolver;
import com.intrbiz.bergamot.config.resolver.ResolveWith;

public abstract class RealCheckCfg<P extends RealCheckCfg<P>> extends CheckCfg<P>
{
    private StateCfg state;

    public RealCheckCfg()
    {
        super();
    }

    @XmlElementRef(type = StateCfg.class)
    @ResolveWith(BeanResolver.class)
    public StateCfg getState()
    {
        return state;
    }

    public void setState(StateCfg state)
    {
        this.state = state;
    }
}

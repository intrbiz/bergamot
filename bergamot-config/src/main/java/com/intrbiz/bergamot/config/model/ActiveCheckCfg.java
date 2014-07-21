package com.intrbiz.bergamot.config.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;

import com.intrbiz.bergamot.config.resolver.BeanResolver;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;

public abstract class ActiveCheckCfg<P extends ActiveCheckCfg<P>> extends RealCheckCfg<P>
{
    private static final long serialVersionUID = 1L;

    private ScheduleCfg schedule;

    private String workerPool;

    public ActiveCheckCfg()
    {
        super();
    }

    @XmlElementRef(type = ScheduleCfg.class)
    @ResolveWith(BeanResolver.class)
    public ScheduleCfg getSchedule()
    {
        return schedule;
    }

    public void setSchedule(ScheduleCfg schedule)
    {
        this.schedule = schedule;
    }

    @XmlAttribute(name = "worker-pool")
    @ResolveWith(CoalesceEmptyString.class)
    public String getWorkerPool()
    {
        return workerPool;
    }

    public void setWorkerPool(String workerPool)
    {
        this.workerPool = workerPool;
    }

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = super.getTemplatedChildObjects();
        return r;
    }
}

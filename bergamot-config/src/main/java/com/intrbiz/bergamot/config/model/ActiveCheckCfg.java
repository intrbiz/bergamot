package com.intrbiz.bergamot.config.model;

import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.resolver.BeanResolver;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.Coalesce;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;
import com.intrbiz.util.uuid.UUIDAdapter;

public abstract class ActiveCheckCfg<P extends ActiveCheckCfg<P>> extends RealCheckCfg<P>
{
    private static final long serialVersionUID = 1L;

    private ScheduleCfg schedule;

    private String workerPool;
    
    private UUID agentId;

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
    
    @XmlAttribute(name = "agent-id")
    @XmlJavaTypeAdapter(UUIDAdapter.class)
    @ResolveWith(Coalesce.class)
    public UUID getAgentId()
    {
        return agentId;
    }

    public void setAgentId(UUID agentId)
    {
        this.agentId = agentId;
    }

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = super.getTemplatedChildObjects();
        return r;
    }
}

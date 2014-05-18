package com.intrbiz.bergamot.config.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.Coalesce;

@XmlType(name = "state")
@XmlRootElement(name = "state")
public class StateCfg
{
    private Integer failedAfter;

    private Integer recoversAfter;

    public StateCfg()
    {
        super();
    }

    @XmlAttribute(name = "failed-after")
    @ResolveWith(Coalesce.class)
    public Integer getFailedAfter()
    {
        return failedAfter;
    }

    public void setFailedAfter(Integer failedAfter)
    {
        this.failedAfter = failedAfter;
    }

    @XmlAttribute(name = "recovers-after")
    @ResolveWith(Coalesce.class)
    public Integer getRecoversAfter()
    {
        return recoversAfter;
    }

    public void setRecoversAfter(Integer recoversAfter)
    {
        this.recoversAfter = recoversAfter;
    }
}

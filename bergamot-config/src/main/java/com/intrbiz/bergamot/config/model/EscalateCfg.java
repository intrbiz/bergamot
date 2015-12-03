package com.intrbiz.bergamot.config.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.adapter.YesNoAdapter;
import com.intrbiz.bergamot.config.resolver.BeanResolver;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.Coalesce;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyCollection;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;
import com.intrbiz.bergamot.util.TimeInterval;

@XmlType(name = "escalate")
@XmlRootElement(name = "escalate")
public class EscalateCfg implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String after;
    
    private String escalationPeriod;
    
    private Set<String> ignore = new LinkedHashSet<String>();
    
    private NotifyCfg notify;
    
    private Boolean renotify;
    
    public EscalateCfg()
    {
        super();
    }
    
    @XmlElementRef(type = NotifyCfg.class)
    @ResolveWith(BeanResolver.class)
    public NotifyCfg getNotify()
    {
        return notify;
    }

    public void setNotify(NotifyCfg notify)
    {
        this.notify = notify;
    }

    @XmlAttribute(name = "after")
    @ResolveWith(Coalesce.class)
    public String getAfter()
    {
        return after;
    }

    public void setAfter(String after)
    {
        this.after = after;
    }
    
    public TimeInterval getAfterTimeInterval()
    {
        return this.after == null ? null : TimeInterval.fromString(this.after);
    }
    
    public TimeInterval getAfterTimeInterval(TimeInterval defaultValue)
    {
        return Util.coalesce(this.getAfterTimeInterval(), defaultValue);
    }

    @XmlAttribute(name="time-period")
    @ResolveWith(CoalesceEmptyString.class)
    public String getEscalationPeriod()
    {
        return escalationPeriod;
    }

    public void setEscalationPeriod(String escalationPeriod)
    {
        this.escalationPeriod = escalationPeriod;
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "ignore")
    @ResolveWith(CoalesceEmptyCollection.class)
    public Set<String> getIgnore()
    {
        return ignore;
    }

    public void setIgnore(Set<String> ignore)
    {
        this.ignore = ignore;
    }
    
    public boolean isIgnore(String state)
    {
        return this.getIgnore().contains(state);
    }

    @XmlJavaTypeAdapter(YesNoAdapter.class)
    @XmlAttribute(name = "renotify")
    @ResolveWith(Coalesce.class)
    public Boolean isRenotify()
    {
        return renotify;
    }

    public void setRenotify(Boolean renotify)
    {
        this.renotify = renotify;
    }
    
    @XmlTransient
    public boolean getRenotifyBooleanValue()
    {
        return this.renotify == null ? false : this.renotify.booleanValue();
    }
}

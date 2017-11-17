package com.intrbiz.bergamot.config.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.adapter.YesNoAdapter;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.Coalesce;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyCollection;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;

@XmlType(name = "updates")
@XmlRootElement(name = "updates")
public class UpdateNotificationsCfg implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String updatesPeriod;
    
    private Set<String> ignore = new LinkedHashSet<String>();
    
    private Boolean enabled;
    
    public UpdateNotificationsCfg()
    {
        super();
    }

    @XmlAttribute(name="time-period")
    @ResolveWith(CoalesceEmptyString.class)
    public String getUpdatesPeriod()
    {
        return updatesPeriod;
    }

    public void setUpdatesPeriod(String updatesPeriod)
    {
        this.updatesPeriod = updatesPeriod;
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
    @XmlAttribute(name = "enabled")
    @ResolveWith(Coalesce.class)
    public Boolean getEnabled()
    {
        return enabled;
    }
    
    @XmlTransient
    public boolean getEnabledBooleanValue()
    {
        return this.enabled == null ? false : this.enabled.booleanValue();
    }

    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }
}

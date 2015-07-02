package com.intrbiz.bergamot.config.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
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

@XmlType(name = "notifications")
@XmlRootElement(name = "notifications")
public class NotificationsCfg implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Boolean enabled;
    
    private String notificationPeriod;
    
    private Boolean alerts;
    
    private Boolean recovery;

    private Boolean acknowledge;
    
    private Set<String> ignore = new LinkedHashSet<String>();
    
    private List<NotificationEngineCfg> notificationEngines = new LinkedList<NotificationEngineCfg>();
    
    private Boolean allEnginesEnabled;
    
    public NotificationsCfg()
    {
        super();
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
        return this.enabled == null ? true : this.enabled.booleanValue();
    }

    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    @XmlAttribute(name="time-period")
    @ResolveWith(CoalesceEmptyString.class)
    public String getNotificationPeriod()
    {
        return notificationPeriod;
    }

    public void setNotificationPeriod(String notificationPeriod)
    {
        this.notificationPeriod = notificationPeriod;
    }
    
    @XmlJavaTypeAdapter(YesNoAdapter.class)
    @XmlAttribute(name = "alerts")
    @ResolveWith(Coalesce.class)
    public Boolean getAlerts()
    {
        return alerts;
    }
    
    public boolean getAlertsBooleanValue()
    {
        return this.alerts == null ? true : this.alerts.booleanValue();
    }

    public void setAlerts(Boolean alerts)
    {
        this.alerts = alerts;
    }

    @XmlJavaTypeAdapter(YesNoAdapter.class)
    @XmlAttribute(name = "recovery")
    @ResolveWith(Coalesce.class)
    public Boolean getRecovery()
    {
        return recovery;
    }
    
    public boolean getRecoveryBooleanValue()
    {
        return this.recovery == null ? true : this.recovery.booleanValue();
    }

    public void setRecovery(Boolean recovery)
    {
        this.recovery = recovery;
    }

    @XmlJavaTypeAdapter(YesNoAdapter.class)
    @XmlAttribute(name = "acknowledge")
    @ResolveWith(Coalesce.class)
    public Boolean getAcknowledge()
    {
        return acknowledge;
    }
    
    public boolean getAcknowledgeBooleanValue()
    {
        return this.acknowledge == null ? true : this.acknowledge.booleanValue();
    }

    public void setAcknowledge(Boolean acknowledge)
    {
        this.acknowledge = acknowledge;
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
    
    @XmlElementRef(type = NotificationEngineCfg.class)
    @ResolveWith(CoalesceEmptyCollection.class)
    public List<NotificationEngineCfg> getNotificationEngines()
    {
        return notificationEngines;
    }

    public void setNotificationEngines(List<NotificationEngineCfg> notificationEngines)
    {
        this.notificationEngines = notificationEngines;
    }

    @XmlJavaTypeAdapter(YesNoAdapter.class)
    @XmlAttribute(name = "all-engines")
    @ResolveWith(Coalesce.class)
    public Boolean getAllEnginesEnabled()
    {
        return allEnginesEnabled;
    }
    
    public boolean getAllEnginesEnabledBooleanValue()
    {
        return this.recovery == null ? true : this.recovery.booleanValue();
    }

    public void setAllEnginesEnabled(Boolean allEnginesEnabled)
    {
        this.allEnginesEnabled = allEnginesEnabled;
    }
}

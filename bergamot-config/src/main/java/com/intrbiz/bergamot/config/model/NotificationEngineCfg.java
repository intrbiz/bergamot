package com.intrbiz.bergamot.config.model;

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

@XmlType(name = "notification-engine")
@XmlRootElement(name = "notification-engine")
public class NotificationEngineCfg
{
    private String engine;

    private Boolean enabled;

    private String notificationPeriod;

    private Boolean alerts;

    private Boolean recovery;

    private Set<String> ignore = new LinkedHashSet<String>();

    public NotificationEngineCfg()
    {
        super();
    }

    @XmlAttribute(name = "engine")
    @ResolveWith(CoalesceEmptyString.class)
    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
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

    @XmlAttribute(name = "time-period")
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
}

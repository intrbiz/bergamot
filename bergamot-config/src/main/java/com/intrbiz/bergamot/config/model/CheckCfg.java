package com.intrbiz.bergamot.config.model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.adapter.YesNoAdapter;
import com.intrbiz.bergamot.config.resolver.BeanResolver;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.Coalesce;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyCollection;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;

public abstract class CheckCfg<P extends CheckCfg<P>> extends NamedObjectCfg<P>
{
    private static final long serialVersionUID = 1L;
    
    private Set<String> groups = new LinkedHashSet<String>();

    private NotificationsCfg notifications;

    private NotifyCfg notify;

    private Boolean suppressed;

    private Boolean enabled;
    
    private InitiallyCfg initialState;
    
    private String externalRef;
    
    private Set<String> securityDomains = new LinkedHashSet<String>();

    public CheckCfg()
    {
        super();
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "groups")
    @ResolveWith(CoalesceEmptyCollection.class)
    public Set<String> getGroups()
    {
        return groups;
    }

    public void setGroups(Set<String> groups)
    {
        this.groups = groups;
    }
    
    public void addGroup(String group)
    {
        this.groups.add(group);
    }
    
    public void removeGroup(String group)
    {
        this.groups.remove(group);
    }
    
    public boolean containsGroup(String name)
    {
        return this.groups.contains(name);
    }

    @XmlElementRef(type = NotificationsCfg.class)
    @ResolveWith(BeanResolver.class)
    public NotificationsCfg getNotifications()
    {
        return notifications;
    }

    public void setNotifications(NotificationsCfg notifications)
    {
        this.notifications = notifications;
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

    @XmlJavaTypeAdapter(YesNoAdapter.class)
    @XmlAttribute(name = "suppressed")
    @ResolveWith(Coalesce.class)
    public Boolean getSuppressed()
    {
        return suppressed;
    }
    
    @XmlTransient
    public boolean getSuppressedBooleanValue()
    {
        return this.suppressed == null ? false : this.suppressed.booleanValue();
    }

    public void setSuppressed(Boolean suppressed)
    {
        this.suppressed = suppressed;
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

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = new LinkedList<TemplatedObjectCfg<?>>();
        return r;
    }

    @XmlElementRef(type = InitiallyCfg.class)
    @ResolveWith(Coalesce.class)
    public InitiallyCfg getInitialState()
    {
        return initialState;
    }

    public void setInitialState(InitiallyCfg initialState)
    {
        this.initialState = initialState;
    }

    @XmlElement(name = "external-ref")
    @ResolveWith(CoalesceEmptyString.class)
    public String getExternalRef()
    {
        return externalRef;
    }

    public void setExternalRef(String externalRef)
    {
        this.externalRef = externalRef;
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "security-domains")
    @ResolveWith(CoalesceEmptyCollection.class)
    public Set<String> getSecurityDomains()
    {
        return securityDomains;
    }

    public void setSecurityDomains(Set<String> securityDomains)
    {
        this.securityDomains = securityDomains;
    }
}

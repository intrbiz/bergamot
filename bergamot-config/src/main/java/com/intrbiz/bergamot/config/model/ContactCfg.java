package com.intrbiz.bergamot.config.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.resolver.BeanResolver;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyCollection;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;

@XmlType(name = "contact")
@XmlRootElement(name = "contact")
public class ContactCfg extends NamedObjectCfg<ContactCfg> implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Set<String> teams = new LinkedHashSet<String>();

    private String email;

    private String pager;

    private String mobile;

    private String phone;

    private NotificationsCfg notifications;

    public ContactCfg()
    {
        super();
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "teams")
    @ResolveWith(CoalesceEmptyCollection.class)
    public Set<String> getTeams()
    {
        return teams;
    }

    public void setTeams(Set<String> teams)
    {
        this.teams = teams;
    }

    public void addTeam(String group)
    {
        this.teams.add(group);
    }

    public void removeTeam(String group)
    {
        this.teams.remove(group);
    }

    public boolean containsTeam(String name)
    {
        return this.teams.contains(name);
    }

    @XmlElement(name = "email")
    @ResolveWith(CoalesceEmptyString.class)
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @XmlElement(name = "pager")
    @ResolveWith(CoalesceEmptyString.class)
    public String getPager()
    {
        return pager;
    }

    public void setPager(String pager)
    {
        this.pager = pager;
    }

    @XmlElement(name = "mobile")
    @ResolveWith(CoalesceEmptyString.class)
    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    @XmlElement(name = "phone")
    @ResolveWith(CoalesceEmptyString.class)
    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
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

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = new LinkedList<TemplatedObjectCfg<?>>();
        return r;
    }
}

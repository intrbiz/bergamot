package com.intrbiz.bergamot.config.model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.resolver.BeanResolver;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyCollection;
import com.intrbiz.bergamot.config.resolver.stratergy.MergeListUnique;

@XmlType(name = "contact")
@XmlRootElement(name = "contact")
public class ContactCfg extends NamedObjectCfg<ContactCfg>
{
    private Set<String> teams = new LinkedHashSet<String>();

    private List<EmailCfg> emails = new LinkedList<EmailCfg>();

    private List<PhoneCfg> phones = new LinkedList<PhoneCfg>();

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

    @XmlElementRef(type = EmailCfg.class)
    @ResolveWith(MergeListUnique.class)
    public List<EmailCfg> getEmails()
    {
        return emails;
    }

    public void setEmails(List<EmailCfg> emails)
    {
        this.emails = emails;
    }
    
    public String lookupEmail(String type)
    {
        for (EmailCfg e : this.emails)
        {
            if (type.equals(e.getType()))
                return e.getAddress();
        }
        return null;
    }

    @XmlElementRef(type = PhoneCfg.class)
    @ResolveWith(MergeListUnique.class)
    public List<PhoneCfg> getPhones()
    {
        return phones;
    }

    public void setPhones(List<PhoneCfg> phones)
    {
        this.phones = phones;
    }
    
    public String lookupPhone(String type)
    {
        for (PhoneCfg e : this.phones)
        {
            if (type.equals(e.getType()))
                return e.getNumber();
        }
        return null;
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

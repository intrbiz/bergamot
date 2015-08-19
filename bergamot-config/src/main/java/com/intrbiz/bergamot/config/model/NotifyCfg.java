package com.intrbiz.bergamot.config.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.SmartMergeSet;

@XmlType(name = "notify")
@XmlRootElement(name = "notify")
public class NotifyCfg implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private Set<String> teams = new LinkedHashSet<String>();

    private Set<String> contacts = new LinkedHashSet<String>();

    public NotifyCfg()
    {
        super();
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "teams")
    @ResolveWith(SmartMergeSet.class)
    public Set<String> getTeams()
    {
        return teams;
    }

    public void setTeams(Set<String> teams)
    {
        this.teams = teams;
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "contacts")
    @ResolveWith(SmartMergeSet.class)
    public Set<String> getContacts()
    {
        return contacts;
    }

    public void setContacts(Set<String> contacts)
    {
        this.contacts = contacts;
    }
}

package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.model.message.TeamMO;
import com.intrbiz.configuration.Configurable;

/**
 * A team of people, who can be notified
 */
public class Team extends NamedObject<TeamMO> implements Configurable<TeamCfg>
{
    private TeamCfg config;

    private List<Contact> contacts = new LinkedList<Contact>();
    
    // team hierarchy

    private Map<String, Team> parents = new TreeMap<String, Team>();

    private Map<String, Team> children = new TreeMap<String, Team>();

    public Team()
    {
        super();
    }

    @Override
    public void configure(TeamCfg cfg)
    {
        this.config = cfg;
        TeamCfg rcfg = cfg.resolve();
        this.name = rcfg.getName();
        this.summary = Util.coalesce(rcfg.getSummary(), this.name);
    }

    @Override
    public TeamCfg getConfiguration()
    {
        return this.config;
    }
    
    public Collection<Team> getParents()
    {
        return parents.values();
    }

    public void addParent(Team parent)
    {
        this.parents.put(parent.getName(), parent);
    }

    public void removeParent(Team parent)
    {
        this.parents.remove(parent.getName());
    }

    public Collection<Team> getChildren()
    {
        return children.values();
    }

    public void removeChild(Team child)
    {
        this.children.remove(child.getName());
        child.removeParent(this);
    }

    public void addChild(Team child)
    {
        this.children.put(child.getName(), child);
        child.addParent(this);
    }
    
    public List<Contact> getAllContacts()
    {
        List<Contact> ret = new LinkedList<Contact>();
        ret.addAll(this.getContacts());
        for (Team child : this.getChildren())
        {
            ret.addAll(child.getAllContacts());
        }
        return ret;
    }

    public List<Contact> getContacts()
    {
        return contacts;
    }

    public void setContacts(List<Contact> contacts)
    {
        this.contacts = contacts;
    }

    public void addContact(Contact contact)
    {
        this.contacts.add(contact);
        contact.getTeams().add(this);
    }
    
    @Override
    public TeamMO toMO(boolean stub)
    {
        TeamMO mo = new TeamMO();
        super.toMO(mo, stub);
        if (! stub)
        {
            mo.setParents(this.getParents().stream().map(Team::toStubMO).collect(Collectors.toList()));
            mo.setChildren(this.getChildren().stream().map(Team::toStubMO).collect(Collectors.toList()));
            mo.setContacts(this.getContacts().stream().map(Contact::toStubMO).collect(Collectors.toList()));
        }
        return mo;
    }
}

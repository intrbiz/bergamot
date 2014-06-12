package com.intrbiz.bergamot.config.model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyCollection;

@XmlType(name = "team")
@XmlRootElement(name = "team")
public class TeamCfg extends NamedObjectCfg<TeamCfg>
{
    private static final long serialVersionUID = 1L;
    
    private Set<String> teams = new LinkedHashSet<String>();

    public TeamCfg()
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

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = new LinkedList<TemplatedObjectCfg<?>>();
        return r;
    }
}

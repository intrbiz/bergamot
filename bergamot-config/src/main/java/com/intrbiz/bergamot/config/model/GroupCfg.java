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

@XmlType(name = "group")
@XmlRootElement(name = "group")
public class GroupCfg extends NamedObjectCfg<GroupCfg>
{
    private static final long serialVersionUID = 1L;
    
    private Set<String> groups = new LinkedHashSet<String>();
    
    private Set<String> securityDomains = new LinkedHashSet<String>();

    public GroupCfg()
    {
        super();
    }
    
    public GroupCfg(String name, String summary)
    {
        super();
        this.setName(name);
        this.setSummary(summary);
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

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = new LinkedList<TemplatedObjectCfg<?>>();
        return r;
    }
}

package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.intrbiz.Util;
import com.intrbiz.bergamot.compat.config.model.HostgroupCfg;
import com.intrbiz.bergamot.model.state.GroupState;

public class HostGroup extends Group
{
    private Map<String, HostGroup> hostgroups = new TreeMap<String, HostGroup>();
    
    private Map<String, Host> hosts = new TreeMap<String, Host>();
    
    private Map<String, HostGroup> members = new TreeMap<String, HostGroup>();

    public HostGroup()
    {
        super();
    }
    public void configure(HostgroupCfg config)
    {
        this.name = config.resolveHostgroupName();
        this.displayName = Util.coalesceEmpty(config.resolveAlias(), this.name); 
    }

    public Collection<Host> getHosts()
    {
        return this.hosts.values();
    }

    public Host getHost(String name)
    {
        return this.hosts.get(name);
    }

    public boolean containsHost(String name)
    {
        return this.hosts.containsKey(name);
    }

    public int getHostCount()
    {
        return this.hosts.size();
    }

    public void addHost(Host host)
    {
        this.hosts.put(host.getName(), host);
        host.addHostGroup(this);
    }

    //
    
    public Collection<HostGroup> getHostGroups()
    {
        return this.hostgroups.values();
    }

    public void addHostGroup(HostGroup hostGroup)
    {
        this.hostgroups.put(hostGroup.getName(), hostGroup);
    }
    
    public HostGroup getHostGroup(String name)
    {
        return this.hostgroups.get(name);
    }

    public boolean containsHostGroup(String name)
    {
        return this.hostgroups.containsKey(name);
    }

    public int getHostGroupCount()
    {
        return this.hostgroups.size();
    }
    
    //
    
    public Collection<HostGroup> getMembers()
    {
        return this.members.values();
    }

    public void addMember(HostGroup member)
    {
        this.members.put(member.getName(), member);
        member.addHostGroup(this);
    }
    
    public HostGroup getMember(String name)
    {
        return this.members.get(name);
    }

    public boolean containsMember(String name)
    {
        return this.members.containsKey(name);
    }

    public int getMemberCount()
    {
        return this.members.size();
    }
    
    //
    
    @Override
    public GroupState getState()
    {
        return GroupState.compute(this.getHosts(), this.getMembers());
    }
}

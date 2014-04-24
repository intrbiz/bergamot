package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.intrbiz.Util;
import com.intrbiz.bergamot.compat.config.model.HostgroupCfg;
import com.intrbiz.bergamot.model.state.GroupState;

public class HostGroup extends NamedObject
{
    private Map<String, Host> hosts = new TreeMap<String, Host>();

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
        host.addHostgroup(this);
    }
    
    public GroupState getState()
    {
        return GroupState.compute(this.getHosts());
    }
}

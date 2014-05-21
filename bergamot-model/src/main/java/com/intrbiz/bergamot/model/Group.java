package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.GroupCfg;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.GroupMO;
import com.intrbiz.bergamot.model.state.GroupState;
import com.intrbiz.configuration.Configurable;

public class Group extends NamedObject<GroupMO> implements Configurable<GroupCfg>
{
    private GroupCfg config;

    // group hierarchy

    private Map<String, Group> parents = new TreeMap<String, Group>();

    private Map<String, Group> children = new TreeMap<String, Group>();

    // group members

    private Map<UUID, Check<? extends CheckMO>> checks = new TreeMap<UUID, Check<? extends CheckMO>>();

    public Group()
    {
        super();
    }

    @Override
    public void configure(GroupCfg cfg)
    {
        this.config = cfg;
        GroupCfg rcfg = cfg.resolve();
        this.name = rcfg.getName();
        this.summary = Util.coalesceEmpty(rcfg.getSummary(), this.name);
    }

    @Override
    public GroupCfg getConfiguration()
    {
        return this.config;
    }

    public GroupState getState()
    {
        return GroupState.compute(this.getChecks(), this.getChildren(), (g) -> { return g.getState(); });
    }

    public Collection<Group> getParents()
    {
        return parents.values();
    }

    public void addParent(Group parent)
    {
        this.parents.put(parent.getName(), parent);
    }

    public void removeParent(Group parent)
    {
        this.parents.remove(parent.getName());
    }

    public Collection<Group> getChildren()
    {
        return children.values();
    }

    public void removeChild(Group child)
    {
        this.children.remove(child.getName());
        child.removeParent(this);
    }

    public void addChild(Group child)
    {
        this.children.put(child.getName(), child);
        child.addParent(this);
    }

    public Collection<Check<? extends CheckMO>> getChecks()
    {
        return checks.values();
    }

    public void addCheck(Check<? extends CheckMO> check)
    {
        this.checks.put(check.getId(), check);
        check.addGroup(this);
    }

    public void removeCheck(Check<? extends CheckMO> check)
    {
        this.checks.remove(check.getId());
        check.removeGroup(this);
    }
    

    @Override    
    public GroupMO toMO(boolean stub)
    {
        GroupMO mo = new GroupMO();
        super.toMO(mo, stub);
        mo.setState(this.getState().toMO());
        if (! stub)
        {
            mo.setChecks(this.getChecks().stream().map(Check::toStubMO).collect(Collectors.toList()));
            mo.setParents(this.getParents().stream().map(Group::toStubMO).collect(Collectors.toList()));
            mo.setChildren(this.getChildren().stream().map(Group::toStubMO).collect(Collectors.toList()));
        }
        return  mo;
    }
}

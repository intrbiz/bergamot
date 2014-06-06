package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.GroupCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.GroupCfgAdapter;
import com.intrbiz.bergamot.model.message.GroupMO;
import com.intrbiz.bergamot.model.state.GroupState;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "group", since = @SQLVersion({ 1, 0, 0 }))
@SQLUnique(name = "name_unq", columns = { "site_id", "name" })
public class Group extends NamedObject<GroupMO, GroupCfg>
{
    @SQLColumn(index = 1, name = "configuration", type = "TEXT", adapter = GroupCfgAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    protected GroupCfg configuration;
    
    /**
     * The groups this group is a member of
     */
    @SQLColumn(index = 2, name = "group_ids", type = "UUID[]", since = @SQLVersion({ 1, 0, 0 }))
    protected List<UUID> groupIds = new LinkedList<UUID>();

    public Group()
    {
        super();
    }
    
    @Override
    public GroupCfg getConfiguration()
    {
        return configuration;
    }

    @Override
    public void setConfiguration(GroupCfg configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void configure(GroupCfg cfg)
    {
        super.configure(cfg);
        GroupCfg rcfg = cfg.resolve();
        this.name = rcfg.getName();
        this.summary = Util.coalesceEmpty(rcfg.getSummary(), this.name);
        this.description = Util.coalesceEmpty(rcfg.getDescription(), "");
    }

    public GroupState getState()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.computeGroupState(this.getId());
        }
    }

    public List<UUID> getGroupIds()
    {
        return groupIds;
    }

    public void setGroupIds(List<UUID> groupIds)
    {
        this.groupIds = groupIds;
    }

    public List<Group> getGroups()
    {
        List<Group> r = new LinkedList<Group>();
        if (this.getGroupIds() != null)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                for (UUID id : this.getGroupIds())
                {
                    r.add(db.getGroup(id));
                }
            }
        }
        return r;
    }

    public void addParent(Group parent)
    {
        // TODO
    }

    public void removeParent(Group parent)
    {
        // TODO
    }

    public List<Group> getChildren()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getGroupsInGroup(this.getId());
        }
    }

    public void removeChild(Group child)
    {
        // TODO
    }

    public void addChild(Group child)
    {
        // TODO
    }

    public Collection<Check<?,?>> getChecks()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getChecksInGroup(this.getId());
        }
    }

    public void addCheck(Check<?,?> check)
    {
        // TODO
    }

    public void removeCheck(Check<?,?> check)
    {
        // TODO
    }

    @Override
    public GroupMO toMO(boolean stub)
    {
        GroupMO mo = new GroupMO();
        super.toMO(mo, stub);
        mo.setState(this.getState().toMO());
        if (!stub)
        {
            mo.setChecks(this.getChecks().stream().map(Check::toStubMO).collect(Collectors.toList()));
            mo.setGroups(this.getGroups().stream().map(Group::toStubMO).collect(Collectors.toList()));
            mo.setChildren(this.getChildren().stream().map(Group::toStubMO).collect(Collectors.toList()));
        }
        return mo;
    }
}

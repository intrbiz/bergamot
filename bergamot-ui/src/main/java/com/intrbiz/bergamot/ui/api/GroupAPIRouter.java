package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.GroupCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.GroupMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/group")
@RequireValidPrincipal()
public class GroupAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @ListOf(GroupMO.class)
    public List<GroupMO> getGroups(BergamotDB db, @Var("site") Site site)
    {
        return db.listGroups(site.getId()).stream().filter((g) -> permission("read", g)).map((x) -> x.toStubMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/roots")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @ListOf(GroupMO.class)
    public List<GroupMO> getRootGroups(BergamotDB db, @Var("site") Site site)
    {
        return db.getRootGroups(site.getId()).stream().filter((g) -> permission("read", g)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public GroupMO getGroup(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Group group = notNull(db.getGroup(id));
        require(permission("read", group));
        return group.toMO(currentPrincipal());
    }
    
    @Get("/id/:id/children")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(GroupMO.class)
    public List<GroupMO> getGroupChildren(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Group group = notNull(db.getGroup(id));
        require(permission("read", group));
        return group.getChildren().stream().filter((g) -> permission("read", g)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/id/:id/checks")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(CheckMO.class)
    public List<CheckMO> getGroupChecks(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Group group = notNull(db.getGroup(id));
        require(permission("read", group));
        return group.getChecks().stream().filter((c) -> permission("read", c)).map((c) -> {return (CheckMO) c.toMO(currentPrincipal());}).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public GroupMO getGroupByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Group group = notNull(db.getGroupByName(site.getId(), name));
        require(permission("read", group));
        return group.toMO(currentPrincipal());
    }
    
    @Get("/name/:name/children")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(GroupMO.class)
    public List<GroupMO> getGroupChildrenByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Group group = notNull(db.getGroupByName(site.getId(), name));
        require(permission("read", group));
        return group.getChildren().stream().filter((g) -> permission("read", g)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/name/:name/checks")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(CheckMO.class)
    public List<CheckMO> getGroupChecksByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Group group = notNull(db.getGroupByName(site.getId(), name));
        require(permission("read", group));
        return group.getChecks().stream().filter((c) -> permission("read", c)).map((c) -> {return (CheckMO) c.toMO(currentPrincipal());}).collect(Collectors.toList());
    }
    
    @Get("/id/:id/execute-all-checks")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public String executeChecksInGroup(BergamotDB db, @IsaObjectId(session = false) UUID id)
    { 
        Group group = notNull(db.getGroup(id));
        int executed = 0;
        for (Check<?,?> check : group.getChecks())
        {
            if (check instanceof ActiveCheck)
            {
                if (permission("execute", check))
                {
                    action("execute-check", check);
                    executed++;
                }
            }
        }
        return "Ok, executed " + executed + " checks";
    }
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public GroupCfg getGroupConfigByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Group group = notNull(db.getGroupByName(site.getId(), name));
        require(permission("read.config", group));
        return group.getConfiguration();
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public GroupCfg getGroupConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Group group = notNull(db.getGroup(id));
        require(permission("read.config", group));
        return group.getConfiguration();
    }
}

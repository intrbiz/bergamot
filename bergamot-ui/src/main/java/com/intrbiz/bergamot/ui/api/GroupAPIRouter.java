package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.GroupMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Var;


@Prefix("/api/group")
public class GroupAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    public List<GroupMO> getGroups(BergamotDB db, @Var("site") Site site)
    {
        return db.listGroups(site.getId()).stream().map(Group::toStubMO).collect(Collectors.toList());
    }
    
    @Get("/roots")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    public List<GroupMO> getRootGroups(BergamotDB db, @Var("site") Site site)
    {
        return db.getRootGroups(site.getId()).stream().filter((e)->{return e.getGroups().isEmpty();}).map(Group::toMO).collect(Collectors.toList());
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public GroupMO getGroup(BergamotDB db, @AsUUID() UUID id)
    {
        return Util.nullable(db.getGroup(id), Group::toMO);
    }
    
    @Get("/id/:id/children")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<GroupMO> getGroupChildren(BergamotDB db, @AsUUID() UUID id)
    {
        return Util.nullable(db.getGroup(id), (e)->{return e.getChildren().stream().map(Group::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/checks")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<CheckMO> getGroupChecks(BergamotDB db, @AsUUID() UUID id)
    {
        return Util.nullable(db.getGroup(id), (e)->{return e.getChecks().stream().map(Check::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public GroupMO getGroup(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getGroupByName(site.getId(), name), Group::toMO);
    }
    
    @Get("/name/:name/children")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<GroupMO> getGroupChildren(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getGroupByName(site.getId(), name), (e)->{return e.getChildren().stream().map(Group::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/checks")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<CheckMO> getGroupChecks(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getGroupByName(site.getId(), name), (e)->{return e.getChecks().stream().map(Check::toMO).collect(Collectors.toList());});
    }
}

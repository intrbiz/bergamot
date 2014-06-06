package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.GroupMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;


@Prefix("/api/group")
public class GroupAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    public List<GroupMO> getGroups()
    {
        return null; //return this.app().getBergamot().getObjectStore().getGroups().stream().map(Group::toStubMO).collect(Collectors.toList());
    }
    
    @Get("/roots")
    @JSON
    public List<GroupMO> getRootLocations()
    {
        return null; //return this.app().getBergamot().getObjectStore().getGroups().stream().filter((e)->{return e.getGroups().isEmpty();}).map(Group::toMO).collect(Collectors.toList());
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    public GroupMO getGroup(@AsUUID() UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupGroup(id), Group::toMO);
    }
    
    @Get("/id/:id/children")
    @JSON(notFoundIfNull = true)
    public List<GroupMO> getGroupChildren(@AsUUID() UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupGroup(id), (e)->{return e.getChildren().stream().map(Group::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/checks")
    @JSON(notFoundIfNull = true)
    public List<CheckMO> getGroupChecks(@AsUUID() UUID id)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupGroup(id), (e)->{return e.getChecks().stream().map(Check::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    public GroupMO getGroup(String name)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupGroup(name), Group::toMO);
    }
    
    @Get("/name/:name/children")
    @JSON(notFoundIfNull = true)
    public List<GroupMO> getGroupChildren(String name)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupGroup(name), (e)->{return e.getChildren().stream().map(Group::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/checks")
    @JSON(notFoundIfNull = true)
    public List<CheckMO> getGroupChecks(String name)
    {
        return null; //return Util.nullable(this.app().getBergamot().getObjectStore().lookupGroup(name), (e)->{return e.getChecks().stream().map(Check::toMO).collect(Collectors.toList());});
    }
}

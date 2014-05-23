package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.TeamMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;


@Prefix("/api/team")
public class TeamAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    public List<TeamMO> getTeams()
    {
        return this.app().getBergamot().getObjectStore().getTeams().stream().map(Team::toStubMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    public TeamMO getTeam(String name)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupTeam(name), Team::toMO);
    }
    
    @Get("/name/:name/children")
    @JSON(notFoundIfNull = true)
    public List<TeamMO> getTeamChildren(String name)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupTeam(name), (e)->{return e.getChildren().stream().map(Team::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/contacts")
    @JSON(notFoundIfNull = true)
    public List<ContactMO> getTeamContacts(String name)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupTeam(name), (e)->{return e.getContacts().stream().map(Contact::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:name")
    @JSON(notFoundIfNull = true)
    public TeamMO getTeam(@AsUUID UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupTeam(id), Team::toMO);
    }
    
    @Get("/id/:id/children")
    @JSON(notFoundIfNull = true)
    public List<TeamMO> getTeamChildren(@AsUUID UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupTeam(id), (e)->{return e.getChildren().stream().map(Team::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/contacts")
    @JSON(notFoundIfNull = true)
    public List<ContactMO> getTeamContacts(@AsUUID UUID id)
    {
        return Util.nullable(this.app().getBergamot().getObjectStore().lookupTeam(id), (e)->{return e.getContacts().stream().map(Contact::toMO).collect(Collectors.toList());});
    }
}

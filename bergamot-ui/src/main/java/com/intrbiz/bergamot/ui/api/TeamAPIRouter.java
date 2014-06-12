package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.TeamMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.Var;


@Prefix("/api/team")
public class TeamAPIRouter extends Router<BergamotApp>
{
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    public List<TeamMO> getTeams(BergamotDB db, @Var("site") Site site)
    {
        return db.listTeams(site.getId()).stream().map(Team::toStubMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TeamMO getTeam(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getTeamByName(site.getId(), name), Team::toMO);
    }
    
    @Get("/name/:name/children")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<TeamMO> getTeamChildren(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getTeamByName(site.getId(), name), (e)->{return e.getChildren().stream().map(Team::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/contacts")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<ContactMO> getTeamContacts(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getTeamByName(site.getId(), name), (e)->{return e.getContacts().stream().map(Contact::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TeamMO getTeam(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getTeam(id), Team::toMO);
    }
    
    @Get("/id/:id/children")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<TeamMO> getTeamChildren(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getTeam(id), (e)->{return e.getChildren().stream().map(Team::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/contacts")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public List<ContactMO> getTeamContacts(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getTeam(id), (e)->{return e.getContacts().stream().map(Contact::toMO).collect(Collectors.toList());});
    }
}

package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.IgnoreBinding;
import com.intrbiz.bergamot.metadata.IsaObjectId;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.TeamMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListOf;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/team")
@RequireValidPrincipal()
public class TeamAPIRouter extends Router<BergamotApp>
{    
    @Get("/")
    @JSON
    @WithDataAdapter(BergamotDB.class)
    @ListOf(TeamMO.class)
    public List<TeamMO> getTeams(BergamotDB db, @Var("site") Site site)
    {
        return db.listTeams(site.getId()).stream().filter((x) -> permission("read", x)).map((x) -> x.toStubMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TeamMO getTeamByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Team team = notNull(db.getTeamByName(site.getId(), name));
        require(permission("read", team));
        return team.toMO(currentPrincipal());
    }
    
    @Get("/name/:name/children")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(TeamMO.class)
    public List<TeamMO> getTeamChildrenByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Team team = notNull(db.getTeamByName(site.getId(), name));
        require(permission("read", team));
        return team.getChildren().stream().filter((x) -> permission("read", x)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/name/:name/contacts")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(ContactMO.class)
    public List<ContactMO> getTeamContactsByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Team team = notNull(db.getTeamByName(site.getId(), name));
        require(permission("read", team));
        return team.getContacts().stream().filter((x) -> permission("read", x)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    public TeamMO getTeam(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Team team = notNull(db.getTeam(id));
        require(permission("read", team));
        return team.toMO(currentPrincipal());
    }
    
    @Get("/id/:id/children")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(TeamMO.class)
    public List<TeamMO> getTeamChildren(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Team team = notNull(db.getTeam(id));
        require(permission("read", team));
        return team.getChildren().stream().filter((x) -> permission("read", x)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/id/:id/contacts")
    @JSON(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @ListOf(ContactMO.class)
    public List<ContactMO> getTeamContacts(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Team team = notNull(db.getTeam(id));
        require(permission("read", team));
        return team.getContacts().stream().filter((x) -> permission("read", x)).map((x) -> x.toMO(currentPrincipal())).collect(Collectors.toList());
    }
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public TeamCfg getTeamConfigByName(BergamotDB db, @Var("site") Site site, String name)
    {
        Team team = notNull(db.getTeamByName(site.getId(), name));
        require(permission("read.config", team));
        return team.getConfiguration();
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @WithDataAdapter(BergamotDB.class)
    @IgnoreBinding
    public TeamCfg getTeamConfig(BergamotDB db, @IsaObjectId(session = false) UUID id)
    {
        Team team = notNull(db.getTeam(id));
        require(permission("read.config", team));
        return team.getConfiguration();
    }
}

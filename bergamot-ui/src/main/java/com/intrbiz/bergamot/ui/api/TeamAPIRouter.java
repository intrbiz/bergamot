package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.TeamMO;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsBoolean;
import com.intrbiz.metadata.AsUUID;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.JSON;
import com.intrbiz.metadata.ListParam;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;


@Prefix("/api/team")
@RequireValidPrincipal()
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
    
    @Any("/configure")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public TeamMO configureTeam(BergamotDB db, @Var("site") Site site, @Param("configuration") @CheckStringLength(min = 1, max = 128 * 1024, mandatory = true) String configurationXML)
    {
        // parse the config and allocate the id
        TeamCfg config = TeamCfg.fromString(TeamCfg.class, configurationXML);
        config.setId(site.randomObjectId());
        // create the team
        Team team = action("create-team", config);
        return team.toMO();
    }
    
    @Any("/create")
    @JSON()
    @WithDataAdapter(BergamotDB.class)
    public TeamMO createTeam(
            BergamotDB db, 
            @Var("site") Site site, 
            @Param("name") @CheckStringLength(min = 1, max = 80, mandatory = true) String name, 
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("description") @CheckStringLength(min = 1, max = 1000) String description, 
            @Param("template") @AsBoolean(coalesce = CoalesceMode.ON_NULL) Boolean template, 
            @ListParam("extends") @CheckStringLength(min = 1, max = 80, mandatory = true) List<String> inherits, 
            @ListParam("grants") @CheckStringLength(min = 1, max = 50, mandatory = true) List<String> grants, 
            @ListParam("revokes") @CheckStringLength(min = 1, max = 50, mandatory = true) List<String> revokes
    )
    {
        // create the team config
        TeamCfg config = new TeamCfg();
        config.setId(site.randomObjectId());
        config.setName(name);
        config.setSummary(summary);
        config.setDescription(description);
        config.setTemplate(template);
        for (String inherit : inherits)
        {
            config.getInheritedTemplates().add(inherit);
        }
        for (String grant : grants)
        {
            config.getGrantedPermissions().add(grant);
        }
        for (String revoke : revokes)
        {
            config.getRevokedPermissions().add(revoke);
        }
        // create the team
        Team team = action("create-team", config);
        return team.toMO();
    }
}

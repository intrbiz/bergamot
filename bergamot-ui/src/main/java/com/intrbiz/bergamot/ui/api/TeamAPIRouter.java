package com.intrbiz.bergamot.ui.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.error.http.BalsaNotFound;
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
import com.intrbiz.metadata.RequirePermissions;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Var;
import com.intrbiz.metadata.XML;


@Prefix("/api/team")
@RequireValidPrincipal()
public class TeamAPIRouter extends Router<BergamotApp>
{    
    @Get("/")
    @JSON
    @RequirePermissions("api.read.team")
    @WithDataAdapter(BergamotDB.class)
    public List<TeamMO> getTeams(BergamotDB db, @Var("site") Site site)
    {
        return db.listTeams(site.getId()).stream().map(Team::toStubMO).collect(Collectors.toList());
    }
    
    @Get("/name/:name")
    @JSON(notFoundIfNull = true)
    @RequirePermissions("api.read.team")
    @WithDataAdapter(BergamotDB.class)
    public TeamMO getTeam(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getTeamByName(site.getId(), name), Team::toMO);
    }
    
    @Get("/name/:name/children")
    @JSON(notFoundIfNull = true)
    @RequirePermissions("api.read.team")
    @WithDataAdapter(BergamotDB.class)
    public List<TeamMO> getTeamChildren(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getTeamByName(site.getId(), name), (e)->{return e.getChildren().stream().map(Team::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/contacts")
    @JSON(notFoundIfNull = true)
    @RequirePermissions("api.read.team")
    @WithDataAdapter(BergamotDB.class)
    public List<ContactMO> getTeamContacts(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getTeamByName(site.getId(), name), (e)->{return e.getContacts().stream().map(Contact::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id")
    @JSON(notFoundIfNull = true)
    @RequirePermissions("api.read.team")
    @WithDataAdapter(BergamotDB.class)
    public TeamMO getTeam(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getTeam(id), Team::toMO);
    }
    
    @Get("/id/:id/children")
    @JSON(notFoundIfNull = true)
    @RequirePermissions("api.read.team")
    @WithDataAdapter(BergamotDB.class)
    public List<TeamMO> getTeamChildren(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getTeam(id), (e)->{return e.getChildren().stream().map(Team::toMO).collect(Collectors.toList());});
    }
    
    @Get("/id/:id/contacts")
    @JSON(notFoundIfNull = true)
    @RequirePermissions("api.read.team")
    @WithDataAdapter(BergamotDB.class)
    public List<ContactMO> getTeamContacts(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getTeam(id), (e)->{return e.getContacts().stream().map(Contact::toMO).collect(Collectors.toList());});
    }
    
    @Get("/name/:name/config.xml")
    @XML(notFoundIfNull = true)
    @RequirePermissions("api.read.team.config")
    @WithDataAdapter(BergamotDB.class)
    public TeamCfg getTeamConfig(BergamotDB db, @Var("site") Site site, String name)
    {
        return Util.nullable(db.getTeamByName(site.getId(), name), Team::getConfiguration);
    }
    
    @Get("/id/:id/config.xml")
    @XML(notFoundIfNull = true)
    @RequirePermissions("api.read.team.config")
    @WithDataAdapter(BergamotDB.class)
    public TeamCfg getTeamConfig(BergamotDB db, @AsUUID UUID id)
    {
        return Util.nullable(db.getTeam(id), Team::getConfiguration);
    }
    
    @Any("/configure")
    @JSON()
    @RequirePermissions("api.write.team.create")
    @WithDataAdapter(BergamotDB.class)
    public TeamMO configureTeam(
            BergamotDB db, 
            @Var("site") Site site, 
            @Param("configuration") @CheckStringLength(min = 1, max = 128 * 1024, mandatory = true) String configurationXML
    )
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
    @RequirePermissions("api.write.team.create")
    @WithDataAdapter(BergamotDB.class)
    public TeamMO createTeam(
            BergamotDB db, 
            @Var("site") Site site, 
            @Param("name") @CheckStringLength(min = 1, max = 80, mandatory = true) String name, 
            @Param("summary") @CheckStringLength(min = 1, max = 80, mandatory = true) String summary, 
            @Param("description") @CheckStringLength(min = 1, max = 1000) String description, 
            @Param("template") @AsBoolean(coalesce = CoalesceMode.ON_NULL) Boolean template, 
            @ListParam("extends") @CheckStringLength(min = 1, max = 80, mandatory = true) List<String> inherits, 
            @ListParam("grants") @CheckStringLength(min = 1, max = 255, mandatory = true) List<String> grants, 
            @ListParam("revokes") @CheckStringLength(min = 1, max = 255, mandatory = true) List<String> revokes
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
    
    @Get("/id/:id/add-contact/id/:contact_id")
    @JSON()
    @RequirePermissions("api.write.team.add-contact")
    @WithDataAdapter(BergamotDB.class)
    public TeamMO addContactToTeam(BergamotDB db, @AsUUID UUID id, @AsUUID UUID contactId)
    {
        Team team = db.getTeam(id);
        if (team == null) throw new BalsaNotFound("No team with the id: " + id);
        Contact contact = db.getContact(contactId);
        if (contact == null) throw new BalsaNotFound("No contact with the id: " + contactId);
        // add the contact to the team
        team.addContact(contact);
        return team.toMO();
    }
    
    @Get("/name/:name/add-contact/name/:contact_name")
    @JSON()
    @RequirePermissions("api.write.team.add-contact")
    @WithDataAdapter(BergamotDB.class)
    public TeamMO addContactToTeamByName(BergamotDB db, @Var("site") Site site, String name, String contactName)
    {
        Team team = db.getTeamByName(site.getId(), name);
        if (team == null) throw new BalsaNotFound("No team with the name: " + name);
        Contact contact = db.getContactByName(site.getId(), contactName);
        if (contact == null) throw new BalsaNotFound("No contact with the name: " + contactName);
        // add the contact to the team
        team.addContact(contact);
        return team.toMO();
    }
    
    @Get("/id/:id/add-child/id/:child")
    @JSON()
    @RequirePermissions("api.write.team.add-child")
    @WithDataAdapter(BergamotDB.class)
    public TeamMO addChild(BergamotDB db, @AsUUID UUID id, @AsUUID UUID childId)
    {
        Team team = db.getTeam(id);
        if (team == null) throw new BalsaNotFound("No team with the id: " + id);
        Team child = db.getTeam(childId);
        if (child == null) throw new BalsaNotFound("No team with the id: " + childId);
        // add the child to the team
        team.addChild(child);
        return team.toMO();
    }
    
    @Get("/name/:name/add-child/name/:child_name")
    @JSON()
    @RequirePermissions("api.write.team.add-child")
    @WithDataAdapter(BergamotDB.class)
    public TeamMO addChildByName(BergamotDB db, @Var("site") Site site, String name, String childName)
    {
        Team team = db.getTeamByName(site.getId(), name);
        if (team == null) throw new BalsaNotFound("No team with the name: " + name);
        Team child = db.getTeamByName(site.getId(), childName);
        if (child == null) throw new BalsaNotFound("No team with the name: " + childName);
        // add the child to the team
        team.addChild(child);
        return team.toMO();
    }
    
    @Get("/id/:id/remove-contact/id/:contact_id")
    @JSON()
    @RequirePermissions("api.write.team.remove-contact")
    @WithDataAdapter(BergamotDB.class)
    public TeamMO removeContactFromTeam(BergamotDB db, @AsUUID UUID id, @AsUUID UUID contactId)
    {
        Team team = db.getTeam(id);
        if (team == null) throw new BalsaNotFound("No team with the id: " + id);
        Contact contact = db.getContact(contactId);
        if (contact == null) throw new BalsaNotFound("No contact with the id: " + contactId);
        // remove the contact to the team
        team.removeContact(contact);
        return team.toMO();
    }
    
    @Get("/name/:name/remove-contact/name/:contact_name")
    @JSON()
    @RequirePermissions("api.write.team.remove-contact")
    @WithDataAdapter(BergamotDB.class)
    public TeamMO removeContactFromTeamByName(BergamotDB db, @Var("site") Site site, String name, String contactName)
    {
        Team team = db.getTeamByName(site.getId(), name);
        if (team == null) throw new BalsaNotFound("No team with the name: " + name);
        Contact contact = db.getContactByName(site.getId(), contactName);
        if (contact == null) throw new BalsaNotFound("No contact with the name: " + contactName);
        // remove the contact to the team
        team.removeContact(contact);
        return team.toMO();
    }
    
    @Get("/id/:id/remove-child/id/:child")
    @JSON()
    @RequirePermissions("api.write.team.remove-child")
    @WithDataAdapter(BergamotDB.class)
    public TeamMO removeChild(BergamotDB db, @AsUUID UUID id, @AsUUID UUID childId)
    {
        Team team = db.getTeam(id);
        if (team == null) throw new BalsaNotFound("No team with the id: " + id);
        Team child = db.getTeam(childId);
        if (child == null) throw new BalsaNotFound("No team with the id: " + childId);
        // remove the child to the team
        team.removeChild(child);
        return team.toMO();
    }
    
    @Get("/name/:name/remove-child/name/:child_name")
    @JSON()
    @RequirePermissions("api.write.team.remove-child")
    @WithDataAdapter(BergamotDB.class)
    public TeamMO removeChildByName(BergamotDB db, @Var("site") Site site, String name, String childName)
    {
        Team team = db.getTeamByName(site.getId(), name);
        if (team == null) throw new BalsaNotFound("No team with the name: " + name);
        Team child = db.getTeamByName(site.getId(), childName);
        if (child == null) throw new BalsaNotFound("No team with the name: " + childName);
        // remove the child to the team
        team.removeChild(child);
        return team.toMO();
    }
}

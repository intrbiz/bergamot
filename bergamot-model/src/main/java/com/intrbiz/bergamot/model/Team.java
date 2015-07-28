package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.TeamMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A team of people, who can be notified
 */
@SQLTable(schema = BergamotDB.class, name = "team", since = @SQLVersion({ 1, 0, 0 }))
@SQLUnique(name = "name_unq", columns = { "site_id", "name" })
public class Team extends NamedObject<TeamMO, TeamCfg>
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "team_ids", type = "UUID[]", since = @SQLVersion({ 1, 0, 0 }))
    private List<UUID> teamIds = new LinkedList<UUID>();

    @SQLColumn(index = 2, name = "granted_permissions", type = "TEXT[]", since = @SQLVersion({ 1, 0, 0 }))
    private List<String> grantedPermissions = new LinkedList<String>();

    @SQLColumn(index = 3, name = "revoked_permissions", type = "TEXT[]", since = @SQLVersion({ 1, 0, 0 }))
    private List<String> revokedPermissions = new LinkedList<String>();

    public Team()
    {
        super();
    }

    @Override
    public void configure(TeamCfg configuration, TeamCfg resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
        // permissions
        this.grantedPermissions.clear();
        this.grantedPermissions.addAll(resolvedConfiguration.getGrantedPermissions());
        this.revokedPermissions.clear();
        this.revokedPermissions.addAll(resolvedConfiguration.getRevokedPermissions());
    }

    public List<UUID> getTeamIds()
    {
        return this.teamIds;
    }

    public void setTeamIds(List<UUID> teamIds)
    {
        this.teamIds = teamIds;
    }

    public List<Team> getTeams()
    {
        List<Team> r = new LinkedList<Team>();
        try (BergamotDB db = BergamotDB.connect())
        {
            for (UUID id : this.getTeamIds())
            {
                r.add(db.getTeam(id));
            }
        }
        return r;
    }

    public Collection<Team> getChildren()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getTeamsInTeam(this.getId());
        }
    }

    public void removeChild(Team child)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.removeTeamChild(this, child);
        }
    }

    public void addChild(Team child)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.addTeamChild(this, child);
        }
    }

    public List<Contact> getAllContacts()
    {
        List<Contact> ret = new LinkedList<Contact>();
        ret.addAll(this.getContacts());
        for (Team child : this.getChildren())
        {
            ret.addAll(child.getAllContacts());
        }
        return ret;
    }

    public List<Contact> getContacts()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getContactsInTeam(this.getId());
        }
    }

    public void addContact(Contact contact)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.addContactToTeam(this, contact);
        }
    }

    public void removeContact(Contact contact)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.removeContactFromTeam(this, contact);
        }
    }

    public List<String> getGrantedPermissions()
    {
        return grantedPermissions;
    }

    public void setGrantedPermissions(List<String> grantedPermissions)
    {
        this.grantedPermissions = grantedPermissions;
    }

    public List<String> getRevokedPermissions()
    {
        return revokedPermissions;
    }

    public void setRevokedPermissions(List<String> revokedPermissions)
    {
        this.revokedPermissions = revokedPermissions;
    }
    
    public List<AccessControl> getAccessControls()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getAccessControlsForRole(this.getId());
        }
    }

    @Override
    public TeamMO toMO(boolean stub)
    {
        TeamMO mo = new TeamMO();
        super.toMO(mo, stub);
        mo.setGrantedPermissions(this.getGrantedPermissions());
        mo.setRevokedPermissions(this.getRevokedPermissions());
        if (!stub)
        {
            mo.setTeams(this.getTeams().stream().map(Team::toStubMO).collect(Collectors.toList()));
            mo.setChildren(this.getChildren().stream().map(Team::toStubMO).collect(Collectors.toList()));
            mo.setContacts(this.getContacts().stream().map(Contact::toStubMO).collect(Collectors.toList()));
        }
        return mo;
    }
}

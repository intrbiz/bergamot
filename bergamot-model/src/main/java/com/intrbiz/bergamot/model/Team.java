package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
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
@SQLUnique(name = "name_unq", columns = {"site_id", "name"})
public class Team extends NamedObject<TeamMO, TeamCfg>
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "team_ids", type = "UUID[]", since = @SQLVersion({ 1, 0, 0 }))
    private List<UUID> teamIds = new LinkedList<UUID>();

    public Team()
    {
        super();
    }

    @Override
    public void configure(TeamCfg cfg)
    {
        super.configure(cfg);
        TeamCfg rcfg = cfg.resolve();
        this.name = rcfg.getName();
        this.summary = Util.coalesce(rcfg.getSummary(), this.name);
        this.description = Util.coalesceEmpty(rcfg.getDescription(), "");
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
        // TODO
        return new LinkedList<Team>();
    }

    public void removeChild(Team child)
    {
        // TODO
    }

    public void addChild(Team child)
    {
        // TODO
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
        // TODO
    }

    public void removeContact(Contact contact)
    {
        // TODO
    }

    @Override
    public TeamMO toMO(boolean stub)
    {
        TeamMO mo = new TeamMO();
        super.toMO(mo, stub);
        if (!stub)
        {
            mo.setTeams(this.getTeams().stream().map(Team::toStubMO).collect(Collectors.toList()));
            mo.setChildren(this.getChildren().stream().map(Team::toStubMO).collect(Collectors.toList()));
            mo.setContacts(this.getContacts().stream().map(Contact::toStubMO).collect(Collectors.toList()));
        }
        return mo;
    }
}

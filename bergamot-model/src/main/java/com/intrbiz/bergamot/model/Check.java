package com.intrbiz.bergamot.model;

import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.config.model.CheckCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.VirtualCheckMO;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * An something which should be checked
 */
public abstract class Check<T extends CheckMO, C extends CheckCfg<C>> extends NamedObject<T, C> implements Commented
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Is the result of this check suppressed
     */
    @SQLColumn(index = 2, name = "suppressed", since = @SQLVersion({ 1, 0, 0 }))
    protected boolean suppressed = false;

    /**
     * Is this check currently scheduled
     */
    @SQLColumn(index = 3, name = "enabled", since = @SQLVersion({ 1, 0, 0 }))
    protected boolean enabled = true;

    /**
     * Teams to notify
     */
    @SQLColumn(index = 4, name = "team_ids", type = "UUID[]", since = @SQLVersion({ 1, 0, 0 }))
    protected List<UUID> teamIds = new LinkedList<UUID>();

    /**
     * Contacts to notify
     */
    @SQLColumn(index = 5, name = "contact_ids", type = "UUID[]", since = @SQLVersion({ 1, 0, 0 }))
    protected List<UUID> contactIds = new LinkedList<UUID>();

    /**
     * The groups this check is a member of
     */
    @SQLColumn(index = 7, name = "group_ids", type = "UUID[]", since = @SQLVersion({ 1, 0, 0 }))
    protected List<UUID> groupIds = new LinkedList<UUID>();
    
    /**
     * The processing pool to which this check is assigned
     */
    @SQLColumn(index = 8, name = "pool", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    protected int pool = 0;
    
    @SQLColumn(index = 9, name = "external_ref", since = @SQLVersion({ 2, 1, 0 }))
    protected String externalRef;

    public Check()
    {
        super();
    }

    public abstract String getType();

    public boolean isSuppressed()
    {
        return suppressed;
    }

    public void setSuppressed(boolean suppressed)
    {
        this.suppressed = suppressed;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public List<VirtualCheck<?, ?>> getReferencedBy()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getVirtualChecksReferencingCheck(this.getId());
        }
    }

    public CheckState getState()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getCheckState(this.getId());
        }
    }

    public Set<Contact> getAllContacts()
    {
        Set<Contact> ret = new HashSet<Contact>();
        ret.addAll(this.getContacts());
        for (Team team : this.getTeams())
        {
            ret.addAll(team.getAllContacts());
        }
        return ret;
    }

    public List<UUID> getContactIds()
    {
        return contactIds;
    }

    public void setContactIds(List<UUID> contactIds)
    {
        this.contactIds = contactIds;
    }

    public List<Contact> getContacts()
    {
        List<Contact> r = new LinkedList<Contact>();
        if (this.getContactIds() != null)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                for (UUID id : this.getContactIds())
                {
                    r.add(db.getContact(id));
                }
            }
        }
        return r;
    }

    public void addContact(Contact contact)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.addContactToCheck(this, contact);
        }
    }
    
    public void removeContact(Contact contact)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.removeContactFromCheck(this, contact);
        }
    }

    public List<UUID> getTeamIds()
    {
        return teamIds;
    }

    public void setTeamIds(List<UUID> teamIds)
    {
        this.teamIds = teamIds;
    }

    public List<Team> getTeams()
    {
        List<Team> r = new LinkedList<Team>();
        if (this.getTeamIds() != null)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                for (UUID id : this.getTeamIds())
                {
                    r.add(db.getTeam(id));
                }
            }
        }
        return r;
    }

    public void addTeam(Team team)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.addTeamToCheck(this, team);
        }
    }
    
    public void removeTeam(Team team)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.removeTeamFromCheck(this, team);
        }
    }

    public Notifications getNotifications()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getNotifications(this.getId());
        }
    }
    
    public void setNotifications(Notifications notifications)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            notifications.setId(this.getId());
            db.setNotifications(notifications);
        }
    }

    public List<UUID> getGroupIds()
    {
        return groupIds;
    }

    public void setGroupIds(List<UUID> groupIds)
    {
        this.groupIds = groupIds;
    }

    public List<Group> getGroups()
    {
        List<Group> r = new LinkedList<Group>();
        if (this.getGroupIds() != null)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                for (UUID id : this.getGroupIds())
                {
                    r.add(db.getGroup(id));
                }
            }
        }
        return r;
    }

    public void removeGroup(Group group)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.removeCheckFromGroup(group, this);
        }
    }

    public void addGroup(Group group)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.addCheckToGroup(group, this);
        }
    }
    
    /**
     * Get the pool to which this check has been assigned
     * @return
     */
    public int getPool()
    {
        return pool;
    }

    public void setPool(int pool)
    {
        this.pool = pool;
    }

    /**
     * Get the processing pool for this check
     */
    public int computePool()
    {
        return this.getSite().computeProcessingPool(this.getId());
    }
    
    /**
     * Get the current downtimes for this check
     */
    public List<Downtime> getDowntime()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getDowntimesForCheck(this.getId());
        }
    }
    
    /**
     * Check if this check is in downtime at the given time
     */
    public boolean isInDowntime(Calendar now)
    {
       for (Downtime downtime : this.getDowntime())
       {
           if (downtime.isInTimeRange(now))
               return true;
       }
       return false;
    }
    
    /**
     * Check if this check is currently in downtime
     */
    public boolean isInDowntime()
    {
        return this.isInDowntime(Calendar.getInstance());
    }
    
    /**
     * Is this check suppressed or in downtime at the given time
     */
    public boolean isSuppressedOrInDowntime(Calendar now)
    {
        return this.isSuppressed() || this.isInDowntime(now);
    }
    
    /**
     * Is this check suppressed or currently in downtime
     */
    public boolean isSuppressedOrInDowntime()
    {
        return this.isSuppressed() || this.isInDowntime();
    }
    
    /**
     * Get comments against this check
     * @param limit the maximum number of comments to get
     */
    @Override
    public List<Comment> getComments(int limit)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getCommentsForObject(this.getId(), 0, limit);
        }
    }

    /**
     * Get comments against this check
     */
    @Override
    public List<Comment> getComments()
    {
        return this.getComments(5);
    }
    
    public String getExternalRef()
    {
        return externalRef;
    }

    public void setExternalRef(String externalRef)
    {
        this.externalRef = externalRef;
    }
    
    /**
     * Get the security domains this group exists within
     */
    public List<SecurityDomain> getSecurityDomains()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getSecurityDomainsForCheck(this.getId());
        }
    }

    protected void toMO(CheckMO mo, boolean stub)
    {
        super.toMO(mo, stub);
        mo.setEnabled(this.isEnabled());
        mo.setState(this.getState().toMO());
        mo.setSuppressed(this.isSuppressed());
        mo.setInDowntime(this.isInDowntime());
        mo.setPool(this.getPool());
        if (!stub)
        {
            mo.setGroups(this.getGroups().stream().map(Group::toStubMO).collect(Collectors.toList()));
            mo.setContacts(this.getContacts().stream().map(Contact::toStubMO).collect(Collectors.toList()));
            mo.setTeams(this.getTeams().stream().map(Team::toStubMO).collect(Collectors.toList()));
            mo.setReferencedBy(this.getReferencedBy().stream().map((v) -> {return (VirtualCheckMO) v.toStubMO();}).collect(Collectors.toList()));
            mo.setNotifications(this.getNotifications().toMO());
            mo.setDowntime(this.getDowntime().stream().map(Downtime::toStubMO).collect(Collectors.toList()));
            mo.setComments(this.getComments().stream().map(Comment::toStubMO).collect(Collectors.toList()));
        }
    }
    
    @Override
    public void configure(C configuration, C resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
        // configure basic check state
        this.enabled    = resolvedConfiguration.getEnabledBooleanValue();
        this.suppressed = resolvedConfiguration.getSuppressedBooleanValue();
        this.externalRef = resolvedConfiguration.getExternalRef();
    }
}

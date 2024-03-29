package com.intrbiz.bergamot.model;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.CheckCfg;
import com.intrbiz.bergamot.config.model.NoteCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.NoteMO;
import com.intrbiz.bergamot.model.message.VirtualCheckMO;
import com.intrbiz.bergamot.model.report.SLAReport;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * An something which should be checked
 */
public abstract class Check<T extends CheckMO, C extends CheckCfg<C>> extends SecuredObject<T, C> implements Commented
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "pool", since = @SQLVersion({4, 0, 0}))
    protected int pool;
    
    /**
     * Is the result of this check suppressed
     */
    @SQLColumn(index = 2, name = "suppressed", since = @SQLVersion({4, 0, 0}))
    protected boolean suppressed = false;

    /**
     * Is this check currently scheduled
     */
    @SQLColumn(index = 3, name = "enabled", since = @SQLVersion({4, 0, 0}))
    protected boolean enabled = true;

    /**
     * Teams to notify
     */
    @SQLColumn(index = 4, name = "team_ids", type = "UUID[]", since = @SQLVersion({4, 0, 0}))
    protected List<UUID> teamIds = new LinkedList<UUID>();

    /**
     * Contacts to notify
     */
    @SQLColumn(index = 5, name = "contact_ids", type = "UUID[]", since = @SQLVersion({4, 0, 0}))
    protected List<UUID> contactIds = new LinkedList<UUID>();

    /**
     * The groups this check is a member of
     */
    @SQLColumn(index = 7, name = "group_ids", type = "UUID[]", since = @SQLVersion({4, 0, 0}))
    protected List<UUID> groupIds = new LinkedList<UUID>();
    
    /**
     * External systems reference for this check
     */
    @SQLColumn(index = 9, name = "external_ref", since = @SQLVersion({4, 0, 0}))
    protected String externalRef;
    
    /**
     * Optional note for this check
     */
    @SQLColumn(index = 10, name = "note", since = @SQLVersion({4, 0, 0}))
    protected String note;
    
    /**
     * Optional URL to external notes for this check
     */
    @SQLColumn(index = 11, name = "note_url", since = @SQLVersion({4, 0, 0}))
    protected String noteUrl;
   
    /**
     * Optional title for url to external notes for this check
     */
    @SQLColumn(index = 12, name = "note_title", since = @SQLVersion({4, 0, 0}))
    protected String noteTitle;

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

    public String getNoteTitle()
    {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle)
    {
        this.noteTitle = noteTitle;
    }

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        this.note = note;
    }

    public String getNoteUrl()
    {
        return noteUrl;
    }

    public void setNoteUrl(String noteUrl)
    {
        this.noteUrl = noteUrl;
    }
    
    // Pool
    
    public int getPool()
    {
        return this.pool;
    }

    public void setPool(int pool)
    {
        this.pool = pool;
    }
    
    // some basic actions

    /**
     * Suppress this check
     */
    public void suppress()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            this.setSuppressed(true);
            db.setCheck(this);
            db.suppressCheck(this.getId(), true);
        }
    }
    
    /**
     * Unsuppress this check
     */
    public void unsuppress()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            this.setSuppressed(false);
            db.setCheck(this);
            db.suppressCheck(this.getId(), false);
        }
    }
    
    /**
     * Enable this check 
     */
    public void enable()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            this.setEnabled(true);
            db.setCheck(this);
        }
    }
    
    /**
     * Disable this check
     */
    public void disable()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            this.setEnabled(false);
            db.setCheck(this);
        }
    }
    
    /**
     * Get the current alert for this check
     */
    public Alert getCurrentAlert()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getCurrentAlertForCheck(this.id);
        }
    }
    
    /**
     * Get the last alert for this check
     */
    public Alert getLastAlert()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getLastAlertForCheck(this.id);
        }
    }
    
    /**
     * Get the alerts for this check
     */
    public List<Alert> getAlerts()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getAlertsForCheck(this.id);
        }
    }
    
    /**
     * Get the recovered alerts for this check
     * @return
     */
    public List<Alert> getRecoveredAlerts()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getRecoveredAlertsForCheck(this.id);
        }
    }
    
    /**
     * Get the recent alerts for this check
     * @param interval
     * @param limit
     * @return
     */
    public List<Alert> getAllRecentAlerts(String interval, long limit)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getAllRecentAlertsForCheck(this.id, interval, limit);
        }
    }
    
    /**
     * Get all alerts for this check
     */
    public List<Alert> getAllAlerts()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getAllAlertsForCheck(this.id);
        }
    }
    
    /**
     * Get all alerts for this check
     */
    public List<Alert> getAllAlerts(long limit, long offset)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getAllAlertsForCheck(this.id, limit, offset);
        }
    }
    
    public List<SLA> getSLAs()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getSLAsForCheck(this.id);
        }
    }
    
    public List<SLAReport> getSLAReports(boolean statusPage)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.buildSLAReportForCheck(this.id, statusPage);
        }
    }
    
    //

    protected void toMO(CheckMO mo, Contact contact, EnumSet<MOFlag> options)
    {
        super.toMO(mo, contact, options);
        mo.setPool(this.getPool());
        mo.setEnabled(this.isEnabled());
        mo.setState(this.getState().toMO(contact));
        mo.setSuppressed(this.isSuppressed());
        mo.setInDowntime(this.isInDowntime());
        mo.setExternalRef(this.getExternalRef());
        if (options.contains(MOFlag.NOTE) && (! Util.isEmpty(this.getNote()))) 
            mo.setNote(new NoteMO(this.getNoteTitle(), this.getNoteUrl(), this.getNote()));
        if (options.contains(MOFlag.GROUPS)) 
            mo.setGroups(this.getGroups().stream().filter((x) -> contact == null || contact.hasPermission("read", x)).map((x) -> x.toStubMO(contact)).collect(Collectors.toList()));
        if (options.contains(MOFlag.CONTACTS)) 
            mo.setContacts(this.getContacts().stream().filter((x) -> contact == null || contact.hasPermission("read", x)).map((x) -> x.toStubMO(contact)).collect(Collectors.toList()));
        if (options.contains(MOFlag.TEAMS)) 
            mo.setTeams(this.getTeams().stream().filter((x) -> contact == null || contact.hasPermission("read", x)).map((x) -> x.toStubMO(contact)).collect(Collectors.toList()));
        if (options.contains(MOFlag.REFERENCED_BY)) 
            mo.setReferencedBy(this.getReferencedBy().stream().filter((x) -> contact == null || contact.hasPermission("read", x)).map((v) -> {return (VirtualCheckMO) v.toStubMO(contact);}).collect(Collectors.toList()));
        if (options.contains(MOFlag.NOTIFICATIONS)) 
            mo.setNotifications(this.getNotifications().toMO(contact));
        if (options.contains(MOFlag.DOWNTIME)) 
            mo.setDowntime(this.getDowntime().stream().map((x) -> x.toStubMO(contact)).collect(Collectors.toList()));
        if (options.contains(MOFlag.COMMENTS)) 
            mo.setComments(this.getComments().stream().map((x) -> x.toStubMO(contact)).collect(Collectors.toList()));
        if (options.contains(MOFlag.SLA)) 
            mo.setSlas(this.getSLAs().stream().map((x) -> x.toStubMO(contact)).collect(Collectors.toList()));
    }
    
    @Override
    public void configure(C configuration, C resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
        // configure basic check state
        this.enabled    = resolvedConfiguration.getEnabledBooleanValue(this.enabled);
        this.suppressed = resolvedConfiguration.getSuppressedBooleanValue(this.suppressed);
        this.externalRef = resolvedConfiguration.getExternalRef();
        // note
        NoteCfg noteCfg = resolvedConfiguration.getNote();
        if (noteCfg != null)
        {
            this.note = noteCfg.getNote().trim();
            this.noteUrl = noteCfg.getUrl();
            this.noteTitle = noteCfg.getTitle();
        }
    }
}

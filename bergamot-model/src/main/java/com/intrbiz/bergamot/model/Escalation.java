package com.intrbiz.bergamot.model;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.StatusesAdapter;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.EscalationMO;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "escalation", since = @SQLVersion({ 3, 22, 0 }))
public class Escalation extends BergamotObject<EscalationMO> implements Comparable<Escalation>
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "notifications_id", since = @SQLVersion({ 3, 22, 0 }))
    @SQLForeignKey(references = Notifications.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey
    private UUID notificationsId;

    @SQLColumn(index = 2, name = "after", since = @SQLVersion({ 3, 22, 0 }))
    @SQLPrimaryKey
    private long after;
    
    @SQLColumn(index = 3, name = "ignore", type = "TEXT[]", adapter = StatusesAdapter.class, since = @SQLVersion({ 3, 22, 0 }))
    private List<Status> ignore = new LinkedList<Status>();
    
    @SQLColumn(index = 4, name = "timeperiod_id", since = @SQLVersion({ 3, 22, 0 }))
    @SQLForeignKey(references = TimePeriod.class, on = "id", onDelete = Action.RESTRICT, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    private UUID timePeriodId;
    
    /**
     * Teams to notify
     */
    @SQLColumn(index = 5, name = "team_ids", type = "UUID[]", since = @SQLVersion({ 3, 22, 0 }))
    protected List<UUID> teamIds = new LinkedList<UUID>();

    /**
     * Contacts to notify
     */
    @SQLColumn(index = 6, name = "contact_ids", type = "UUID[]", since = @SQLVersion({ 3, 22, 0 }))
    protected List<UUID> contactIds = new LinkedList<UUID>();
    
    /**
     * Should we renotify the original contacts
     */
    @SQLColumn(index = 7, name = "renotify", since = @SQLVersion({ 3, 36, 0 }))
    protected boolean renotify = false;

    public Escalation()
    {
        super();
    }

    public UUID getNotificationsId()
    {
        return notificationsId;
    }

    public void setNotificationsId(UUID notificationsId)
    {
        this.notificationsId = notificationsId;
    }

    public long getAfter()
    {
        return after;
    }

    public void setAfter(long after)
    {
        this.after = after;
    }

    public List<Status> getIgnore()
    {
        return ignore;
    }

    public void setIgnore(List<Status> ignore)
    {
        this.ignore = ignore;
    }

    public UUID getTimePeriodId()
    {
        return timePeriodId;
    }

    public void setTimePeriodId(UUID timePeriodId)
    {
        this.timePeriodId = timePeriodId;
    }

    public List<UUID> getTeamIds()
    {
        return teamIds;
    }

    public void setTeamIds(List<UUID> teamIds)
    {
        this.teamIds = teamIds;
    }

    public List<UUID> getContactIds()
    {
        return contactIds;
    }

    public void setContactIds(List<UUID> contactIds)
    {
        this.contactIds = contactIds;
    }
    
    public boolean isRenotify()
    {
        return renotify;
    }

    public void setRenotify(boolean renotify)
    {
        this.renotify = renotify;
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
    
    public TimePeriod getTimePeriod()
    {
        if (this.getTimePeriodId() == null) return null;
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getTimePeriod(this.getTimePeriodId());
        }
    }
    
    /**
     * Is this escalation active for the given check state and a point in time
     * @param status the status of the check
     * @param time the time
     * @return true if this escalation is active
     */
    public boolean isActiveFor(Status status, Calendar time)
    {
        TimePeriod timePeriod = this.getTimePeriod();
        return  (!this.isStatusIgnored(status)) && (timePeriod == null ? true : timePeriod.isInTimeRange(time));
    }

    public boolean isStatusIgnored(Status status)
    {
        return this.ignore.stream().anyMatch((e) -> e == status);
    }
    
    /**
     * Compute the list of contacts who should be notified
     * @return a list of contact message objects
     */
    public List<ContactMO> getContactsToNotify(Check<?,?> check, Status status, Calendar time)
    {
        final Notifications checkNotifications = check.getNotifications();
        // compute the engines available
        final Set<String> enabledEngines = checkNotifications.getEnginesEnabledAt(NotificationType.ALERT, status, time);
        // compute the contacts to notify
        return this.getAllContacts().stream()
        .filter((contact) -> contact.getNotifications().isEnabledAt(NotificationType.ALERT, status, time))
        .map((contact) -> {
            ContactMO cmo = contact.toMOUnsafe();
            cmo.setEngines(
                    contact.getNotifications().getEnginesEnabledAt(NotificationType.ALERT, status, time).stream()
                    .filter((engine) -> checkNotifications.isAllEnginesEnabled() || enabledEngines.contains(engine))
                    .collect(Collectors.toSet())
            );
            return cmo;
        }).collect(Collectors.toList());
    }

    @Override
    public EscalationMO toMO(Contact contact, EnumSet<com.intrbiz.bergamot.model.BergamotObject.MOFlag> options)
    {
        EscalationMO mo = new EscalationMO();
        mo.setAfter(this.after);
        mo.setRenotify(this.renotify);
        mo.setIgnore(this.ignore.stream().map(Status::toString).collect(Collectors.toSet()));
        TimePeriod timePeriod = this.getTimePeriod();
        if (timePeriod != null)
        {
            if (contact == null || contact.hasPermission("read", timePeriod)) 
                mo.setTimePeriod(timePeriod.toStubMO(contact));
        }
        mo.setContacts(this.getContacts().stream().filter((x) -> contact == null || contact.hasPermission("read", x)).map((x) -> x.toStubMO(contact)).collect(Collectors.toList()));
        mo.setTeams(this.getTeams().stream().filter((x) -> contact == null || contact.hasPermission("read", x)).map((x) -> x.toStubMO(contact)).collect(Collectors.toList()));
        return mo;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (after ^ (after >>> 32));
        result = prime * result + ((notificationsId == null) ? 0 : notificationsId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Escalation other = (Escalation) obj;
        if (after != other.after) return false;
        if (notificationsId == null)
        {
            if (other.notificationsId != null) return false;
        }
        else if (!notificationsId.equals(other.notificationsId)) return false;
        return true;
    }

    @Override
    public int compareTo(Escalation o)
    {
        return Long.compare(o.after, this.after);
    }
}

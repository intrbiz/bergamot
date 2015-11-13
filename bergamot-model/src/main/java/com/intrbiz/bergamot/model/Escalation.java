package com.intrbiz.bergamot.model;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.StatusesAdapter;
import com.intrbiz.bergamot.model.message.EscalationMO;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "escalation", since = @SQLVersion({ 3, 22, 0 }))
public class Escalation extends BergamotObject<EscalationMO>
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "notifications_id", since = @SQLVersion({ 3, 22, 0 }))
    @SQLForeignKey(references = Notifications.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey
    private UUID notificationsId;
    
    @SQLColumn(index = 2, name = "sequence", since = @SQLVersion({ 3, 22, 0 }))
    @SQLPrimaryKey
    private int sequence;

    @SQLColumn(index = 2, name = "after", since = @SQLVersion({ 3, 22, 0 }))
    private long after;
    
    @SQLColumn(index = 7, name = "ignore", type = "TEXT[]", adapter = StatusesAdapter.class, since = @SQLVersion({ 3, 22, 0 }))
    private List<Status> ignore = new LinkedList<Status>();
    
    @SQLColumn(index = 4, name = "timeperiod_id", since = @SQLVersion({ 3, 22, 0 }))
    @SQLForeignKey(references = TimePeriod.class, on = "id", onDelete = Action.RESTRICT, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    private UUID timePeriodId;
    
    /**
     * Teams to notify
     */
    @SQLColumn(index = 4, name = "team_ids", type = "UUID[]", since = @SQLVersion({ 3, 22, 0 }))
    protected List<UUID> teamIds = new LinkedList<UUID>();

    /**
     * Contacts to notify
     */
    @SQLColumn(index = 5, name = "contact_ids", type = "UUID[]", since = @SQLVersion({ 3, 22, 0 }))
    protected List<UUID> contactIds = new LinkedList<UUID>();

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

    public int getSequence()
    {
        return sequence;
    }

    public void setSequence(int sequence)
    {
        this.sequence = sequence;
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

    @Override
    public EscalationMO toMO(Contact contact, EnumSet<com.intrbiz.bergamot.model.BergamotObject.MOFlag> options)
    {
        EscalationMO mo = new EscalationMO();
        mo.setAfter(this.after);
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
}

package com.intrbiz.bergamot.model;

import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.StatusesAdapter;
import com.intrbiz.bergamot.model.message.NotificationsMO;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "notifications", since = @SQLVersion({ 1, 0, 0 }))
public class Notifications extends BergamotObject<NotificationsMO>
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey
    private UUID id;

    @SQLColumn(index = 2, name = "enabled", since = @SQLVersion({ 1, 0, 0 }))
    private boolean enabled = true;

    @SQLColumn(index = 3, name = "timeperiod_id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLForeignKey(references = TimePeriod.class, on = "id", onDelete = Action.RESTRICT, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    private UUID timePeriodId;

    @SQLColumn(index = 4, name = "alerts_enabled", since = @SQLVersion({ 1, 0, 0 }))
    private boolean alertsEnabled = true;

    @SQLColumn(index = 5, name = "recovery_enabled", since = @SQLVersion({ 1, 0, 0 }))
    private boolean recoveryEnabled = true;

    @SQLColumn(index = 6, name = "ignore", type = "TEXT[]", adapter = StatusesAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    private List<Status> ignore = new LinkedList<Status>();

    @SQLColumn(index = 7, name = "all_engines_enabled", since = @SQLVersion({ 1, 0, 0 }))
    private boolean allEnginesEnabled = true;
    
    @SQLColumn(index = 8, name = "acknowledge_enabled", since = @SQLVersion({ 3, 2, 0 }))
    private boolean acknowledgeEnabled = true;

    public Notifications()
    {
        super();
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public List<NotificationEngine> getEngines()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getNotificationEngines(this.getId());
        }
    }

    public void addEngine(NotificationEngine engine)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            engine.setNotificationsId(this.getId());
            db.setNotificationEngine(engine);
        }
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public TimePeriod getTimePeriod()
    {
        if (this.getTimePeriodId() == null) return null;
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getTimePeriod(this.getTimePeriodId());
        }
    }

    public UUID getTimePeriodId()
    {
        return timePeriodId;
    }

    public void setTimePeriodId(UUID timePeriodId)
    {
        this.timePeriodId = timePeriodId;
    }

    public boolean isAlertsEnabled()
    {
        return alertsEnabled;
    }

    public void setAlertsEnabled(boolean alertsEnabled)
    {
        this.alertsEnabled = alertsEnabled;
    }

    public boolean isRecoveryEnabled()
    {
        return recoveryEnabled;
    }

    public void setRecoveryEnabled(boolean recoveryEnabled)
    {
        this.recoveryEnabled = recoveryEnabled;
    }

    public boolean isAcknowledgeEnabled()
    {
        return acknowledgeEnabled;
    }

    public void setAcknowledgeEnabled(boolean acknowledgeEnabled)
    {
        this.acknowledgeEnabled = acknowledgeEnabled;
    }

    public List<Status> getIgnore()
    {
        return ignore;
    }

    public void setIgnore(List<Status> ignore)
    {
        this.ignore = ignore;
    }

    public boolean isAllEnginesEnabled()
    {
        return allEnginesEnabled;
    }

    public void setAllEnginesEnabled(boolean allEnginesEnabled)
    {
        this.allEnginesEnabled = allEnginesEnabled;
    }

    /**
     * Are notifications enabled for the given notification type, the given check statue and a point in time
     * @param type the notification type
     * @param status the status of the check
     * @param time the time
     * @return true if notifications are enabled
     */
    public boolean isEnabledAt(NotificationType type, Status status, Calendar time)
    {
        TimePeriod timePeriod = this.getTimePeriod();
        return this.enabled && 
                this.isNotificationTypeEnabled(type) && 
                (!this.isStatusIgnored(status)) && 
                (timePeriod == null ? true : timePeriod.isInTimeRange(time)) && 
                (this.allEnginesEnabled || this.getEngines().stream().anyMatch((e) -> e.isEnabledAt(type, status, time)));
    }

    public boolean isStatusIgnored(Status status)
    {
        return this.ignore.stream().anyMatch((e) -> e == status);
    }

    public boolean isNotificationTypeEnabled(NotificationType type)
    {
        return (type == NotificationType.ALERT && this.alertsEnabled) || 
                (type == NotificationType.RECOVERY && this.recoveryEnabled) ||
                (type == NotificationType.ACKNOWLEDGE && this.acknowledgeEnabled);
    }

    public Set<String> getEnginesEnabledAt(NotificationType type, Status status, Calendar time)
    {
        return this.getEngines().stream()
                .filter((e) -> e.isEnabledAt(type, status, time))
                .map(NotificationEngine::getEngine)
                .collect(Collectors.toSet());
    }

    public boolean isEngineEnabledAt(NotificationType type, Status status, Calendar time, String engine)
    {
        return this.getEngines().stream()
                .filter((e) -> engine.equals(e.getEngine()))
                .anyMatch((e) -> e.isEnabledAt(type, status, time));
    }
    
    public List<Escalation> getEscalations()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getEscalations(this.getId());
        }
    }
    
    public Escalation evalEscalations(long alertDuration, Status status, Calendar time)
    {
        // process the escalations in descending order
        List<Escalation> escalations = this.getEscalations();
        Collections.sort(escalations);
        for (Escalation escalation : escalations)
        {
            if (alertDuration > escalation.getAfter() && escalation.isActiveFor(status, time))
                return escalation;
        }
        return null;
    }
    
    public void evalEscalations(long alertDuration, Status status, Calendar time, List<Escalation> escalations)
    {
        Escalation escalation = this.evalEscalations(alertDuration, status, time);
        if (escalation != null) escalations.add(escalation);
    }

    @Override
    public NotificationsMO toMO(Contact contact, EnumSet<MOFlag> options)
    {
        NotificationsMO mo = new NotificationsMO();
        mo.setEnabled(this.isEnabled());
        mo.setAlertsEnabled(this.isAlertsEnabled());
        mo.setAllEnginesEnabled(this.isAllEnginesEnabled());
        mo.setIgnore(this.getIgnore().stream().map(Status::toString).collect(Collectors.toSet()));
        mo.setRecoveryEnabled(this.isRecoveryEnabled());
        mo.setAcknowledgeEnabled(this.isAcknowledgeEnabled());
        TimePeriod timePeriod = this.getTimePeriod();
        if (timePeriod != null)
        {
            if (contact == null || contact.hasPermission("read", timePeriod)) mo.setTimePeriod(timePeriod.toStubMO(contact));
        }
        mo.setEngines(this.getEngines().stream().map((x) -> x.toStubMO(contact)).collect(Collectors.toList()));
        mo.setEscalations(this.getEscalations().stream().map((e) -> e.toStubMO(contact)).collect(Collectors.toList()));
        return mo;
    }
}

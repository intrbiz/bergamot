package com.intrbiz.bergamot.model;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.StatusesAdapter;
import com.intrbiz.bergamot.model.message.NotificationEngineMO;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "notification_engine", since = @SQLVersion({ 1, 0, 0 }))
public class NotificationEngine extends BergamotObject<NotificationEngineMO>
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "notifications_id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLForeignKey(references = Notifications.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey
    private UUID notificationsId;

    @SQLColumn(index = 2, name = "engine", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey
    private String engine;

    @SQLColumn(index = 3, name = "enabled", since = @SQLVersion({ 1, 0, 0 }))
    private boolean enabled;

    @SQLColumn(index = 4, name = "timeperiod_id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLForeignKey(references = TimePeriod.class, on = "id", onDelete = Action.RESTRICT, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    private UUID timePeriodId;

    @SQLColumn(index = 5, name = "alerts_enabled", since = @SQLVersion({ 1, 0, 0 }))
    private boolean alertsEnabled = true;

    @SQLColumn(index = 6, name = "recovery_enabled", since = @SQLVersion({ 1, 0, 0 }))
    private boolean recoveryEnabled = true;

    @SQLColumn(index = 7, name = "ignore", type = "TEXT[]", adapter = StatusesAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    private List<Status> ignore = new LinkedList<Status>();
    
    @SQLColumn(index = 8, name = "acknowledge_enabled", since = @SQLVersion({ 3, 2, 0 }))
    private boolean acknowledgeEnabled = true;

    public NotificationEngine()
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

    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
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

    /**
     * Is this notification engine valid for the given time
     */
    public boolean isEnabledAt(NotificationType type, Status status, Calendar time)
    {
        TimePeriod timePeriod = this.getTimePeriod();
        return this.enabled && this.isNotificationTypeEnabled(type) && 
                (!this.isStatusIgnored(status)) && 
                (timePeriod == null ? true : timePeriod.isInTimeRange(time));
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

    @Override
    public NotificationEngineMO toMO(boolean stub)
    {
        NotificationEngineMO mo = new NotificationEngineMO();
        mo.setEnabled(this.isEnabled());
        mo.setAlertsEnabled(this.isAlertsEnabled());
        mo.setEngine(this.getEngine());
        mo.setIgnore(this.getIgnore().stream().map(Status::toString).collect(Collectors.toSet()));
        mo.setRecoveryEnabled(this.isRecoveryEnabled());
        mo.setAcknowledgeEnabled(this.isAcknowledgeEnabled());
        mo.setTimePeriod(Util.nullable(this.getTimePeriod(), TimePeriod::toStubMO));
        return mo;
    }
}

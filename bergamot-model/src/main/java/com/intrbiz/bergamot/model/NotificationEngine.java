package com.intrbiz.bergamot.model;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class NotificationEngine
{
    private String engine;

    private boolean enabled;

    private TimePeriod timePeriod;

    private boolean alertsEnabled = true;

    private boolean recoveryEnabled = true;

    private Set<Status> ignore = new HashSet<Status>();

    public NotificationEngine()
    {
        super();
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
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod)
    {
        this.timePeriod = timePeriod;
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

    public Set<Status> getIgnore()
    {
        return ignore;
    }

    public void setIgnore(Set<Status> ignore)
    {
        this.ignore = ignore;
    }

    /**
     * Is this notification engine valid for the given time
     */
    public boolean isEnabledAt(NotificationType type, Status status, Calendar time)
    {
        return this.enabled && 
               this.isNotificationTypeEnabled(type) &&
               (! this.isStatusIgnored(status)) &&
               (this.timePeriod == null ? true : this.timePeriod.isInTimeRange(time));
    }
    
    private boolean isStatusIgnored(Status status)
    {
        return this.ignore.stream().anyMatch((e) -> {return e == status;});
    }
    
    private boolean isNotificationTypeEnabled(NotificationType type)
    {
        return (type == NotificationType.ALERT && this.alertsEnabled) || 
               (type == NotificationType.RECOVERY && this.recoveryEnabled);
    }
}

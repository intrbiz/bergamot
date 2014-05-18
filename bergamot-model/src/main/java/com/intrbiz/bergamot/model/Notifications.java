package com.intrbiz.bergamot.model;

import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Notifications
{
    private boolean enabled = true;
    
    private TimePeriod timePeriod;

    private List<NotificationEngine> engines = new LinkedList<NotificationEngine>();
    
    private boolean alertsEnabled = true;
    
    private boolean recoveryEnabled = true;
    
    private Set<Status> ignore = new HashSet<Status>();
    
    private boolean allEnginesEnabled = true;

    public Notifications()
    {
        super();
    }

    public List<NotificationEngine> getEngines()
    {
        return engines;
    }

    public void setEngines(List<NotificationEngine> engines)
    {
        this.engines = engines;
    }
    
    public void addEngine(NotificationEngine engine)
    {
        this.engines.add(engine);
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
    
    
    
    public boolean isAllEnginesEnabled()
    {
        return allEnginesEnabled;
    }

    public void setAllEnginesEnabled(boolean allEnginesEnabled)
    {
        this.allEnginesEnabled = allEnginesEnabled;
    }

    public boolean isEnabledAt(NotificationType type, Status status, Calendar time)
    {
        return this.enabled && 
               this.isNotificationTypeEnabled(type) &&
               (! this.isStatusIgnored(status)) &&
               (this.timePeriod == null ? true : this.timePeriod.isInTimeRange(time)) &&
               (this.allEnginesEnabled || this.engines.stream().anyMatch((e) -> {return e.isEnabledAt(type, status, time);}));
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
    
    public Set<String> getEnginesEnabledAt(NotificationType type, Status status, Calendar time)
    {
        return this.engines.stream().filter((e) -> {return e.isEnabledAt(type, status, time);}).map(NotificationEngine::getEngine).collect(Collectors.toSet());
    }
    
    public boolean isEngineEnabledAt(NotificationType type, Status status, Calendar time, String engine)
    {
        return this.engines.stream().filter((e) -> {return engine.equals(e.getEngine());}).anyMatch((e) -> {return e.isEnabledAt(type, status, time);});
    }
}

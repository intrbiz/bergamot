package com.intrbiz.bergamot.model.message;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.notifications")
public class NotificationsMO extends MessageObject
{
    @JsonProperty("enabled")
    private boolean enabled = true;
    
    @JsonProperty("time_period")
    private TimePeriodMO timePeriod;

    @JsonProperty("engines")
    private List<NotificationEngineMO> engines = new LinkedList<NotificationEngineMO>();
    
    @JsonProperty("alerts_enabled")
    private boolean alertsEnabled = true;
    
    @JsonProperty("recovery_enabled")
    private boolean recoveryEnabled = true;
    
    @JsonProperty("ignore")
    private Set<String> ignore = new HashSet<String>();
    
    @JsonProperty("all_engines_enabled")
    private boolean allEnginesEnabled = true;
    
    @JsonProperty("acknowledge_enabled")
    private boolean acknowledgeEnabled = true;
    
    @JsonProperty("escalations")
    private List<EscalationMO> escalations = new LinkedList<EscalationMO>();
    
    public NotificationsMO()
    {
        super();
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public TimePeriodMO getTimePeriod()
    {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriodMO timePeriod)
    {
        this.timePeriod = timePeriod;
    }

    public List<NotificationEngineMO> getEngines()
    {
        return engines;
    }

    public void setEngines(List<NotificationEngineMO> engines)
    {
        this.engines = engines;
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

    public Set<String> getIgnore()
    {
        return ignore;
    }

    public void setIgnore(Set<String> ignore)
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

    public boolean isAcknowledgeEnabled()
    {
        return acknowledgeEnabled;
    }

    public void setAcknowledgeEnabled(boolean acknowledgeEnabled)
    {
        this.acknowledgeEnabled = acknowledgeEnabled;
    }

    public List<EscalationMO> getEscalations()
    {
        return escalations;
    }

    public void setEscalations(List<EscalationMO> escalations)
    {
        this.escalations = escalations;
    }
}

package com.intrbiz.bergamot.model.message;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.notification_engine")
public class NotificationEngineMO extends MessageObject
{
    @JsonProperty("enabled")
    private boolean enabled = true;

    @JsonProperty("time_period")
    private TimePeriodMO timePeriod;

    @JsonProperty("engine")
    private String engine;

    @JsonProperty("alerts_enabled")
    private boolean alertsEnabled = true;

    @JsonProperty("recovery_enabled")
    private boolean recoveryEnabled = true;

    @JsonProperty("ignore")
    private Set<String> ignore = new HashSet<String>();
    
    @JsonProperty("acknowledge_enabled")
    private boolean acknowledgeEnabled = true;

    public NotificationEngineMO()
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

    public String getEngine()
    {
        return engine;
    }

    public void setEngine(String engine)
    {
        this.engine = engine;
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

    public boolean isAcknowledgeEnabled()
    {
        return acknowledgeEnabled;
    }

    public void setAcknowledgeEnabled(boolean acknowledgeEnabled)
    {
        this.acknowledgeEnabled = acknowledgeEnabled;
    }
}

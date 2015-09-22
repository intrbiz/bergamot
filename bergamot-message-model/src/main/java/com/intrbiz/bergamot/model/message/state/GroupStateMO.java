package com.intrbiz.bergamot.model.message.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

@JsonTypeName("bergamot.state.group")
public class GroupStateMO extends MessageObject
{
    @JsonProperty("ok")
    private boolean ok;

    @JsonProperty("status")
    private String status;

    @JsonProperty("pending_count")
    private int pendingCount = 0;

    @JsonProperty("ok_count")
    private int okCount = 0;

    @JsonProperty("warning_count")
    private int warningCount = 0;

    @JsonProperty("critical_count")
    private int criticalCount = 0;

    @JsonProperty("unknown_count")
    private int unknownCount = 0;

    @JsonProperty("timeout_count")
    private int timeoutCount = 0;

    @JsonProperty("error_count")
    private int errorCount = 0;

    @JsonProperty("suppressed_count")
    private int suppressedCount = 0;
    
    @JsonProperty("info_count")
    private int infoCount = 0;
    
    @JsonProperty("action_count")
    private int actionCount = 0;
    
    @JsonProperty("in_downtime_count")
    private int inDowntimeCount = 0;
    
    @JsonProperty("total_checks")
    private int totalchecks = 0;
    
    @JsonProperty("disconnected_count")
    private int disconnectedCount;

    public GroupStateMO()
    {
        super();
    }

    public boolean isOk()
    {
        return ok;
    }

    public void setOk(boolean ok)
    {
        this.ok = ok;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public int getPendingCount()
    {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount)
    {
        this.pendingCount = pendingCount;
    }

    public int getOkCount()
    {
        return okCount;
    }

    public void setOkCount(int okCount)
    {
        this.okCount = okCount;
    }

    public int getWarningCount()
    {
        return warningCount;
    }

    public void setWarningCount(int warningCount)
    {
        this.warningCount = warningCount;
    }

    public int getCriticalCount()
    {
        return criticalCount;
    }

    public void setCriticalCount(int criticalCount)
    {
        this.criticalCount = criticalCount;
    }

    public int getUnknownCount()
    {
        return unknownCount;
    }

    public void setUnknownCount(int unknownCount)
    {
        this.unknownCount = unknownCount;
    }

    public int getTimeoutCount()
    {
        return timeoutCount;
    }

    public void setTimeoutCount(int timeoutCount)
    {
        this.timeoutCount = timeoutCount;
    }

    public int getErrorCount()
    {
        return errorCount;
    }

    public void setErrorCount(int errorCount)
    {
        this.errorCount = errorCount;
    }

    public int getSuppressedCount()
    {
        return suppressedCount;
    }

    public void setSuppressedCount(int suppressedCount)
    {
        this.suppressedCount = suppressedCount;
    }

    public int getInfoCount()
    {
        return infoCount;
    }

    public void setInfoCount(int infoCount)
    {
        this.infoCount = infoCount;
    }

    public int getActionCount()
    {
        return actionCount;
    }

    public void setActionCount(int actionCount)
    {
        this.actionCount = actionCount;
    }

    public int getInDowntimeCount()
    {
        return inDowntimeCount;
    }

    public void setInDowntimeCount(int inDowntimeCount)
    {
        this.inDowntimeCount = inDowntimeCount;
    }

    public int getTotalchecks()
    {
        return totalchecks;
    }

    public void setTotalchecks(int totalchecks)
    {
        this.totalchecks = totalchecks;
    }

    public int getDisconnectedCount()
    {
        return disconnectedCount;
    }

    public void setDisconnectedCount(int disconnectedCount)
    {
        this.disconnectedCount = disconnectedCount;
    }
}

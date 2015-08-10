package com.intrbiz.bergamot.model.state;

import java.util.EnumSet;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.BergamotObject;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.message.state.GroupStateMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLTable(schema = BergamotDB.class, name = "group_state", virtual = true, since = @SQLVersion({ 1, 0, 0 }))
public class GroupState extends BergamotObject<GroupStateMO>
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "group_id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey
    private UUID groupId;

    @SQLColumn(index = 2, name = "ok", since = @SQLVersion({ 1, 0, 0 }))
    private boolean ok = true;

    @SQLColumn(index = 3, name = "status", since = @SQLVersion({ 1, 0, 0 }))
    private Status status = Status.PENDING;

    // counts;

    @SQLColumn(index = 4, name = "pending_count", since = @SQLVersion({ 1, 0, 0 }))
    private int pendingCount = 0;

    @SQLColumn(index = 5, name = "ok_count", since = @SQLVersion({ 1, 0, 0 }))
    private int okCount = 0;

    @SQLColumn(index = 6, name = "warning_count", since = @SQLVersion({ 1, 0, 0 }))
    private int warningCount = 0;

    @SQLColumn(index = 7, name = "critical_count", since = @SQLVersion({ 1, 0, 0 }))
    private int criticalCount = 0;

    @SQLColumn(index = 8, name = "unknown_count", since = @SQLVersion({ 1, 0, 0 }))
    private int unknownCount = 0;

    @SQLColumn(index = 9, name = "timeout_count", since = @SQLVersion({ 1, 0, 0 }))
    private int timeoutCount = 0;

    @SQLColumn(index = 10, name = "error_count", since = @SQLVersion({ 1, 0, 0 }))
    private int errorCount = 0;

    @SQLColumn(index = 11, name = "suppressed_count", since = @SQLVersion({ 1, 0, 0 }))
    private int suppressedCount = 0;
    
    @SQLColumn(index = 12, name = "info_count", since = @SQLVersion({ 2, 4, 0 }))
    private int infoCount = 0;
    
    @SQLColumn(index = 13, name = "action_count", since = @SQLVersion({ 2, 4, 0 }))
    private int actionCount = 0;
    
    @SQLColumn(index = 14, name = "in_downtime_count", since = @SQLVersion({ 2, 6, 0 }))
    private int inDowntimeCount = 0;
    
    @SQLColumn(index = 15, name = "total_checks", since = @SQLVersion({ 2, 7, 0 }))
    private int totalChecks = 0;

    public GroupState()
    {
        super();
    }

    public GroupState(boolean ok, Status status, int okCount, int warningCount, int criticalCount, int unknownCount, int timeoutCount, int errorCount, int infoCount, int actionCount, int inDowntimeCount, int totalChecks)
    {
        super();
        this.ok = ok;
        this.status = status;
        this.okCount = okCount;
        this.warningCount = warningCount;
        this.criticalCount = criticalCount;
        this.unknownCount = unknownCount;
        this.timeoutCount = timeoutCount;
        this.errorCount = errorCount;
        this.infoCount = infoCount;
        this.actionCount = actionCount;
        this.inDowntimeCount = inDowntimeCount;
        this.totalChecks = totalChecks;
    }

    public UUID getGroupId()
    {
        return groupId;
    }

    public void setGroupId(UUID groupId)
    {
        this.groupId = groupId;
    }

    public boolean isOk()
    {
        return ok;
    }

    public void setOk(boolean ok)
    {
        this.ok = ok;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
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

    public int getTotalChecks()
    {
        return totalChecks;
    }

    public void setTotalChecks(int totalChecks)
    {
        this.totalChecks = totalChecks;
    }

    @Override
    public GroupStateMO toMO(Contact contact, EnumSet<MOFlag> options)
    {
        GroupStateMO mo = new GroupStateMO();
        mo.setOk(this.ok);
        mo.setStatus(this.status.toString());
        mo.setPendingCount(this.pendingCount);
        mo.setOkCount(this.okCount);
        mo.setWarningCount(this.warningCount);
        mo.setCriticalCount(this.criticalCount);
        mo.setUnknownCount(this.unknownCount);
        mo.setErrorCount(this.errorCount);
        mo.setTimeoutCount(this.timeoutCount);
        mo.setSuppressedCount(this.suppressedCount);
        mo.setInfoCount(this.infoCount);
        mo.setActionCount(this.actionCount);
        mo.setInDowntimeCount(this.inDowntimeCount);
        mo.setTotalchecks(this.totalChecks);
        return mo;
    }
}

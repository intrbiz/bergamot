package com.intrbiz.bergamot.model.state;

import java.util.Collection;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Status;

public class GroupState
{
    private boolean ok = true;

    private Status status = Status.PENDING;

    // counts;

    private int pendingCount = 0;

    private int okCount = 0;

    private int warningCount = 0;

    private int criticalCount = 0;

    private int unknownCount = 0;

    private int timeoutCount = 0;

    private int internalCount = 0;

    public GroupState()
    {
        super();
    }

    public GroupState(boolean ok, Status status, int okCount, int warningCount, int criticalCount, int unknownCount, int timeoutCount, int internalCount)
    {
        super();
        this.ok = ok;
        this.status = status;
        this.okCount = okCount;
        this.warningCount = warningCount;
        this.criticalCount = criticalCount;
        this.unknownCount = unknownCount;
        this.timeoutCount = timeoutCount;
        this.internalCount = internalCount;
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

    public int getInternalCount()
    {
        return internalCount;
    }

    public void setInternalCount(int internalCount)
    {
        this.internalCount = internalCount;
    }
    
    public static GroupState compute(Collection<? extends Check> checks, Collection<? extends Group> groups)
    {
        GroupState state = new GroupState();
        for (Check check : checks)
        {
            state.ok     = state.ok & check.getState().isOk();
            state.status = Status.worst(state.status, check.getState().getStatus());
            switch (check.getState().getStatus())
            {
                case PENDING:
                    state.pendingCount++;
                    break;
                case OK:
                    state.okCount++;
                    break;
                case WARNING:
                    state.warningCount++;
                    break;
                case CRITICAL:
                    state.criticalCount++;
                    break;
                case UNKNOWN:
                    state.unknownCount++;
                    break;
                case TIMEOUT:
                    state.timeoutCount++;
                    break;
                case INTERNAL:
                    state.internalCount++;
                    break;
            }
        }
        if (groups != null)
        {
            for (Group group : groups)
            {
                GroupState gstate = group.getState();
                //
                state.ok            = state.ok & gstate.isOk();
                state.status        = Status.worst(state.status, gstate.getStatus());
                state.pendingCount  += gstate.pendingCount;
                state.okCount       += gstate.okCount;
                state.warningCount  += gstate.warningCount;
                state.criticalCount += gstate.criticalCount;
                state.timeoutCount  += gstate.timeoutCount;
                state.internalCount += gstate.internalCount;
            }
        }
        return state;
    }
}

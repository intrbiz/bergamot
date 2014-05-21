package com.intrbiz.bergamot.model.state;

import java.util.Collection;
import java.util.function.Function;

import com.intrbiz.bergamot.model.BergamotObject;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.message.state.GroupStateMO;

public class GroupState extends BergamotObject<GroupStateMO>
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

    private int errorCount = 0;

    private int suppressedCount = 0;

    public GroupState()
    {
        super();
    }

    public GroupState(boolean ok, Status status, int okCount, int warningCount, int criticalCount, int unknownCount, int timeoutCount, int errorCount)
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

    public static <T> GroupState compute(Collection<? extends Check<?>> checks, Collection<T> groups, Function<T,GroupState> groupStateAccessor)
    {
        GroupState state = new GroupState();
        for (Check<?> check : checks)
        {
            if (!check.isSuppressed())
            {
                if (check.getState().isHard())
                {
                    state.ok = state.ok && check.getState().isOk();
                    state.status = Status.worst(state.status, check.getState().getStatus());
                }
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
                    case ERROR:
                        state.errorCount++;
                        break;
                }
            }
            else
            {
                state.suppressedCount++;
            }
        }
        if (groups != null)
        {
            for (T group : groups)
            {
                GroupState gstate = groupStateAccessor.apply(group);
                //
                state.ok = state.ok & gstate.isOk();
                state.status = Status.worst(state.status, gstate.getStatus());
                state.pendingCount += gstate.pendingCount;
                state.okCount += gstate.okCount;
                state.warningCount += gstate.warningCount;
                state.criticalCount += gstate.criticalCount;
                state.timeoutCount += gstate.timeoutCount;
                state.errorCount += gstate.errorCount;
                state.suppressedCount += gstate.suppressedCount;
            }
        }
        return state;
    }
    
    @Override
    public GroupStateMO toMO(boolean stub)
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
        return mo;
    }
}

package com.intrbiz.bergamot.model.state;

import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.intrbiz.bergamot.model.BergamotObject;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;

/**
 * The state of a check
 */
public class CheckState extends BergamotObject<CheckStateMO>
{
    private UUID checkId;

    /**
     * Is the check ok?
     */
    private boolean ok = true;

    /**
     * Why is the check ok or not ok?
     */
    private Status status = Status.PENDING;

    /**
     * What was the output of the last check
     */
    private String output = "Pending";

    /**
     * When did the last check happen
     */
    private long lastCheckTime = System.currentTimeMillis();

    /**
     * What was the Id of the last check
     */
    private UUID lastCheckId;

    /**
     * The number of attempts since the last hard state change
     */
    private int attempt = Integer.MAX_VALUE;

    /**
     * Has a hard state transition happened
     */
    private boolean hard = true;

    /**
     * Is the state in transition
     */
    private boolean transitioning = false;

    /**
     * Is the state flapping between ok and not ok, but never reaching a hard state
     */
    private boolean flapping = false;

    /**
     * When was the last hard state change
     */
    private long lastStateChange = System.currentTimeMillis();

    // history

    /**
     * A bitmap of the ok history
     */
    private long okHistory = 0x1L;

    /**
     * Was the last hard state ok?
     */
    private boolean lastHardOk = true;

    /**
     * What was the last hard status?
     */
    private Status lastHardStatus = Status.PENDING;

    /**
     * What was the output of the last hard state
     */
    private String lastHardOutput = "Pending";

    // locking

    private final Lock lock = new ReentrantLock();

    public CheckState()
    {
        super();
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
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

    public String getOutput()
    {
        return output;
    }

    public void setOutput(String output)
    {
        this.output = output;
    }

    public long getLastCheckTime()
    {
        return lastCheckTime;
    }

    public void setLastCheckTime(long lastCheckTime)
    {
        this.lastCheckTime = lastCheckTime;
    }

    public UUID getLastCheckId()
    {
        return lastCheckId;
    }

    public void setLastCheckId(UUID lastCheckId)
    {
        this.lastCheckId = lastCheckId;
    }

    public int getAttempt()
    {
        return attempt;
    }

    public void setAttempt(int attempt)
    {
        this.attempt = attempt;
    }

    public boolean isHard()
    {
        return hard;
    }

    public void setHard(boolean hard)
    {
        this.hard = hard;
    }

    public long getLastStateChange()
    {
        return lastStateChange;
    }

    public void setLastStateChange(long lastStateChange)
    {
        this.lastStateChange = lastStateChange;
    }

    public long getOkHistory()
    {
        return okHistory;
    }

    public void setOkHistory(long okHistory)
    {
        this.okHistory = okHistory;
    }

    public void pushOkHistory(boolean ok)
    {
        this.okHistory = ((this.okHistory << 1) & 0x7FFFFFFFFFFFFFFFL) | (ok ? 1L : 0L);
    }

    public boolean isTransitioning()
    {
        return transitioning;
    }

    public void setTransitioning(boolean transitioning)
    {
        this.transitioning = transitioning;
    }

    public boolean isFlapping()
    {
        return flapping;
    }

    public void setFlapping(boolean flapping)
    {
        this.flapping = flapping;
    }
    
    public boolean isHardOk()
    {
        return this.hard && this.ok;
    }

    public boolean isLastHardOk()
    {
        return lastHardOk;
    }

    public void setLastHardOk(boolean lastHardOk)
    {
        this.lastHardOk = lastHardOk;
    }

    public Status getLastHardStatus()
    {
        return lastHardStatus;
    }

    public void setLastHardStatus(Status lastHardStatus)
    {
        this.lastHardStatus = lastHardStatus;
    }

    public String getLastHardOutput()
    {
        return lastHardOutput;
    }

    public void setLastHardOutput(String lastHardOutput)
    {
        this.lastHardOutput = lastHardOutput;
    }

    public Lock getLock()
    {
        return this.lock;
    }

    @Override
    public CheckStateMO toMO(boolean stub)
    {
        CheckStateMO mo = new CheckStateMO();
        mo.setAttempt(this.getAttempt());
        mo.setFlapping(this.isFlapping());
        mo.setHard(this.isHard());
        mo.setLastCheckId(this.getLastCheckId());
        mo.setLastCheckTime(this.getLastCheckTime());
        mo.setLastStateChange(this.getLastStateChange());
        mo.setOk(this.isOk());
        mo.setOutput(this.getOutput());
        mo.setStatus(this.getStatus().toString());
        mo.setTransitioning(this.isTransitioning());
        mo.setLastHardOk(this.isLastHardOk());
        mo.setLastHardStatus(this.getLastHardStatus().toString());
        mo.setLastHardOutput(this.getLastHardOutput());
        return mo;
    }
}

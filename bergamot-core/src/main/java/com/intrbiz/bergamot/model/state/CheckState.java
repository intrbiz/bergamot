package com.intrbiz.bergamot.model.state;

import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.intrbiz.bergamot.model.Status;

/**
 * The state of a check
 */
public class CheckState
{
    private UUID checkId;

    private boolean ok = true;

    private Status status = Status.PENDING;

    private String output = "Pending";

    private long lastCheckTime;

    private UUID lastCheckId;

    private int attempt = 0;

    private boolean hard = false;

    private boolean transitioning = false;

    private boolean flapping = false;

    private long lastStateChange;

    // history

    private long okHistory = 0x1L;

    // stats

    private double lastRuntime;

    private double averageRuntime;

    private double lastCheckExecutionLatency;

    private double averageCheckExecutionLatency;

    private double lastCheckProcessingLatency;

    private double averageCheckProcessingLatency;

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

    public double getLastRuntime()
    {
        return lastRuntime;
    }

    public void setLastRuntime(double lastRuntime)
    {
        this.lastRuntime = lastRuntime;
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

    public double getLastCheckExecutionLatency()
    {
        return lastCheckExecutionLatency;
    }

    public void setLastCheckExecutionLatency(double lastCheckExecutionLatency)
    {
        this.lastCheckExecutionLatency = lastCheckExecutionLatency;
    }

    public double getAverageCheckExecutionLatency()
    {
        return averageCheckExecutionLatency;
    }

    public void setAverageCheckExecutionLatency(double averageCheckExecutionLatency)
    {
        this.averageCheckExecutionLatency = averageCheckExecutionLatency;
    }

    public double getLastCheckProcessingLatency()
    {
        return lastCheckProcessingLatency;
    }

    public void setLastCheckProcessingLatency(double lastCheckProcessingLatency)
    {
        this.lastCheckProcessingLatency = lastCheckProcessingLatency;
    }

    public double getAverageCheckProcessingLatency()
    {
        return averageCheckProcessingLatency;
    }

    public void setAverageCheckProcessingLatency(double averageCheckProcessingLatency)
    {
        this.averageCheckProcessingLatency = averageCheckProcessingLatency;
    }

    public double getAverageRuntime()
    {
        return averageRuntime;
    }

    public void setAverageRuntime(double averageRuntime)
    {
        this.averageRuntime = averageRuntime;
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

    public Lock getLock()
    {
        return this.lock;
    }
}

package com.intrbiz.bergamot.health.model;

import java.util.Objects;
import java.util.UUID;

public class KnownDaemon implements Comparable<KnownDaemon>
{
    private final UUID instanceId;

    private final UUID runtimeId;

    private final String daemonKind;

    private final String daemonName;

    private final long startedAt;

    private final UUID hostId;

    private final String hostName;

    private volatile long lastHeartbeatAt = System.nanoTime();

    private volatile long lastHeartbeatTime = System.currentTimeMillis();

    private volatile int alertCount = 0;

    private volatile int recoveryCount = 0;

    private volatile int recoveryHeartbeatCount = 0;

    private volatile long lastRecoveryTime = System.currentTimeMillis();

    private volatile long lastAlertTime = -1;

    private volatile boolean alive = true;

    public KnownDaemon(UUID instanceId, UUID runtimeId, String daemonKind, String daemonName, long startedAt, UUID hostId, String hostName)
    {
        Objects.requireNonNull(instanceId);
        this.instanceId = instanceId;
        this.runtimeId = runtimeId;
        this.daemonKind = daemonKind;
        this.daemonName = daemonName;
        this.startedAt = startedAt;
        this.hostId = hostId;
        this.hostName = hostName;
    }

    public long getLastHeartbeatAt()
    {
        return lastHeartbeatAt;
    }

    public void setLastHeartbeatAt(long lastHeartbeatAt)
    {
        this.lastHeartbeatAt = lastHeartbeatAt;
    }

    public UUID getInstanceId()
    {
        return instanceId;
    }

    public UUID getRuntimeId()
    {
        return this.runtimeId;
    }

    public String getDaemonKind()
    {
        return daemonKind;
    }

    public String getDaemonName()
    {
        return daemonName;
    }

    public long getStartedAt()
    {
        return startedAt;
    }

    public UUID getHostId()
    {
        return this.hostId;
    }

    public String getHostName()
    {
        return hostName;
    }

    public long getLastHeartbeatTime()
    {
        return lastHeartbeatTime;
    }

    public void setLastHeartbeatTime(long lastHeartbeatTime)
    {
        this.lastHeartbeatTime = lastHeartbeatTime;
    }

    public int getAlertCount()
    {
        return alertCount;
    }

    public void setAlertCount(int alertCount)
    {
        this.alertCount = alertCount;
    }

    public int incAlertCount()
    {
        this.alertCount++;
        return this.alertCount;
    }

    public long getLastAlertTime()
    {
        return lastAlertTime;
    }

    public void setLastAlertTime(long lastAlertTime)
    {
        this.lastAlertTime = lastAlertTime;
    }

    /**
     * How long ago was the last heartbeat
     * 
     * @return the number of milliseconds since the last heartbeat
     */
    public long getLastHeartbeatAge()
    {
        return (System.nanoTime() - this.lastHeartbeatAt) / 1_000_000L;
    }

    public boolean isLastHeartbeatTooOld()
    {
        // was the heartbeat over two minutes ago (4 join events)
        return this.getLastHeartbeatAge() > 120_000L;
    }

    public boolean isDaemonLongGone()
    {
        // did we last see this daemon over an hour ago
        return this.getLastHeartbeatAge() > 3600_000L;
    }

    public boolean isAlive()
    {
        return alive;
    }

    public void setAlive(boolean alive)
    {
        this.alive = alive;
    }

    public int getRecoveryCount()
    {
        return recoveryCount;
    }

    public void setRecoveryCount(int recoveryCount)
    {
        this.recoveryCount = recoveryCount;
    }

    public int incRecoveryCount()
    {
        this.recoveryCount++;
        return this.recoveryCount;
    }

    public int getRecoveryHeartbeatCount()
    {
        return recoveryHeartbeatCount;
    }

    public void setRecoveryHeartbeatCount(int recoveryHeartbeatCount)
    {
        this.recoveryHeartbeatCount = recoveryHeartbeatCount;
    }

    public int incRecoveryHeartbeatCount()
    {
        this.recoveryHeartbeatCount++;
        return recoveryHeartbeatCount;
    }

    @Override
    public int compareTo(KnownDaemon o)
    {
        return this.instanceId.compareTo(o.instanceId);
    }

    public long getLastRecoveryTime()
    {
        return lastRecoveryTime;
    }

    public void setLastRecoveryTime(long lastRecoveryTime)
    {
        this.lastRecoveryTime = lastRecoveryTime;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        KnownDaemon other = (KnownDaemon) obj;
        if (instanceId == null)
        {
            if (other.instanceId != null) return false;
        }
        else if (!instanceId.equals(other.instanceId)) return false;
        return true;
    }
}
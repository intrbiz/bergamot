package com.intrbiz.bergamot.pinger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.TimerTask;

/**
 * A target host which is being pinged
 */
public class PingTarget
{
    private final String host;

    private volatile InetAddress address;

    private final short id;

    private final long interval;
    
    private final long timeout;

    private short sequence;
    
    private TimerTask sendTask;
    
    private TimerTask lookupTask;

    private PingWindow window;
    
    private OnPingUpdate updateCallback;

    public PingTarget(String host, long interval, long timeout)
    {
        super();
        this.host = host;
        this.interval = interval;
        this.timeout = timeout < ((long)(interval * 0.75)) ? timeout : ((long)(interval * 0.75));
        this.id = (short) new SecureRandom().nextInt();
        this.sequence = 0;
        this.window = new PingWindow((int) (300_000L / interval) ); // 5 minutes of pings
        // lookup the host
        this.lookup();
    }

    public short getSequence()
    {
        return sequence;
    }

    public String getHost()
    {
        return host;
    }

    public InetAddress getAddress()
    {
        return address;
    }

    public short getId()
    {
        return id;
    }

    public long getInterval()
    {
        return interval;
    }
    
    public long getTimeout()
    {
        return timeout;
    }
    
    public OnPingUpdate getUpdateCallback()
    {
        return updateCallback;
    }

    public void setUpdateCallback(OnPingUpdate updateCallback)
    {
        this.updateCallback = updateCallback;
    }

    /**
     * Get a snapshot of the last 5 minutes of pinging
     */
    public PingSnapshot getSnapshot()
    {
        return new PingSnapshot(this.window.getWindow());
    }

    // the scheduled task sending the requests
    
    void setSendTask(TimerTask sendTask)
    {
        this.sendTask = sendTask;
    }
    
    TimerTask getSendTask()
    {
        return this.sendTask;
    }
    
    void setLookupTask(TimerTask lookupTask)
    {
        this.lookupTask = lookupTask;
    }
    
    TimerTask getLookupTask()
    {
        return this.lookupTask;
    }
    
    short nextSequence()
    {
        return this.sequence++;
    }
    
    /**
     * Resolve the host name to IP address again
     */
    public void lookup()
    {
        try
        {
            InetAddress newAddress = InetAddress.getByName(this.host);
            // System.out.println("Resolved: " + this.host + " => " + newAddress);
            this.address = newAddress;
        }
        catch (UnknownHostException e)
        {
            // System.out.println("Could not resolved: " + this.host);
        }
    }
    
    /**
     * Update the statistics with the given RTT
     * @param rtt the Round Trip Time, -1 = timeout
     */
    private void update(long rtt)
    {
        this.window.add(rtt);
        if (this.updateCallback != null)
        {
            this.updateCallback.onUpdate(this, this.getSnapshot());
        }        
    }
    
    /**
     * A reply was received from the target
     * @param rtt the reply Round Trip Time
     */
    void reply(long rtt)
    {
        this.update(rtt);
    }
    
    /**
     * The request timeout was exceeded
     */
    void timeout()
    {
        this.update(-1);
    }
    
    /**
     * Attempted to send a ping but the host was unknown
     */
    void unknownHost()
    {
        this.update(-1);
    }
}

package com.intrbiz.bergamot.pinger;

import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.intrbiz.bergamot.net.raw.RawEngine;
import com.intrbiz.bergamot.net.raw.jna.SockAddrIn;
import com.intrbiz.bergamot.net.raw.model.IPPacket;
import com.intrbiz.bergamot.net.raw.model.IPPayload;
import com.intrbiz.bergamot.net.raw.model.payload.ICMPPacket;

/**
 * Ping lots of targets concurrently measuring the RTT
 */
public class Pinger
{
    private ConcurrentMap<String, PingTarget> targetsByName = new ConcurrentHashMap<String, PingTarget>();
    
    private ConcurrentMap<PingKey, SentMessage> messages = new ConcurrentHashMap<PingKey, SentMessage>();
    
    private RawEngine<SentMessage> engine;
    
    private Timer timer = new Timer();
    
    private SecureRandom random = new SecureRandom();
    
    private final long lookupInterval = TimeUnit.MINUTES.toMillis(5);
    
    public Pinger()
    {
        super();
        this.engine = new RawEngine<SentMessage>(this::recv);
    }
    
    public void start()
    {
        this.engine.start();
    }
    
    private void recv(IPPacket packet)
    {
        if (packet.getPayload() instanceof ICMPPacket)
        {
            ICMPPacket icmp = (ICMPPacket) packet.getPayload();
            // is the ICMP packet
            if (icmp.getType() == 0)
            {
                // lookup the message
                // ignore any messages we cannot correlate
                SentMessage sent = this.messages.remove(new PingKey(packet.getSource().getAddress(), icmp.getId(), icmp.getSequence()));
                if (sent != null)
                {
                    // cancel the timeout
                    sent.cancel();
                    // compute the RTT (in microseconds)
                    long rtt = (System.nanoTime() - sent.sentAt) / 1000L;
                    // update the stats
                    sent.target.reply(rtt);
                }
            }
        }
    }
    
    private void send(PingTarget target)
    {
        // get the target address
        InetAddress address = target.getAddress();
        if (address != null)
        {
            // assemble the ICMP request
            ICMPPacket packet = new ICMPPacket(ICMPPacket.ICMP_TYPE_ECHO, target.getId(), target.nextSequence());
            // per message state
            PingKey key = new PingKey(address.getAddress(), packet.getId(), packet.getSequence());
            SentMessage sent = new SentMessage(target, key);
            this.messages.put(key, sent);
            // send
            this.engine.send(sent, packet, address, 8, this::sent);
        }
        else
        {
            // the address is unknown for some reason
            target.unknownHost();
        }
    }
    
    void sent(SentMessage context, IPPayload payload, InetAddress to, int port, SockAddrIn.ByReference addr)
    {
        // record when we sent the packet
        context.sentAt = System.nanoTime();
        // schedule timeout task
        this.timer.schedule(context, (long) (context.target.getInterval() * 0.75));
    }
    
    // targets
    
    public PingTarget getTarget(String host)
    {
        Objects.requireNonNull(host);
        return this.targetsByName.get(host);
    }
    
    public PingTarget addTarget(String host, long interval, TimeUnit intervalTimeUnit)
    {
        Objects.requireNonNull(intervalTimeUnit);
        return this.addTarget(host, interval, intervalTimeUnit, null);
    }
    
    public PingTarget addTarget(String host, long interval, TimeUnit intervalTimeUnit, long timeout, TimeUnit timeoutTimeUnit)
    {
        Objects.requireNonNull(intervalTimeUnit);
        Objects.requireNonNull(timeoutTimeUnit);
        return this.addTarget(host, interval, intervalTimeUnit, timeout, timeoutTimeUnit, null);
    }
    
    public PingTarget addTarget(String host, long interval)
    {
        return this.addTarget(host, interval, (OnPingUpdate) null);
    }
    
    public PingTarget addTarget(String host, long interval, long timeout)
    {
        return this.addTarget(host, interval, timeout, null);
    }
    
    public PingTarget addTarget(String host, long interval, TimeUnit intervalTimeUnit, OnPingUpdate updateCallback)
    {
        Objects.requireNonNull(intervalTimeUnit);
        return this.addTarget(host, intervalTimeUnit.toMillis(interval), updateCallback);
    }
    
    public PingTarget addTarget(String host, long interval, TimeUnit intervalTimeUnit, long timeout, TimeUnit timeoutTimeUnit, OnPingUpdate updateCallback)
    {
        Objects.requireNonNull(intervalTimeUnit);
        Objects.requireNonNull(timeoutTimeUnit);
        return this.addTarget(host, intervalTimeUnit.toMillis(interval), timeoutTimeUnit.toMillis(timeout), updateCallback);
    }
    
    public PingTarget addTarget(String host, long interval, OnPingUpdate updateCallback)
    {
        return this.addTarget(host, interval, (long) (interval * 0.75), updateCallback);
    }
    
    public PingTarget addTarget(String host, long interval, long timeout, OnPingUpdate updateCallback)
    {
        Objects.requireNonNull(host);
        final PingTarget newTarget = new PingTarget(host, interval, timeout);
        PingTarget oldTarget = this.targetsByName.putIfAbsent(host, newTarget);
        if (oldTarget == null)
        {
            // setup the target
            oldTarget = newTarget;
            // callbacks
            if (updateCallback != null) newTarget.setUpdateCallback(updateCallback);
            // scheduled tasks
            TimerTask sendTask = new TimerTask()
            {
                public void run()
                {
                    Pinger.this.send(newTarget);
                }
            };
            newTarget.setSendTask(sendTask);
            TimerTask lookupTask = new TimerTask()
            {
                public void run()
                {
                    newTarget.lookup();
                }
            };
            newTarget.setLookupTask(lookupTask);
            // schedule
            this.timer.scheduleAtFixedRate(sendTask, this.random.nextInt((int) interval), interval);
            this.timer.scheduleAtFixedRate(lookupTask, this.lookupInterval, this.lookupInterval);
        }
        return oldTarget;
    }
    
    public void removeTarget(String host)
    {
        Objects.requireNonNull(host);
        // remove the target
        PingTarget target = this.targetsByName.remove(host);
        if (target != null)
        {
            // cancel send task
            TimerTask sendTask = target.getSendTask();
            if (sendTask != null) sendTask.cancel();
            // cancel lookup task
            TimerTask lookupTask = target.getLookupTask();
            if (lookupTask != null) lookupTask.cancel();
        }
    }
    
    // internal data structures
    
    private static final class PingKey
    {
        public final byte[] address;
        
        public final short id;
        
        public final short seq;
        
        public PingKey(byte[] address, short id, short seq)
        {
            this.address = address;
            this.id = id;
            this.seq = seq;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            result = prime * result + seq;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            PingKey other = (PingKey) obj;
            if (!Arrays.equals(address, other.address)) return false;
            if (id != other.id) return false;
            if (seq != other.seq) return false;
            return true;
        }
    }
    
    private final class SentMessage extends TimerTask
    {
        public final PingTarget target;
        
        public final PingKey key;
        
        public long sentAt;
        
        public SentMessage(PingTarget target, PingKey key)
        {
            this.target = target;
            this.key = key;
        }
        
        public void run()
        {
            // timeout
            this.target.timeout();
            // clean up
            Pinger.this.messages.remove(this.key);
        }
    }
}

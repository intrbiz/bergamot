package com.intrbiz.bergamot.net.raw;

import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.intrbiz.bergamot.net.raw.jna.SockAddrIn;
import com.intrbiz.bergamot.net.raw.model.IPPacket;
import com.intrbiz.bergamot.net.raw.model.IPPayload;
import com.intrbiz.bergamot.net.raw.model.payload.ICMPPacket;

public class PingTest
{    
    private RawEngine<Target> engine;
    
    private Timer timer;
    
    /* Targets to ping */
    private Map<InetAddress, Target> targets = new HashMap<InetAddress, Target>();
    
    /* Messages on the wire */
    private Map<MessageKey,MessageState> messages = new HashMap<MessageKey, MessageState>();
    
    private final SecureRandom random = new SecureRandom();
    
    public PingTest()
    {
        super();        
        this.engine = new RawEngine<Target>(this::recvPing);
        this.timer = new Timer();
    }
    
    private void sendPing(final Target target)
    {
        ICMPPacket payload = new ICMPPacket(ICMPPacket.ICMP_TYPE_ECHO, target.id, target.seq++);
        this.engine.send(target, payload, target.dest, 8, this::onSent);
    }
    
    private void onSent(Target target, IPPayload payload, InetAddress to, int port, SockAddrIn.ByReference addr)
    {
        ICMPPacket icmp = (ICMPPacket) payload;
        MessageKey key = new MessageKey(to, icmp.getId(), icmp.getSequence());
        MessageState state = new MessageState(target, key, System.nanoTime());
        this.messages.put(key, state);
        this.timer.schedule(state, target.interval / 2);
    }
    
    private void recvPing(IPPacket packet)
    {
        long gotAt = System.nanoTime();
        ICMPPacket icmp = (ICMPPacket) packet.getPayload();
        MessageState state = this.messages.remove(new MessageKey(packet.getSource(), icmp.getId(), icmp.getSequence()));
        if (state != null)
        {
            state.cancel();
            System.out.println("ICMP Reply from " + state.target.dest + " seq " + ((ICMPPacket) packet.getPayload()).getSequence() + " in " + (((double)((gotAt - state.sentAt) / 1000L)) / 1000D) + "ms");
        }
        else
        {
            // System.out.println("Duplicate or bad ICMP Reply from " + packet.getSource() + " seq " + ((ICMPPacket) packet.getPayload()).getSequence());
        }
    }
    
    /**
     * Start the ICMP engine
     */
    public void start()
    {
        this.engine.start();
    }
    
    /**
     * Stop pinging stuff
     */
    public void stop()
    {
        for (Target target : this.targets.values())
        {
            target.cancel();
        }
        this.engine.shutdown();
    }
    
    /**
     * Add something to ping
     */
    public void ping(final InetAddress dest, final int interval)
    {
        final Target target = new Target(dest, (short) this.random.nextInt(), interval);
        this.targets.put(dest, target);
        this.timer.scheduleAtFixedRate(target, this.random.nextInt(interval) , interval);
    }
    
    /**
     * Stop pinging something
     */
    public void cancel(final InetAddress dest)
    {
        TimerTask task = this.targets.get(dest);
        if (task != null) task.cancel();
    }
    
    public static final class MessageKey
    {
        public byte[] address;
        
        public short id;
        
        public short seq;
        
        public MessageKey(InetAddress address, short id, short seq)
        {
            this.address = address.getAddress();
            this.id = id;
            this.seq = seq;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(address);
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
            MessageKey other = (MessageKey) obj;
            if (!Arrays.equals(address, other.address)) return false;
            if (id != other.id) return false;
            if (seq != other.seq) return false;
            return true;
        }
    }
    
    public final class Target extends TimerTask
    {
        public final InetAddress dest;
        
        public final short id;
        
        public short seq = 0;
        
        public long interval;
        
        public Target(InetAddress dest, short id, long interval)
        {
            this.dest = dest;
            this.id = id;
            this.interval = interval;
        }
        
        public void run()
        {
            PingTest.this.sendPing(this);
        }
    }
    
    public static final class MessageState extends TimerTask
    {
        public final Target target;
        
        public final MessageKey key;
        
        public final long sentAt;
        
        public MessageState(Target target, MessageKey key, long sentAt)
        {
            this.target = target;
            this.key = key;
            this.sentAt = sentAt;
        }

        @Override
        public void run()
        {
            System.out.println("Timeout for " + this.target.dest + " seq " + this.key.seq);
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        PingTest test = new PingTest();
        test.start();
        // start pinging things
        test.ping(InetAddress.getByName("google.com"),     10_000);
        test.ping(InetAddress.getByName("bbc.co.uk"),      10_000);
        test.ping(InetAddress.getByName("172.30.12.2"),    10_000);
        test.ping(InetAddress.getByName("172.30.12.1"),    10_000);
        test.ping(InetAddress.getByName("172.30.13.1"),    10_000);
        test.ping(InetAddress.getByName("172.30.13.42"),   10_000);
        test.ping(InetAddress.getByName("172.28.12.254"),  10_000);
        test.ping(InetAddress.getByName("172.28.12.253"),  10_000);
        test.ping(InetAddress.getByName("172.28.12.254"),  10_000);
        test.ping(InetAddress.getByName("10.250.100.122"), 10_000);
        test.ping(InetAddress.getByName("10.250.100.133"), 10_000);
        test.ping(InetAddress.getByName("10.250.100.144"), 10_000);
        test.ping(InetAddress.getByName("10.250.100.1"),   10_000);
        test.ping(InetAddress.getByName("172.30.13.10"),   10_000);
        test.ping(InetAddress.getByName("172.30.13.11"),   10_000);
        test.ping(InetAddress.getByName("172.30.13.12"),   10_000);
        test.ping(InetAddress.getByName("172.30.13.13"),   10_000);
        //
        /*
        int t = 0;
        for (int g = 176; g < 180; g ++)
        {
            for (int i = 122; i < 132; i ++)
            {
                for (int j = 100; j < 200; j++)
                {
                    test.ping(InetAddress.getByName(g + ".35." + i + "." + j),   10_000);
                    t++;
                }
            }
        }
        System.out.println("Added: " + t);
        */
    }
}

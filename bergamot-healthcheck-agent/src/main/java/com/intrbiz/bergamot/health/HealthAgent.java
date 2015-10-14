package com.intrbiz.bergamot.health;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.intrbiz.bergamot.model.message.health.HealthCheckHeartbeat;
import com.intrbiz.bergamot.model.message.health.HealthCheckJoin;
import com.intrbiz.bergamot.model.message.health.HealthCheckMessage;
import com.intrbiz.bergamot.model.message.health.HealthCheckRequestJoin;
import com.intrbiz.bergamot.model.message.health.HealthCheckUnjoin;
import com.intrbiz.bergamot.queue.HealthCheckQueue;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.Producer;
import com.intrbiz.queue.name.NullKey;

public final class HealthAgent
{
    private static final HealthAgent US = new HealthAgent();
    
    public HealthAgent getInstance()
    {
        return US;
    }
    
    private final UUID instanceId = UUID.randomUUID();
    
    private String daemonName;
    
    private final AtomicLong heartbeatSequence = new AtomicLong();
    
    private volatile boolean inited = false;
    
    private long startedAt;
    
    private String hostName;
    
    private HealthCheckQueue queue;
    
    private Producer<HealthCheckMessage> healthcheckProducer;
    
    @SuppressWarnings("unused")
    private Consumer<HealthCheckMessage, NullKey> healthcheckConsumer;
    
    private Timer timer;
    
    private HealthAgent()
    {
        super();
    }
    
    public void init(String daemonName)
    {
        if (! this.inited)
        {
            this.daemonName = daemonName;
            this.startedAt = System.currentTimeMillis();
            // setup our queues
            this.setupQueues();
            // setup shutdown hook
            this.setupShutdownHook();
            // send join
            this.joinHealthCheckCluster();
            // setup timer
            this.setupTimer();
        }
    }
    
    public String getDaemonName()
    {
        return this.daemonName;
    }
    
    public UUID getInstanceId()
    {
        return this.instanceId;
    }
    
    public String getHostName()
    {
        return this.hostName;
    }
    
    public long getStartedAt()
    {
        return this.startedAt;
    }
    
    long nextHeartbeatSequence()
    {
        return this.heartbeatSequence.getAndIncrement();
    }
    
    private void setupQueues()
    {
        this.queue = HealthCheckQueue.open();
        this.healthcheckProducer = this.queue.publishHealthCheckEvents();
        this.healthcheckConsumer = this.queue.consumeHealthCheckControlEvents(this::handleHealthCheckMessage);
    }
    
    private void setupTimer()
    {
        this.timer = new Timer();
        // heartbeat every 5 seconds
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                sendHeartbeat();
            }
        }, 
        5_000L, 5_000L);
    }
    
    private void setupShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run()
            {
                unjoinHealthCheckCluster();
            }
        });
    }
    
    private void joinHealthCheckCluster()
    {
        // send a join message
        this.healthcheckProducer.publish(new HealthCheckJoin(this.instanceId, this.daemonName, this.startedAt, this.hostName));
    }
    
    private void unjoinHealthCheckCluster()
    {
        // send a join message
        this.healthcheckProducer.publish(new HealthCheckUnjoin(this.instanceId, this.daemonName));
    }
    
    private void sendHeartbeat()
    {
        this.healthcheckProducer.publish(new HealthCheckHeartbeat(this.instanceId, System.currentTimeMillis(), this.nextHeartbeatSequence()));
    }
    
    private void handleHealthCheckMessage(HealthCheckMessage message)
    {
        if (message instanceof HealthCheckRequestJoin)
        {
            this.joinHealthCheckCluster();
        }
    }
}

package com.intrbiz.bergamot.health;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.health.HealthCheckJoin;
import com.intrbiz.bergamot.model.message.health.HealthCheckMessage;
import com.intrbiz.bergamot.model.message.health.HealthCheckUnjoin;
import com.intrbiz.bergamot.queue.HealthCheckQueue;
import com.intrbiz.gerald.polyakov.Node;
import com.intrbiz.queue.Producer;

public final class HealthAgent
{    
    private static final HealthAgent US = new HealthAgent();
    
    public static HealthAgent getInstance()
    {
        return US;
    }
    
    private UUID instanceId = null;
    
    private final UUID runtimeId = UUID.randomUUID();
    
    private String daemonName;
    
    private volatile boolean inited = false;
    
    private long startedAt;
    
    private UUID hostId;
    
    private String hostName;
    
    private String daemonKind;
    
    private HealthCheckQueue queue;
    
    private Producer<HealthCheckMessage> healthcheckProducer;
    
    private Timer timer;
    
    private HealthAgent()
    {
        super();
    }
    
    public void init(UUID instanceId, String daemonKind, String daemonName, UUID hostId, String hostName)
    {
        synchronized (this)
        {
            if (! this.inited)
            {
                this.inited = true;
                this.instanceId = instanceId;
                this.daemonKind = daemonKind;
                this.daemonName = daemonName;
                this.hostId = hostId;
                this.hostName = hostName;
                this.startedAt = System.currentTimeMillis();
                // setup our queues
                this.setupQueues();
                // setup shutdown hook
                this.setupShutdownHook();
                // setup timer
                this.setupTimer();
            }
        }
    }
    
    public void init(String daemonKind, String daemonName)
    {
        Node node = Node.service(daemonName);
        this.init(computeInstanceId(), daemonKind, daemonName, node.getHostId(), node.getHostName());
    }
    
    public void init()
    {
        Node node = Node.service();
        this.init(computeInstanceId(), null, node.getServiceName(), node.getHostId(), node.getHostName());
    }
    
    public String getDaemonKind()
    {
        return this.daemonKind;
    }
    
    public String getDaemonName()
    {
        return this.daemonName;
    }
    
    public UUID getInstanceId()
    {
        return this.instanceId;
    }
    
    public UUID getRuntimeId()
    {
        return this.runtimeId;
    }
    
    public String getHostName()
    {
        return this.hostName;
    }
    
    public long getStartedAt()
    {
        return this.startedAt;
    }
    
    private void setupQueues()
    {
        this.queue = HealthCheckQueue.open();
        this.healthcheckProducer = this.queue.publishHealthCheckEvents();
    }
    
    private void setupTimer()
    {
        this.timer = new Timer();
        // heartbeat every 5 seconds
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                try
                {
                    sendHeartbeat();
                }
                catch (Exception e)
                {
                    // ignore
                }
            }
        }, 
        30_000L, 30_000L);
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
    
    private void unjoinHealthCheckCluster()
    {
        // send a join message
        this.healthcheckProducer.publish(new HealthCheckUnjoin(this.instanceId, this.daemonKind, this.daemonName));
    }
    
    private void sendHeartbeat()
    {
        this.healthcheckProducer.publish(new HealthCheckJoin(this.instanceId, this.runtimeId, this.daemonKind, this.daemonName, this.startedAt, this.hostId, this.hostName));
    }
    
    public static final UUID computeInstanceId()
    {
        // Attempt to find a configured specific instance id
        String instanceIdStr = System.getProperty("bergamot.instanceid", System.getenv("BERGAMOT_INSTANCEID"));
        if (instanceIdStr != null && instanceIdStr.length() > 0)
        {
            try
            {
                return UUID.fromString(instanceIdStr);
            }
            catch (IllegalArgumentException e)
            {
            }
        }
        Logger.getLogger(HealthAgent.class).warn("Failed to get service instance id, defaulting to a randomly allocated id, please set the environment variable \"BERGAMOT_INSTANCEID\" or the system property \"bergamot.instanceid\"");
        return UUID.randomUUID();
    }
}

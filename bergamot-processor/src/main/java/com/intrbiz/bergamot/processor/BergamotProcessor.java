package com.intrbiz.bergamot.processor;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.agent.AgentRegistrationService;
import com.intrbiz.bergamot.cluster.broker.AgentEventQueue;
import com.intrbiz.bergamot.cluster.broker.SiteEventTopic;
import com.intrbiz.bergamot.cluster.broker.SiteNotificationTopic;
import com.intrbiz.bergamot.cluster.broker.SiteUpdateTopic;
import com.intrbiz.bergamot.cluster.coordinator.NotifierClusterCoordinator;
import com.intrbiz.bergamot.cluster.coordinator.ProcessingPoolClusterCoordinator;
import com.intrbiz.bergamot.cluster.coordinator.WorkerClusterCoordinator;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyClusterLookup;
import com.intrbiz.bergamot.cluster.queue.ProcessingPoolConsumer;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.result.DefaultResultProcessor;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.bergamot.scheduler.WheelScheduler;
import com.intrbiz.lamplighter.reading.DefaultReadingProcessor;
import com.intrbiz.lamplighter.reading.ReadingProcessor;

public class BergamotProcessor
{
    private final HazelcastInstance hazelcast;
    
    private final SiteEventTopic siteEventTopic;
    
    private final SiteNotificationTopic notificationTopic;
    
    private final SiteUpdateTopic updateTopic;
    
    private final AgentKeyClusterLookup agentKeyLookup;
    
    private final AgentEventQueue agentEventQueue;
    
    private final WorkerClusterCoordinator workerCoordinator;
    
    private final ProcessingPoolClusterCoordinator processingPoolCoordinator;
    
    private final NotifierClusterCoordinator notifierCoordinator;
    
    private final UUID id;
    
    private Scheduler scheduler;
    
    private ResultProcessor resultProcessor;
    
    private ReadingProcessor readingProcessor;
    
    private AgentRegistrationService agentRegistrationService;

    public BergamotProcessor(HazelcastInstance hazelcast)
    {
        super();
        this.hazelcast = hazelcast;
        // brokers
        this.siteEventTopic = new SiteEventTopic(this.hazelcast);
        this.notificationTopic = new SiteNotificationTopic(this.hazelcast);
        this.updateTopic = new SiteUpdateTopic(this.hazelcast);
        // queues
        this.agentEventQueue = new AgentEventQueue(this.hazelcast);
        // lookups
        this.agentKeyLookup = new AgentKeyClusterLookup(this.hazelcast);
        // coordinators
        this.workerCoordinator = new WorkerClusterCoordinator(this.hazelcast);
        this.notifierCoordinator = new NotifierClusterCoordinator(this.hazelcast);
        this.processingPoolCoordinator = new ProcessingPoolClusterCoordinator(this.hazelcast, this.siteEventTopic);
        this.id = this.processingPoolCoordinator.getId();
    }
    
    public void start() throws Exception
    {
        // start our coordinators
        this.notifierCoordinator.start();
        // create our agent registration service
        this.agentRegistrationService = new AgentRegistrationService(this.agentEventQueue, this.processingPoolCoordinator, this.notifierCoordinator);
        // create and start our scheduler
        this.scheduler = new WheelScheduler(this.id, this.workerCoordinator, this.processingPoolCoordinator.createProcessingPoolProducer());
        // setup the procesing pools
        this.processingPoolCoordinator.listen(this.scheduler);
        ProcessingPoolConsumer consumer = this.processingPoolCoordinator.createConsumer();
        this.resultProcessor = new DefaultResultProcessor(this.id, consumer, this.processingPoolCoordinator.createSchedulerActionProducer(), this.notifierCoordinator, this.notificationTopic, this.updateTopic);
        this.readingProcessor = new DefaultReadingProcessor(this.id, consumer);
        // go go go
        this.processingPoolCoordinator.start();
        this.agentRegistrationService.start();
        this.resultProcessor.start();
        this.readingProcessor.start();
        this.scheduler.start();
    }
    
    public void stop()
    {
        // shutdown the processing pool
        this.scheduler.stop();
        this.resultProcessor.stop();
        this.readingProcessor.stop();
        // stop our coordinators
        this.processingPoolCoordinator.stop();
        this.agentRegistrationService.stop();
        this.notifierCoordinator.stop();
        this.workerCoordinator.stop();
    }
    
    public void initAllSites()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            for (Site site : db.listSites())
            {
                if (! site.isDisabled())
                {
                    this.processingPoolCoordinator.registerSite(site);
                }
            }
        }
    }

    public SiteEventTopic getSiteEventTopic()
    {
        return siteEventTopic;
    }

    public SiteNotificationTopic getNotificationTopic()
    {
        return notificationTopic;
    }

    public SiteUpdateTopic getUpdateTopic()
    {
        return updateTopic;
    }

    public AgentEventQueue getAgentEventQueue()
    {
        return this.agentEventQueue;
    }

    public UUID getId()
    {
        return this.id;
    }

    public Scheduler getScheduler()
    {
        return this.scheduler;
    }

    public ResultProcessor getResultProcessor()
    {
        return this.resultProcessor;
    }

    public ReadingProcessor getReadingProcessor()
    {
        return this.readingProcessor;
    }

    public AgentKeyClusterLookup getAgentKeyLookup()
    {
        return this.agentKeyLookup;
    }

    public HazelcastInstance getHazelcastInstance()
    {
        return hazelcast;
    }

    public WorkerClusterCoordinator getWorkerCoordinator()
    {
        return workerCoordinator;
    }

    public ProcessingPoolClusterCoordinator getProcessingPoolCoordinator()
    {
        return processingPoolCoordinator;
    }
    
    public NotifierClusterCoordinator getNotifierCoordinator()
    {
        return notifierCoordinator;
    }

    public int getMemberCount()
    {
        return this.processingPoolCoordinator.getMemberCount();
    }
    
    public int getProcessPoolCount()
    {
        return this.processingPoolCoordinator.getProcessPoolCount();
    }
}

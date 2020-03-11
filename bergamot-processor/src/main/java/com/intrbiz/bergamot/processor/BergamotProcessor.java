package com.intrbiz.bergamot.processor;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.broker.SiteEventBroker;
import com.intrbiz.bergamot.cluster.broker.SiteNotificationBroker;
import com.intrbiz.bergamot.cluster.broker.SiteUpdateBroker;
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
    
    private final SiteEventBroker siteEventBroker;
    
    private final SiteNotificationBroker notificationBroker;
    
    private final SiteUpdateBroker updateBroker;
    
    private final AgentKeyClusterLookup agentKeyLookup;
    
    private final WorkerClusterCoordinator workerCoordinator;
    
    private final ProcessingPoolClusterCoordinator processingPoolCoordinator;
    
    private final NotifierClusterCoordinator notifierCoordinator;
    
    private final UUID id;
    
    private Scheduler scheduler;
    
    private ResultProcessor resultProcessor;
    
    private ReadingProcessor readingProcessor;

    public BergamotProcessor(HazelcastInstance hazelcast)
    {
        super();
        this.hazelcast = hazelcast;
        // brokers
        this.siteEventBroker = new SiteEventBroker(this.hazelcast);
        this.notificationBroker = new SiteNotificationBroker(this.hazelcast);
        this.updateBroker = new SiteUpdateBroker(this.hazelcast);
        // lookups
        this.agentKeyLookup = new AgentKeyClusterLookup(this.hazelcast);
        // coordinators
        this.workerCoordinator = new WorkerClusterCoordinator(this.hazelcast);
        this.notifierCoordinator = new NotifierClusterCoordinator(this.hazelcast);
        this.processingPoolCoordinator = new ProcessingPoolClusterCoordinator(this.hazelcast, this.siteEventBroker);
        this.id = this.processingPoolCoordinator.getId();
    }
    
    public void start() throws Exception
    {
        // start our coordinators
        this.notifierCoordinator.start();
        this.workerCoordinator.stop();
        // create and start our scheduler
        this.scheduler = new WheelScheduler(this.id, this.workerCoordinator, this.processingPoolCoordinator.createProcessingPoolProducer());
        // create and start our result processor
        ProcessingPoolConsumer consumer = this.processingPoolCoordinator.startProcessingPool(this.scheduler);
        this.resultProcessor = new DefaultResultProcessor(this.id, consumer, this.processingPoolCoordinator.createSchedulerActionProducer(), this.notifierCoordinator, this.notificationBroker, this.updateBroker);
        this.readingProcessor = new DefaultReadingProcessor(this.id, consumer);
        // start
        this.resultProcessor.start();
        this.readingProcessor.start();
        this.scheduler.start();
    }
    
    public void stop()
    {
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

    public SiteEventBroker getSiteEventBroker()
    {
        return siteEventBroker;
    }

    public SiteNotificationBroker getNotificationBroker()
    {
        return notificationBroker;
    }

    public SiteUpdateBroker getUpdateBroker()
    {
        return updateBroker;
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

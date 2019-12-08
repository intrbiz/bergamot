package com.intrbiz.bergamot.processor;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.broker.SiteEventBroker;
import com.intrbiz.bergamot.cluster.coordinator.ProcessingPoolClusterCoordinator;
import com.intrbiz.bergamot.cluster.coordinator.WorkerClusterCoordinator;
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
    private final HazelcastInstance hazelcastInstance;
    
    private final SiteEventBroker siteEventBroker;
    
    private final WorkerClusterCoordinator workerCoordinator;
    
    private final ProcessingPoolClusterCoordinator processingPoolCoordinator;
    
    private final UUID id;
    
    private Scheduler scheduler;
    
    private ResultProcessor resultProcessor;
    
    private ReadingProcessor readingProcessor;

    public BergamotProcessor(HazelcastInstance hazelcastInstance, SiteEventBroker siteEventBroker)
    {
        super();
        this.hazelcastInstance = hazelcastInstance;
        this.siteEventBroker = siteEventBroker;
        // coordinators
        this.workerCoordinator = new WorkerClusterCoordinator(this.hazelcastInstance);
        this.processingPoolCoordinator = new ProcessingPoolClusterCoordinator(this.hazelcastInstance, this.siteEventBroker);
        this.id = this.processingPoolCoordinator.getId();
    }
    
    public void start() throws Exception
    { 
        // create and start our scheduler
        this.scheduler = new WheelScheduler(this.id, this.workerCoordinator.createCheckProducer(), this.processingPoolCoordinator.createProcessingPoolProducer());
        // create and start our result processor
        ProcessingPoolConsumer consumer = this.processingPoolCoordinator.startProcessingPool(this.scheduler);
        this.resultProcessor = new DefaultResultProcessor(this.id, consumer);
        this.readingProcessor = new DefaultReadingProcessor(this.id, consumer);
        // start
        this.resultProcessor.start();
        this.readingProcessor.start();
        this.scheduler.start();
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

    public HazelcastInstance getHazelcastInstance()
    {
        return hazelcastInstance;
    }

    public WorkerClusterCoordinator getWorkerCoordinator()
    {
        return workerCoordinator;
    }

    public ProcessingPoolClusterCoordinator getProcessingPoolCoordinator()
    {
        return processingPoolCoordinator;
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

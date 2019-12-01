package com.intrbiz.bergamot.processor;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.coordinator.ProcessingPoolCoordinator;
import com.intrbiz.bergamot.cluster.coordinator.WorkerCoordinator;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.result.DefaultResultProcessor;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.bergamot.scheduler.WheelScheduler;

public class BergamotProcessor
{
    private final HazelcastInstance hazelcastInstance;
    
    private final WorkerCoordinator workerCoordinator;
    
    private final ProcessingPoolCoordinator processingPoolCoordinator;
    
    private final UUID id;
    
    private Scheduler scheduler;
    
    private ResultProcessor resultProcessor;

    public BergamotProcessor(HazelcastInstance hazelcastInstance)
    {
        super();
        this.hazelcastInstance = hazelcastInstance;
        // coordinators
        this.workerCoordinator = new WorkerCoordinator(this.hazelcastInstance);
        this.processingPoolCoordinator = new ProcessingPoolCoordinator(this.hazelcastInstance);
        this.id = this.processingPoolCoordinator.getId();
    }
    
    public void start() throws Exception
    { 
        // create and start our scheduler
        this.scheduler = new WheelScheduler(this.id, this.workerCoordinator.createCheckProducer(), this.processingPoolCoordinator.createProcessingPoolProducer());
        // create and start our result processor
        this.resultProcessor = new DefaultResultProcessor(this.id, this.processingPoolCoordinator.startProcessingPool(this.scheduler));
        // start
        this.resultProcessor.start();
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

    public HazelcastInstance getHazelcastInstance()
    {
        return hazelcastInstance;
    }

    public WorkerCoordinator getWorkerCoordinator()
    {
        return workerCoordinator;
    }

    public ProcessingPoolCoordinator getProcessingPoolCoordinator()
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

package com.intrbiz.bergamot.cluster;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.hazelcast.core.Member;
import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.coordinator.WorkerSchedulerCoordinator;
import com.intrbiz.bergamot.cluster.listener.ProcessingPoolListener;
import com.intrbiz.bergamot.result.DefaultResultProcessor;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.bergamot.scheduler.WheelScheduler;

/**
 * A processing pool responsible for:
 * - scheduling checks
 * - result processing
 * - reading processing
 * 
 */
public class ProcessingPool
{
    private static final Logger logger = Logger.getLogger(ProcessingPool.class);
    
    private final UUID id;
    
    private final String memberName;
    
    private final WorkerSchedulerCoordinator workerCoordinator;

    private ResultProcessor resultProcessor;
    
    private Scheduler scheduler;
 
    public ProcessingPool(WorkerSchedulerCoordinator workerCoordinator, Member localMember)
    {
        super();
        this.id = UUID.fromString(localMember.getUuid());
        this.memberName = Util.coalesceEmpty(localMember.getStringAttribute("bergamot.name"), localMember.getAddress().getHost());
        this.workerCoordinator = workerCoordinator;
    }
 
    public UUID getId()
    {
        return this.id;
    }
    
    public void start() throws Exception
    {
        logger.info("Processing pool " + id + " starting");
        // register ourselves
        this.workerCoordinator.registerProcessingPool(this.id, this.memberName);
        // start the result processor
        this.resultProcessor = new DefaultResultProcessor(this.id, this.workerCoordinator.createResultConsumer(this.id));
        this.resultProcessor.start();
        // start the scheduler
        this.scheduler = new WheelScheduler(this.id, this.workerCoordinator.createCheckProducer(this.id));
        this.scheduler.start();
        // listen to process pool events
        this.workerCoordinator.registerProcessPoolListener(this.id, new ProcessingPoolAssigner());
    }
    
    public void ownSite(UUID siteId, int processingPool)
    {
        logger.info("Assigning site processing pool " + siteId + "." + processingPool + " to processing pool " + this.id);
        this.scheduler.schedulePool(siteId, processingPool);
    }
    
    public void disownSite(UUID siteId, int processingPool)
    {
        logger.info("Unassigning site processing pool " + siteId + "." + processingPool + " to processing pool " + this.id);
        this.scheduler.unschedulePool(siteId, processingPool);
    }
    
    private class ProcessingPoolAssigner implements ProcessingPoolListener
    {
        @Override
        public void sitePoolAssigned(UUID siteId, int processingPool, UUID processingPoolId)
        {
            ownSite(siteId, processingPool);
        }

        @Override
        public void sitePoolUnassigned(UUID siteId, int processingPool, UUID processingPoolId)
        {
            disownSite(siteId, processingPool);
        }
    }
}

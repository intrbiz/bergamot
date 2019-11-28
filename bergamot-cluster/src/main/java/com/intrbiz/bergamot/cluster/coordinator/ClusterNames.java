package com.intrbiz.bergamot.cluster.coordinator;

import java.util.UUID;

public final class ClusterNames
{
    public static final class Attributes
    {

        public static final String MEMBER_TYPE_UI = "bergamot.member.type.ui";
        
        public static final String MEMBER_TYPE_API = "bergamot.member.type.ui";
        
        public static final String MEMBER_TYPE_PROCESSOR = "bergamot.member.type.processor";
        
        public static final String MEMBER_TYPE_WORKER = "bergamot.member.type.worker";
        
        public static final String MEMBER_TYPE_NOTIFIER = "bergamot.member.type.notifier";
        
    }
    
    public static final String buildProcessingPoolsMapName()
    {
        return "bergamot.cluster.processing.pools";
    }
    
    public static final String buildProcessingPoolSitesMapName()
    {
        return "bergamot.cluster.processing.pool.sites";
    }
    
    public static final String buildWorkerRegistrationsMapName()
    {
        return "bergamot.cluster.workers";
    }
    
    public static final String buildCheckQueueName(UUID workerId)
    {
        return "bergamot.queue.worker.checks." + workerId;
    }
    
    public static final String buildResultQueueName(UUID pool)
    {
        return "bergamot.queue.pool.results." + pool;
    }
    
    public static final String buildReadingQueueName(UUID pool)
    {
        return "bergamot.queue.pool.readings." + pool;
    }
    
    public static final String getAssignmentLockName()
    {
        return "bergamot.lock.processing.pool.assignment";
    }
}

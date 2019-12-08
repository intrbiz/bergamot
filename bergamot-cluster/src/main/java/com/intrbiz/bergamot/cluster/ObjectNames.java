package com.intrbiz.bergamot.cluster;

import java.util.UUID;

public final class ObjectNames
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
    
    public static final String buildWorkerRegistrationsMapName()
    {
        return "bergamot.cluster.workers";
    }
    
    public static final String buildCheckQueueName(UUID workerId)
    {
        return "bergamot.queue.worker.checks." + workerId;
    }
    
    public static final String buildResultQueueName(UUID memberId)
    {
        return "bergamot.queue.pool.results." + memberId;
    }
    
    public static final String buildReadingQueueName(UUID memberId)
    {
        return "bergamot.queue.pool.readings." + memberId;
    }
    
    public static final String buildClusterMigrationQueueName(UUID memberUUID)
    {
        return "bergamot.queue.cluster.migrations." + memberUUID;
    }
    
    public static final String getSiteTopicName()
    {
        return "bergamot.topic.event.site";
    }
    
    public static final String getClusterManagerLock()
    {
        return "bergamot.lock.cluster.manager";
    }
}

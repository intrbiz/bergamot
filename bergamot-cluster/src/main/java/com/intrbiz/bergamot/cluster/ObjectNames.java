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
        return "bergamot.cluster.map.processing.pools";
    }
    
    public static final String getClusterManagerLock()
    {
        return "bergamot.cluster.lock.manager";
    }
    
    public static final String buildWorkerRegistrationsMapName()
    {
        return "bergamot.cluster.map.workers";
    }
    
    public static final String buildAgentsMapName()
    {
        return "bergamot.cluster.map.agents";
    }
    
    public static final String buildNotifierRegistrationsMapName()
    {
        return "bergamot.cluster.map.notifiers";
    }
    
    public static final String buildNotifierRegistrationsLockName()
    {
        return "bergamot.cluster.lock.notifiers";
    }
    
    public static final String buildWorkerQueueName(UUID workerId)
    {
        return "bergamot.queue.worker." + workerId;
    }
    
    public static final String buildWorkerCleanupQueueName()
    {
        return "bergamot.queue.worker.cleanup";
    }
    
    public static final String buildWorkerDeadQueueName()
    {
        return "bergamot.queue.worker.dead";
    }
    
    public static final String buildNotifierQueueName(UUID notifierId)
    {
        return "bergamot.queue.notifier." + notifierId;
    }
    
    public static final String buildNotifierCleanupQueueName()
    {
        return "bergamot.queue.notifier.cleanup";
    }
    
    public static final String buildNotifierDeadQueueName()
    {
        return "bergamot.queue.notifier.dead";
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
    
    public static final String getSiteNotificationTopicName(UUID site)
    {
        return "bergamot.topic.notification.site." + site;
    }
    
    public static final String getSiteUpdateTopicName(UUID site)
    {
        return "bergamot.topic.update.site." + site;
    }
    
    public static final String buildAgentKeyLookupMapName()
    {
        return "bergamot.cluster.map.lookup.agent.key";
    }
}

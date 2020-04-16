package com.intrbiz.bergamot.cluster.util;

import java.util.UUID;

public final class HZNames
{
    public static final String buildWorkerQueueName(UUID workerId)
    {
        return "bergamot.queue.worker." + (workerId == null ? "*" : workerId.toString());
    }
    
    public static final String buildNotifierQueueName(UUID notifierId)
    {
        return "bergamot.queue.notifier." + (notifierId == null ? "*" : notifierId.toString());
    }
    
    public static final String buildProcessorQueueName(UUID processorId)
    {
        return "bergamot.queue.processor." + (processorId == null ? "*" : processorId.toString());
    }
    
    public static final String getSiteEventTopicName()
    {
        return "bergamot.topic.event.site";
    }
    
    public static final String getSchedulingTopicName()
    {
        return "bergamot.topic.scheduling";
    }
    
    public static final String getSiteNotificationTopicName(UUID site)
    {
        return "bergamot.topic.notification.site." + (site == null ? "*" : site.toString());
    }
    
    public static final String getSiteUpdateTopicName(UUID site)
    {
        return "bergamot.topic.update.site." + (site == null ? "*" : site.toString());
    }
    
    public static final String buildAgentKeyLookupMapName()
    {
        return "bergamot.cluster.map.lookup.agent.key";
    }
}

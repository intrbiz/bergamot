package com.intrbiz.bergamot.cluster.util;

import java.util.UUID;

public final class HZNames
{    
    public static final String buildWorkersSequenceMapName()
    {
        return "bergamot.map.workers.sequence";
    }
    
    public static final String buildWorkerRingbufferName(UUID workerId)
    {
        return "bergamot.ringbuffer.worker." + (workerId == null ? "*" : workerId.toString());
    }
    
    public static final String buildNotifiersSequenceMapName()
    {
        return "bergamot.map.notifiers.sequence";
    }
    
    public static final String buildNotifierRingbufferName(UUID notifierId)
    {
        return "bergamot.ringbuffer.notifier." + (notifierId == null ? "*" : notifierId.toString());
    }
    
    public static final String buildProcessorsSequenceMapName()
    {
        return "bergamot.map.processors.sequence";
    }
    
    public static final String buildProcessorRingbufferName(UUID processorId)
    {
        return "bergamot.ringbuffer.processor." + (processorId == null ? "*" : processorId.toString());
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
    
    public static final String buildProxyKeyLookupMapName()
    {
        return "bergamot.cluster.map.lookup.proxy.key";
    }
}

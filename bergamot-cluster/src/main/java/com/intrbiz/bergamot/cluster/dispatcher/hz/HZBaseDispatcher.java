package com.intrbiz.bergamot.cluster.dispatcher.hz;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.ringbuffer.Ringbuffer;
import com.hazelcast.ringbuffer.impl.RingbufferService;

/**
 * Dispatch results, readings, agent message to a processor
 */
public abstract class HZBaseDispatcher<T>
{
    private final HazelcastInstance hazelcast;
    
    private final ConcurrentMap<String, Ringbuffer<T>> ringbuffersCache = new ConcurrentHashMap<>();
    
    private final Function<UUID, String> ringbufferName;
    
    protected HZBaseDispatcher(HazelcastInstance hazelcast, Function<UUID, String> ringbufferName)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.ringbufferName = Objects.requireNonNull(ringbufferName);
        this.hazelcast.addDistributedObjectListener(new RingbufferListener());
    }
    
    protected Ringbuffer<T> getRingbuffer(UUID processorId)
    {
        return this.ringbuffersCache.computeIfAbsent(this.ringbufferName.apply(processorId), this.hazelcast::getRingbuffer);
    }

    private class RingbufferListener implements DistributedObjectListener
    {
        @Override
        public void distributedObjectCreated(DistributedObjectEvent event)
        {
        }

        @Override
        public void distributedObjectDestroyed(DistributedObjectEvent event)
        {
            if (RingbufferService.SERVICE_NAME.equals(event.getServiceName()))
            {
                HZBaseDispatcher.this.ringbuffersCache.remove(event.getObjectName());
            }
        }
    }
}

package com.intrbiz.bergamot.cluster.consumer;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;

public class ProcessorConsumer extends BaseConsumer<ProcessorMessage>
{
    public ProcessorConsumer(HazelcastInstance hazelcast, UUID id)
    {
        super(hazelcast, id, HZNames::buildProcessorRingbufferName, HZNames::buildProcessorsSequenceMapName);
    }
}

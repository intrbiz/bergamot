package com.intrbiz.bergamot.cluster.consumer.hz;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.consumer.ProcessorConsumer;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;

public class HZProcessorConsumer extends HZBaseConsumer<ProcessorMessage> implements ProcessorConsumer
{
    public HZProcessorConsumer(HazelcastInstance hazelcast, UUID id)
    {
        super(hazelcast, id, HZNames::buildProcessorRingbufferName, HZNames::buildProcessorsSequenceMapName);
    }
}

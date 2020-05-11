package com.intrbiz.bergamot.cluster.consumer.hz;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.worker.WorkerMessage;

public class HZWorkerConsumer extends HZBaseConsumer<WorkerMessage> implements WorkerConsumer
{
    public HZWorkerConsumer(HazelcastInstance hazelcast, UUID id)
    {
        super(hazelcast, id, HZNames::buildWorkerRingbufferName, HZNames::buildWorkersSequenceMapName);
    }
}

package com.intrbiz.bergamot.cluster.consumer;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

public class WorkerConsumer extends BaseConsumer<ExecuteCheck>
{
    public WorkerConsumer(HazelcastInstance hazelcast, UUID id)
    {
        super(hazelcast, id, HZNames::buildWorkerRingbufferName, HZNames::buildWorkersSequenceMapName);
    }
}

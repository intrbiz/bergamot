package com.intrbiz.bergamot.cluster.consumer.hz;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

public class HZWorkerConsumer extends HZBaseConsumer<ExecuteCheck> implements WorkerConsumer
{
    public HZWorkerConsumer(HazelcastInstance hazelcast, UUID id)
    {
        super(hazelcast, id, HZNames::buildWorkerRingbufferName, HZNames::buildWorkersSequenceMapName);
    }
}

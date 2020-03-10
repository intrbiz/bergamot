package com.intrbiz.bergamot.cluster.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.hazelcast.aggregation.Aggregator;

public class WorkerAgentCountAggregator extends Aggregator<Map.Entry<UUID, UUID>, Map<UUID, Integer>>
{
    private static final long serialVersionUID = 1L;
    
    private Map<UUID, Integer> agentCounts = new HashMap<>();

    @Override
    public void accumulate(Entry<UUID, UUID> input)
    {
        this.agentCounts.compute(input.getValue(), (key, old) -> old == null ? 1 : old + 1);
    }

    @Override
    public void combine(@SuppressWarnings("rawtypes") Aggregator aggregator)
    {
        for (Entry<UUID, Integer> entry : ((WorkerAgentCountAggregator) aggregator).agentCounts.entrySet())
        {
            this.agentCounts.merge(entry.getKey(), entry.getValue(), (a, b) -> a + b);
        }
    }

    @Override
    public Map<UUID, Integer> aggregate()
    {
        return this.agentCounts;
    }
}

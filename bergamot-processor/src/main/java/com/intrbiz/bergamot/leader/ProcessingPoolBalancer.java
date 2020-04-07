package com.intrbiz.bergamot.leader;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Stack;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.election.PoolElector;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;
import com.intrbiz.bergamot.model.message.cluster.ProcessorRegistration;

public class ProcessingPoolBalancer
{
    private static final Logger logger = Logger.getLogger(ProcessingPoolBalancer.class);
    
    private final PoolElector[] poolElectors;
    
    private final ProcessorRegistry processorRegistry;

    public ProcessingPoolBalancer(PoolElector[] poolElectors, ProcessorRegistry processorRegistry)
    {
        super();
        this.poolElectors = Objects.requireNonNull(poolElectors);
        this.processorRegistry = Objects.requireNonNull(processorRegistry);
    }
    
    public void balance()
    {
        try
        {
            // Compute our targets
            int processors = this.processorRegistry.count();
            int poolsPerProcessor = (int) Math.ceil(((double) this.poolElectors.length) / ((double) processors));
            logger.info("Balancing processing pools across processors, target: " + poolsPerProcessor);
            // Compute the current distribution of the pools
            Map<UUID, Stack<Integer>> poolDistribution = new HashMap<>();
            for (PoolElector pool : this.shufflePools())
            {
                poolDistribution.computeIfAbsent(pool.getLeader(), (key) -> new Stack<>()).push(pool.getPool());
            }
            // Take pools away from nodes which have to many
            Stack<Integer> poolsToReassign = new Stack<>();
            for (Entry<UUID, Stack<Integer>> entry : poolDistribution.entrySet())
            {
                while (entry.getValue().size() > poolsPerProcessor)
                {
                    poolsToReassign.push(entry.getValue().pop());
                }
            }
            // Assign pools to nodes without enough pools
            if (! poolsToReassign.isEmpty())
            {
                Map<Integer, UUID> reasignments = new HashMap<>();
                // rebalance onto new members
                for (ProcessorRegistration processor : this.processorRegistry.getProcessors())
                {
                    if (! poolDistribution.containsKey(processor.getId()))
                    {
                        int extra = poolsPerProcessor;
                        while (extra > 0 && poolsToReassign.size() > 0)
                        {
                            reasignments.put(poolsToReassign.pop(), processor.getId());
                            extra --;
                        }    
                    }
                }
                // rebalance onto old members
                for (Entry<UUID, Stack<Integer>> entry : poolDistribution.entrySet())
                {
                    int extra = poolsPerProcessor - entry.getValue().size();
                    while (extra > 0 && poolsToReassign.size() > 0)
                    {
                        reasignments.put(poolsToReassign.pop(), entry.getKey());
                        extra --;
                    }
                }
                // Actually move stuff around
                logger.info("Reassigning pools: " + reasignments.keySet());
                for (Entry<Integer, UUID> entry : reasignments.entrySet())
                {
                    logger.info("Reassigning pools " + entry.getKey() + " to " + entry.getValue());
                    this.poolElectors[entry.getKey()].promoteMember(entry.getValue());
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error balancing processing pools", e);
        }
    }
    
    protected List<PoolElector> shufflePools()
    {
        List<PoolElector> pools = new LinkedList<>(Arrays.asList(this.poolElectors));
        Collections.shuffle(pools);
        return pools;
    }
}

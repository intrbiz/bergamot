package com.intrbiz.bergamot.leader;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Stack;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.election.LeaderElector;
import com.intrbiz.bergamot.cluster.election.SchedulingPoolElector;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;
import com.intrbiz.bergamot.model.message.cluster.ProcessorRegistration;

public class ProcessingPoolBalancer
{
    private static final Logger logger = Logger.getLogger(ProcessingPoolBalancer.class);
    
    private final SchedulingPoolElector[] poolElectors;
    
    private final ProcessorRegistry processorRegistry;
    
    private final LeaderElector leaderElector;

    public ProcessingPoolBalancer(SchedulingPoolElector[] poolElectors, ProcessorRegistry processorRegistry, LeaderElector leaderElector)
    {
        super();
        this.poolElectors = Objects.requireNonNull(poolElectors);
        this.processorRegistry = Objects.requireNonNull(processorRegistry);
        this.leaderElector = Objects.requireNonNull(leaderElector);
    }
    
    public void balance()
    {
        // Balance the pool leaders
        int leadersMoved = this.balanceLeaders();
        // Balance pool followers
        if (leadersMoved == 0)
        {
            this.balanceFollowers();
        }
    }
    
    public int balanceLeaders()
    {
        int moved = 0;
        try
        {
            // Compute our targets
            int processors = this.leaderElector.getElectionMemberCount();
            int poolsPerProcessor = (int) Math.ceil(((double) this.poolElectors.length) / ((double) processors));
            // Compute the current distribution of the pools
            Map<UUID, Stack<Integer>> poolDistribution = new HashMap<>();
            for (SchedulingPoolElector pool : this.shufflePools())
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
                LinkedHashMap<Integer, UUID> reasignments = new LinkedHashMap<>();
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
                logger.info("Reassigning pools, target " + poolsPerProcessor + ": " + reasignments.keySet());
                for (Entry<Integer, UUID> entry : reasignments.entrySet())
                {
                    logger.info("Reassigning pool " + entry.getKey() + " to " + entry.getValue());
                    this.poolElectors[entry.getKey()].promoteMember(entry.getValue());
                    moved ++;
                    Thread.sleep(2_500);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error balancing processing pools", e);
        }
        return moved;
    }
    
    public int balanceFollowers()
    {
        int moved = 0;
        try
        {
            // Compute our targets
            int processors = this.leaderElector.getElectionMemberCount();
            int poolsPerProcessor = (int) Math.ceil(((double) this.poolElectors.length) / ((double) processors));
            // Compute the current distribution of the pools for each follower level
            for (int level = 1; level < (processors - 1) ; level++)
            {
                Map<UUID, Stack<Integer>> poolDistribution = new HashMap<>();
                for (SchedulingPoolElector pool : this.shufflePools())
                {
                    poolDistribution.computeIfAbsent(pool.getElectionMember(level), (key) -> new Stack<>()).push(pool.getPool());
                }
                // Take pools away from nodes which have to many
                for (Entry<UUID, Stack<Integer>> entry : poolDistribution.entrySet())
                {
                    if (entry.getValue().size() > poolsPerProcessor)
                    {
                        logger.info("Member " + entry.getKey() + " has " + entry.getValue().size() + " pools at level " + level);
                        while (entry.getValue().size() > poolsPerProcessor)
                        {
                            int pool = entry.getValue().pop();
                            logger.info("Releasing member " + entry.getKey() + " as follower of pool " + pool);
                            this.poolElectors[pool].releaseMember(entry.getKey());
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error balancing processing pools", e);
        }
        return moved;
    }
    
    protected List<SchedulingPoolElector> shufflePools()
    {
        List<SchedulingPoolElector> pools = new LinkedList<>(Arrays.asList(this.poolElectors));
        Collections.shuffle(pools);
        return pools;
    }
}

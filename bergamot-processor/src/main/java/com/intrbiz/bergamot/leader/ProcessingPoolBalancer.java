package com.intrbiz.bergamot.leader;

import java.util.Objects;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.election.PoolElector;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;

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
        
    }
}

package com.intrbiz.bergamot.leader;

import com.intrbiz.bergamot.cluster.election.PoolElector;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;

public class BergamotClusterLeader
{
    private ProcessingPoolBalancer processingPoolBalancer;
    
    public BergamotClusterLeader(PoolElector[] poolElectors, ProcessorRegistry processorRegistry)
    {
        super();
        this.processingPoolBalancer = new ProcessingPoolBalancer(poolElectors, processorRegistry);
    }
    

    public void start()
    {
    }
    
    protected void run()
    {
        
    }
    
    public void halt()
    {
    }
    
}

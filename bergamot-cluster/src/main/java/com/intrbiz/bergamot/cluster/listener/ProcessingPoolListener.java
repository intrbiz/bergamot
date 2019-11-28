package com.intrbiz.bergamot.cluster.listener;

import java.util.UUID;

public interface ProcessingPoolListener
{

    void sitePoolAssigned(UUID siteId, int processingPool, UUID processingPoolId);
    
    void sitePoolUnassigned(UUID siteId, int processingPool, UUID processingPoolId);
    
}

package com.intrbiz.bergamot.virtual;

import java.util.UUID;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Trap;

/**
 * Resolve checks by looking them up in the ObjectStore
 */
public interface VirtualCheckExpressionContext
{   
    Check<?,?> lookupCheck(UUID id);
    
    Host lookupHost(String name);
    
    Host lookupHost(UUID id);
    
    Cluster lookupCluster(String name);
    
    Cluster lookupCluster(UUID id);
    
    Service lookupService(Host on, String name);
    
    Service lookupAnonymousService(String name);
    
    Service lookupService(UUID id);
    
    Trap lookupTrap(Host on, String name);
    
    Trap lookupAnonymousTrap(String name);
    
    Trap lookupTrap(UUID id);
    
    Resource lookupResource(Cluster on, String name);
    
    Resource lookupResource(UUID id);
}

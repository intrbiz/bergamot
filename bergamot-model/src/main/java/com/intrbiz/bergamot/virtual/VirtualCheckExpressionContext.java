package com.intrbiz.bergamot.virtual;

import java.util.List;
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
    
    List<Host> lookupHostsInPool(String pool);
    
    Cluster lookupCluster(String name);
    
    Cluster lookupCluster(UUID id);
    
    Service lookupService(Host on, String name);
    
    Service lookupAnonymousService(String name);
    
    Service lookupService(UUID id);
    
    List<Service> lookupAnonymousServicesInPool(String pool);
    
    List<Service> lookupServicesInPool(String service, String pool);
    
    Trap lookupTrap(Host on, String name);
    
    Trap lookupAnonymousTrap(String name);
    
    Trap lookupTrap(UUID id);
    
    List<Trap> lookupAnonymousTrapsInPool(String pool);
    
    List<Trap> lookupTrapsInPool(String trap, String pool);
    
    Resource lookupResource(Cluster on, String name);
    
    Resource lookupResource(UUID id);
}

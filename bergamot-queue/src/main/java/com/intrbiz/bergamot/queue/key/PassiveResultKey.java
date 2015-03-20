package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

/**
 * A key use to route passive check results
 */
public class PassiveResultKey extends ResultKey
{
    /**
     * Route to the passive check processors for the given site.
     * 
     * Note: the given UUID can either be a site id or an object id, it will be masked to a site id
     */
    public PassiveResultKey(UUID id)
    {
        super(id == null ? "" : toSiteId(id).toString());
    }
    
    /**
     * Route to the global passive check processors
     * 
     * Note: this should be avoided where possible
     */
    public PassiveResultKey()
    {
        super("");
    }
}

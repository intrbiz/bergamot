package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

/**
 * A key used to route active check results
 */
public class ActiveResultKey extends ResultKey
{
    /**
     * Route to the specific processing pool of an active check
     * @param site the site id
     * @param pool the processing pool
     */
    public ActiveResultKey(UUID site, int pool)
    {
        super(site + "." + pool);
    }
}

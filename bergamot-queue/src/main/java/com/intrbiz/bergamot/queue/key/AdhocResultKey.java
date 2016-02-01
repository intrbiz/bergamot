package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

/**
 * A key used to route adhoc check results
 */
public class AdhocResultKey extends ResultKey
{
    /**
     * Route to the originator of an adhoc check
     * @param site the adhoc id
     */
    public AdhocResultKey(UUID adhocId)
    {
        super(toSiteId(adhocId) + "." + adhocId);
    }
}

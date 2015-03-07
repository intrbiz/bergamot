package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

/**
 * A key used to route active check results
 */
public class ActiveResultKey extends ResultKey
{
    public ActiveResultKey(UUID site, int pool)
    {
        super(site + "." + pool);
    }
}

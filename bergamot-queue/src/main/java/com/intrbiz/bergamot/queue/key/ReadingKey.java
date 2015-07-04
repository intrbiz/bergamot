package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

import com.intrbiz.bergamot.model.message.SiteMO;
import com.intrbiz.queue.name.GenericKey;

/**
 * A key used to route readings
 */
public class ReadingKey extends GenericKey
{    
    protected ReadingKey(String key)
    {
        super(key);
    }
    
    /**
     * Route to the specific processing pool of an active check
     * @param site the site id
     * @param pool the processing pool
     */
    public ReadingKey(UUID siteId, int pool)
    {
        super(SiteMO.getSiteId(siteId) + "." + pool);
    }
    
    /**
     * Route to the passive check processors for the given site.
     * 
     * Note: the given UUID can either be a site id or an object id, it will be masked to a site id
     */
    public ReadingKey(UUID id)
    {
        super(id == null ? "" : ResultKey.toSiteId(id).toString());
    }
    
    /**
     * Route to the global passive check processors
     * 
     * Note: this should be avoided where possible
     */
    public ReadingKey()
    {
        super("");
    }
}

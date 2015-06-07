package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

import com.intrbiz.queue.name.GenericKey;

/**
 * A key used to route readings
 */
public abstract class ReadingKey extends GenericKey
{    
    protected ReadingKey(String key)
    {
        super(key);
    }
    
    /**
     * For a given id mask down to only the site id.
     * 
     * Note: toSiteId(site_id) == site_id
     * 
     * @param id the id to mask
     * @return the site id
     */
    public static UUID toSiteId(UUID id)
    {
        return new UUID((id.getMostSignificantBits() & 0xFFFFFFFF_FFFF0000L) | 0x0000000000004000L, 0x80000000_00000000L);
    }
}

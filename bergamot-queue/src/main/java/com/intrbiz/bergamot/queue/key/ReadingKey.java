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
    
    public ReadingKey(UUID siteId, int pool)
    {
        super(SiteMO.getSiteId(siteId) + "." + pool);
    }
    
    public ReadingKey()
    {
        super("");
    }
}

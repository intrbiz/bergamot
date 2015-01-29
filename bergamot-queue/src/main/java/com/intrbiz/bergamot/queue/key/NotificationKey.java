package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

import com.intrbiz.queue.name.GenericKey;

public class NotificationKey extends GenericKey
{    
    public NotificationKey(UUID site)
    {
        super(site.toString());
    }
}

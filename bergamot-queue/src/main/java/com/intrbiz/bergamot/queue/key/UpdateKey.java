package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

import com.intrbiz.queue.name.GenericKey;

public class UpdateKey extends GenericKey
{    
    public UpdateKey(UUID site, UUID checkId)
    {
        super(site + "." + checkId.toString());
    }
    
    public UpdateKey(UUID site)
    {
        super(site + ".*");
    }
}

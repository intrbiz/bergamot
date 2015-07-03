package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

import com.intrbiz.queue.name.GenericKey;

public class UpdateKey extends GenericKey
{   
    public enum UpdateType {
        CHECK,
        GROUP,
        LOCATION,
        ALERT
    }
    
    public UpdateKey(UpdateType type, UUID site, UUID checkId)
    {
        super(type.toString().toLowerCase() + "." + site + "." + checkId.toString());
    }
    
    public UpdateKey(UpdateType type, UUID site)
    {
        super(type.toString().toLowerCase() + "." + site + ".*");
    }
}

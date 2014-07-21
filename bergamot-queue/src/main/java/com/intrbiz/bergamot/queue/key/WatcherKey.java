package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

import com.intrbiz.queue.name.GenericKey;

public class WatcherKey extends GenericKey
{    
    public WatcherKey(UUID watcherId, String engine)
    {
        super(watcherId + "." + engine);
    }

}

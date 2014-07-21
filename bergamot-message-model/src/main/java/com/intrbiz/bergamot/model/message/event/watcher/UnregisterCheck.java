package com.intrbiz.bergamot.model.message.event.watcher;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;

/**
 * Unregister the given check with a watcher
 */
@JsonTypeName("bergamot.unregister_check")
public class UnregisterCheck extends CheckEvent
{    
    public UnregisterCheck()
    {
        super();
    }
}

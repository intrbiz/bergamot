package com.intrbiz.bergamot.model.message.worker.check;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Unregister the given check with a watcher
 */
@JsonTypeName("bergamot.worker.check.unregister")
public class UnregisterCheck extends CheckMessage
{   
    private static final long serialVersionUID = 1L;
    
    public UnregisterCheck()
    {
        super();
    }
}

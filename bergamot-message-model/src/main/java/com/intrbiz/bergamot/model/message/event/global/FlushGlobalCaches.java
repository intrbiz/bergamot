package com.intrbiz.bergamot.model.message.event.global;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Flush the global caches of the UI cluster
 */
@JsonTypeName("bergamot.event.global.flush_caches")
public class FlushGlobalCaches extends GlobalEvent
{   
    private static final long serialVersionUID = 1L;
    
    public FlushGlobalCaches()
    {
        super();
    }
}

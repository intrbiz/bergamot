package com.intrbiz.bergamot.model.message.event.watcher;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.pool.check.CheckMessage;

/**
 * Register the given check with a watcher
 */
@JsonTypeName("bergamot.register_check")
public class RegisterCheck extends CheckMessage
{   
    private static final long serialVersionUID = 1L;

    public RegisterCheck()
    {
        super();
    }
}

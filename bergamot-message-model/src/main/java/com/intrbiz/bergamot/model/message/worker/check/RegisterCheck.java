package com.intrbiz.bergamot.model.message.worker.check;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Register the given check with a watcher
 */
@JsonTypeName("bergamot.worker.check.register")
public class RegisterCheck extends CheckMessage
{   
    private static final long serialVersionUID = 1L;

    public RegisterCheck()
    {
        super();
    }
}

package com.intrbiz.bergamot.model.message.check;

import com.fasterxml.jackson.annotation.JsonTypeName;

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

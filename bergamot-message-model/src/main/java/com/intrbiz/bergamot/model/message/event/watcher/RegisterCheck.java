package com.intrbiz.bergamot.model.message.event.watcher;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;

/**
 * Register the given check with a watcher
 */
@JsonTypeName("bergamot.register_check")
public class RegisterCheck extends CheckEvent
{   
    public RegisterCheck()
    {
        super();
    }
}

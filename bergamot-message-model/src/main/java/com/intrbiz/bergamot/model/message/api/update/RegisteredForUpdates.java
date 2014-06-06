package com.intrbiz.bergamot.model.message.api.update;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIResponse;

@JsonTypeName("bergamot.api.registered_for_updates")
public class RegisteredForUpdates extends APIResponse
{
    public RegisteredForUpdates(RegisterForUpdates inResponseTo)
    {
        super(inResponseTo, Stat.OK);
    }
}

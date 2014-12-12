package com.intrbiz.bergamot.model.message.api.notification;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIResponse;

@JsonTypeName("bergamot.api.registered_for_notifications")
public class RegisteredForNotifications extends APIResponse
{
    public RegisteredForNotifications(RegisterForNotifications inResponseTo)
    {
        super(inResponseTo, Stat.OK);
    }
}

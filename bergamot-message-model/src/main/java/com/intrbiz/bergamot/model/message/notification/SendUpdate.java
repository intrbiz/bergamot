package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A check update notifications
 */
@JsonTypeName("bergamot.send_update")
public class SendUpdate extends CheckNotification
{    
    public SendUpdate()
    {
        super();
    }
    
    public String getNotificationType()
    {
        return "update";
    }
}

package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A check update notifications
 */
@JsonTypeName("bergamot.send_update")
public class SendUpdate extends CheckNotification
{
    private static final long serialVersionUID = 1L;

    public SendUpdate()
    {
        super();
    }
    
    public String getNotificationType()
    {
        return "update";
    }
}

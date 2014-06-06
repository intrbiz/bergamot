package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * An alert
 */
@JsonTypeName("bergamot.send_recovery")
public class SendRecovery extends Notification
{
    public SendRecovery()
    {
        super();
    }
    
    public String getNotificationType()
    {
        return "recovery";
    }
}

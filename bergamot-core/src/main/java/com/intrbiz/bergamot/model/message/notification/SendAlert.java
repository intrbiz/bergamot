package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * An alert
 */
@JsonTypeName("bergamot.send_alert")
public class SendAlert extends Notification
{
    public SendAlert()
    {
        super();
    }

    @Override
    public String getDefaultExchange()
    {
        return "bergamot.notification";
    }
}

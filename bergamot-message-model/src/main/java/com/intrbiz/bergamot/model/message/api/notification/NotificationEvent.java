package com.intrbiz.bergamot.model.message.api.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIEvent;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;

@JsonTypeName("bergamot.api.event.notification")
public class NotificationEvent extends APIEvent
{
    @JsonProperty("notification")
    private CheckNotification notification;

    public NotificationEvent()
    {
        super();
    }

    public NotificationEvent(CheckNotification notification)
    {
        super();
        this.notification = notification;
    }

    public CheckNotification getNotification()
    {
        return notification;
    }

    public void setNotification(CheckNotification notification)
    {
        this.notification = notification;
    }
}

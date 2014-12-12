package com.intrbiz.bergamot.model.message.api.notification;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIRequest;

@JsonTypeName("bergamot.api.register_for_notifications")
public class RegisterForNotifications extends APIRequest
{
    @JsonProperty("site_id")
    private UUID siteId;

    public RegisterForNotifications()
    {
        super();
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }
}

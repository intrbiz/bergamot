package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.SiteMO;

@JsonTypeName("bergamot.password_reset")
public class PasswordResetNotification extends ContactNotification
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("url")
    private String url;

    public PasswordResetNotification()
    {
        super();
    }
    
    public PasswordResetNotification(SiteMO site, ContactMO contact, String url)
    {
        super(contact);
        this.setRaised(System.currentTimeMillis());
        this.setSite(site);
        this.getTo().add(contact);
        this.setUrl(url);
    }

    @Override
    public String getNotificationType()
    {
        return "password_reset";
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
}

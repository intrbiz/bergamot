package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.SiteMO;

@JsonTypeName("bergamot.register_contact")
public class RegisterContactNotification extends GenericNotification
{
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("contact")
    private ContactMO contact;

    public RegisterContactNotification()
    {
        super();
    }
    
    public RegisterContactNotification(SiteMO site, ContactMO contact, String url)
    {
        super();
        this.setRaised(System.currentTimeMillis());
        this.setSite(site);
        this.setContact(contact);
        this.getTo().add(contact);
        this.setUrl(url);
    }

    @Override
    public String getNotificationType()
    {
        return "register_contact";
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public ContactMO getContact()
    {
        return contact;
    }

    public void setContact(ContactMO contact)
    {
        this.contact = contact;
    }
}

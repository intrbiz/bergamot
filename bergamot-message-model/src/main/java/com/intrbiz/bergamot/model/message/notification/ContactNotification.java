package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.ContactMO;

public abstract class ContactNotification extends GenericNotification
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("contact")
    private ContactMO contact;
    
    public ContactNotification()
    {
        super();
    }
    
    public ContactNotification(ContactMO contact)
    {
        super();
        this.setContact(contact);
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

package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.SiteMO;

/**
 * Sent when a backup code is used to login for a contact
 */
@JsonTypeName("bergamot.u2fa.backup_code_used")
public class BackupCodeUsed extends GenericNotification
{
    @JsonProperty("contact")
    private ContactMO contact;

    @JsonProperty("code")
    private String code;
    
    @JsonProperty("registered_at")
    private long registeredAt;

    public BackupCodeUsed()
    {
        super();
    }

    public BackupCodeUsed(SiteMO site, ContactMO contact, String code)
    {
        super();
        this.setRaised(System.currentTimeMillis());
        this.setSite(site);
        this.setContact(contact);
        this.getTo().add(contact);
        this.code = code;
        this.registeredAt = System.currentTimeMillis();
    }

    @Override
    public String getNotificationType()
    {
        return "u2fa_backup_code_used";
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public ContactMO getContact()
    {
        return contact;
    }

    public void setContact(ContactMO contact)
    {
        this.contact = contact;
    }

    public long getRegisteredAt()
    {
        return registeredAt;
    }

    public void setRegisteredAt(long registeredAt)
    {
        this.registeredAt = registeredAt;
    }
}

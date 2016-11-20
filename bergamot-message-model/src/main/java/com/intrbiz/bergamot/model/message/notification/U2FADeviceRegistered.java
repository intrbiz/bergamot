package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.SiteMO;

/**
 * Sent when a new U2FA device is registered by a contact
 */
@JsonTypeName("bergamot.u2fa.device_registered")
public class U2FADeviceRegistered extends ContactNotification
{
    @JsonProperty("device_name")
    private String deviceName;

    @JsonProperty("device_type")
    private String deviceType;
    
    @JsonProperty("registered_at")
    private long registeredAt;

    public U2FADeviceRegistered()
    {
        super();
    }

    public U2FADeviceRegistered(SiteMO site, ContactMO contact, String deviceName, String deviceType)
    {
        super(contact);
        this.setRaised(System.currentTimeMillis());
        this.setSite(site);
        this.getTo().add(contact);
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.registeredAt = System.currentTimeMillis(); 
    }

    @Override
    public String getNotificationType()
    {
        return "u2fa_device_registered";
    }

    public String getDeviceName()
    {
        return deviceName;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    public String getDeviceType()
    {
        return deviceType;
    }

    public void setDeviceType(String deviceType)
    {
        this.deviceType = deviceType;
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

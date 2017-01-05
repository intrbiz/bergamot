package com.intrbiz.bergamot.ui.security.info;

import java.io.Serializable;

import com.intrbiz.bergamot.model.ContactU2FDeviceRegistration;

public class U2FDeviceAuthenticationDetail implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final ContactU2FDeviceRegistration u2fDevice;

    public U2FDeviceAuthenticationDetail(ContactU2FDeviceRegistration u2fDevice)
    {
        this.u2fDevice = u2fDevice;
    }

    public ContactU2FDeviceRegistration getU2fDevice()
    {
        return u2fDevice;
    }
}

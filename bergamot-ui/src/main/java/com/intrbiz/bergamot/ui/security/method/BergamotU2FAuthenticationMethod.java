package com.intrbiz.bergamot.ui.security.method;

import java.security.Principal;

import com.intrbiz.balsa.engine.impl.security.method.U2FAuthenticationMethod;
import com.intrbiz.balsa.error.BalsaSecurityException;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.ContactU2FDeviceRegistration;
import com.intrbiz.bergamot.ui.security.info.U2FDeviceAuthenticationDetail;
import com.yubico.u2f.data.DeviceRegistration;

public class BergamotU2FAuthenticationMethod extends U2FAuthenticationMethod
{
    @Override
    protected Object createAuthenticationInfoDetail(Principal principal, DeviceRegistration device) throws BalsaSecurityException
    {
        ContactU2FDeviceRegistration authenticatedUsing = ((Contact) principal).getU2FDeviceRegistrations().stream()
                .filter((d) -> d.getKeyHandle().equals(device.getKeyHandle()) && d.getPublicKey().equals(device.getPublicKey()))
                .findFirst().get();
        return new U2FDeviceAuthenticationDetail(authenticatedUsing);
    }
}

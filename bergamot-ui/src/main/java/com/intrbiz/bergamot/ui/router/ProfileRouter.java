package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.APIToken;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.U2FDeviceRegistration;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsString;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.IsaUUID;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.SessionVar;
import com.intrbiz.metadata.Template;
import com.yubico.u2f.U2F;
import com.yubico.u2f.attestation.Attestation;
import com.yubico.u2f.attestation.MetadataService;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.RegisterRequestData;
import com.yubico.u2f.data.messages.RegisterResponse;

@Prefix("/profile")
@Template("layout/main")
@RequireValidPrincipal()
public class ProfileRouter extends Router<BergamotApp>
{   
    private final U2F u2f = new U2F();
    
    private final MetadataService u2fMetadata = new MetadataService();
    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @SessionVar("site") Site site)
    {
        // generate a new U2F registration token
        Contact contact = currentPrincipal();
        RegisterRequestData registerRequestData = this.u2f.startRegistration(site.getU2FAppId(), contact.getU2FDeviceRegistrations().stream().map(U2FDeviceRegistration::toDeviceRegistration).collect(Collectors.toList()));
        var("u2fregister", registerRequestData);
        // encode the profile view
        encode("profile/index");
    }
    
    @Any("/revoke-api-token")
    @WithDataAdapter(BergamotDB.class)
    public void revokeAPIToken(BergamotDB db, @Param("token") @CheckStringLength(mandatory = true) String token) throws IOException
    {
        APIToken apiToken = db.getAPIToken(token);
        if (apiToken != null)
        {
            db.setAPIToken(apiToken.revoke());
        }
        redirect(path("/profile/"));
    }
    
    @Any("/remove-api-token")
    @WithDataAdapter(BergamotDB.class)
    public void removeAPIToken(BergamotDB db, @Param("token") @CheckStringLength(mandatory = true) String token) throws IOException
    {
        db.removeAPIToken(token);
        redirect(path("/profile/"));
    }
        
    @Any("/generate-api-token")
    @WithDataAdapter(BergamotDB.class)
    public void generateAPIToken(BergamotDB db, @Param("summary") @AsString() String summary) throws IOException
    {
        String token = app().getSecurityEngine().generatePerpetualAuthenticationTokenForPrincipal(currentPrincipal());
        db.setAPIToken(new APIToken(token, currentPrincipal(), Util.coalesceEmpty(summary, "API Access")));
        redirect(path("/profile/"));
    }
    
    @Any("/register-u2f-device")
    @WithDataAdapter(BergamotDB.class)
    public void registerU2FDevice(BergamotDB db, @SessionVar("site") Site site, @Param("u2f-register-request") String request, @Param("u2f-register-response") String response) throws Exception
    {
        // register
        DeviceRegistration reg = this.u2f.finishRegistration(RegisterRequestData.fromJson(request), RegisterResponse.fromJson(response));
        // lookup the device metadata
        Attestation attestation = this.u2fMetadata.getAttestation(reg.getAttestationCertificate());
        System.out.println("Trusted: " + (attestation == null ? null : attestation.isTrusted()) + " " + attestation);
        // store the registration
        db.setU2FDeviceRegistration(new U2FDeviceRegistration(
                currentPrincipal(), 
                reg,
                attestation == null ? null : attestation.getVendorProperties().get("name"),
                attestation == null ? null : attestation.getDeviceProperties().get("displayName"),
                attestation == null ? null : attestation.getDeviceProperties().get("imageUrl")
        ));
        // done
        redirect(path("/profile/"));
    }
    
    @Any("/revoke-u2f-device")
    @WithDataAdapter(BergamotDB.class)
    public void revokeU2FDevice(BergamotDB db, @Param("id") @IsaUUID UUID id) throws IOException
    {
        U2FDeviceRegistration device = db.getU2FDeviceRegistration(id);
        if (device != null)
        {
            db.setU2FDeviceRegistration(device.revoke());
        }
        redirect(path("/profile/"));
    }
    
    @Any("/remove-u2f-device")
    @WithDataAdapter(BergamotDB.class)
    public void removeU2FDevice(BergamotDB db, @Param("id") @IsaUUID UUID id) throws IOException
    {
        db.removeU2FDeviceRegistration(id);
        redirect(path("/profile/"));
    }
}

package com.intrbiz.bergamot.ui.router;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.metadata.GetBergamotSite;
import com.intrbiz.bergamot.model.APIToken;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.ContactHOTPRegistration;
import com.intrbiz.bergamot.model.ContactU2FDeviceRegistration;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.AsString;
import com.intrbiz.metadata.CheckStringLength;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.Get;
import com.intrbiz.metadata.IsaUUID;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;
import com.intrbiz.util.CounterHOTP;
import com.intrbiz.util.HOTP.HOTPSecret;
import com.yubico.u2f.U2F;
import com.yubico.u2f.attestation.Attestation;
import com.yubico.u2f.attestation.MetadataService;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.RegisterRequestData;
import com.yubico.u2f.data.messages.RegisterResponse;

import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

@Prefix("/profile")
@Template("layout/main")
@RequireValidPrincipal()
public class ProfileRouter extends Router<BergamotApp>
{   
    private final U2F u2f = new U2F();
    
    private final MetadataService u2fMetadata = new MetadataService();
    
    private final CounterHOTP hotp = new CounterHOTP();
    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @GetBergamotSite() Site site)
    {
        // generate a new U2F registration token
        Contact contact = currentPrincipal();
        RegisterRequestData registerRequestData = this.u2f.startRegistration(site.getU2FAppId(), contact.getU2FDeviceRegistrations().stream().map(ContactU2FDeviceRegistration::toDeviceRegistration).collect(Collectors.toList()));
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
    public void registerU2FDevice(BergamotDB db, @GetBergamotSite() Site site, @Param("u2f-register-request") String request, @Param("u2f-register-response") String response, @Param("summary") String summary) throws Exception
    {
        // the contact
        Contact contact = currentPrincipal();
        // the name
        String name = Util.coalesceEmpty(summary, "Security Key " + (contact.getHOTPRegistrations().size() + 1));
        // register
        DeviceRegistration reg = this.u2f.finishRegistration(RegisterRequestData.fromJson(request), RegisterResponse.fromJson(response));
        // lookup the device metadata
        Attestation attestation = this.u2fMetadata.getAttestation(reg.getAttestationCertificate());
        System.out.println("Trusted: " + (attestation == null ? null : attestation.isTrusted()) + " " + attestation);
        // store the registration
        db.setU2FDeviceRegistration(new ContactU2FDeviceRegistration(
                contact, 
                reg,
                attestation == null ? null : attestation.getVendorProperties().get("name"),
                attestation == null ? null : attestation.getDeviceProperties().get("displayName"),
                attestation == null ? null : attestation.getDeviceProperties().get("imageUrl"),
                name
        ));
        // do we need more backup codes
        contact.generateMoreBackupCodes();
        // notifications
        action("u2fa-device-registered", contact, name, attestation == null ? "Unknown Device" : attestation.getDeviceProperties().get("displayName"));
        // done
        redirect(path("/profile/"));
    }
    
    @Any("/revoke-u2f-device")
    @WithDataAdapter(BergamotDB.class)
    public void revokeU2FDevice(BergamotDB db, @Param("id") @IsaUUID UUID id) throws IOException
    {
        ContactU2FDeviceRegistration device = db.getU2FDeviceRegistration(id);
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
    
    @Any("/revoke-hotp")
    @WithDataAdapter(BergamotDB.class)
    public void revokeHOTPDevice(BergamotDB db, @Param("id") @IsaUUID UUID id) throws IOException
    {
        ContactHOTPRegistration device = db.getHOTPRegistration(id);
        if (device != null)
        {
            db.setHOTPRegistration(device.revoke());
        }
        redirect(path("/profile/"));
    }
    
    @Any("/remove-hotp")
    @WithDataAdapter(BergamotDB.class)
    public void removeHOTPDevice(BergamotDB db, @Param("id") @IsaUUID UUID id) throws IOException
    {
        db.removeHOTPRegistration(id);
        redirect(path("/profile/"));
    }
    
    @Any("/setup-hotp")
    @WithDataAdapter(BergamotDB.class)
    public void setupHOTPDevice(BergamotDB db, @GetBergamotSite() Site site, @Param("summary") String summary) throws Exception
    {
        // the contact
        Contact contact = currentPrincipal();
        // the name
        String name = Util.coalesceEmpty(summary, "Authenticator " + (contact.getHOTPRegistrations().size() + 1));
        // generate a HOTP secret
        HOTPSecret secret = this.hotp.newOTPSecret();
        // store our HOTP registration
        ContactHOTPRegistration registration = var("hotp", new ContactHOTPRegistration(contact, secret, name));
        db.setHOTPRegistration(registration);
        // do we need more backup codes
        contact.generateMoreBackupCodes();
        // notifications
        action("u2fa-device-registered", contact, name, "HOTP Authenticator");
        // done
        encode("/profile/setuphotp");
    }
    
    @Get("/hotp-qr-code")
    @WithDataAdapter(BergamotDB.class)
    public void generateHOTPQRCode(BergamotDB db, @Param("id") @IsaUUID UUID hotpRegistrationId, @CurrentPrincipal Contact contact, @GetBergamotSite Site site) throws IOException
    {
        // get the HOTP registration
        ContactHOTPRegistration registration = notNull(db.getHOTPRegistration(hotpRegistrationId));
        // require that this registration is owned by the current principal
        require(registration.getContactId().equals(contact.getId()));
        // generate the QR Code
        String account = Util.urlEncode(contact.getName(), Util.UTF8);
        String issuer  = Util.urlEncode(site.getSummary(), Util.UTF8);
        String secret  = registration.getHOTPSecret().toString();
        String otpQRData = "otpauth://hotp/" + account + "?secret=" + secret + "&issuer=" + issuer + "&algorithm=SHA1&digits=6&counter=0";
        // generate the QR code
        OutputStream stream = response().ok().contentType("image/png").getOutput();
        QRCode.from(otpQRData).withCharset("UTF-8").withSize(350, 350).to(ImageType.PNG).writeTo(stream);
    }
    
    @Any("/more-backup-codes")
    @WithDataAdapter(BergamotDB.class)
    public void moreBackupCodes(BergamotDB db) throws IOException
    {
        require(! authenticationState().info().isBackupCodeUsed());
        Contact contact = currentPrincipal();
        contact.generateMoreBackupCodes();
        redirect(path("/profile/"));
    }
}

package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.data.DataException;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.key.util.CertificateParser;
import com.yubico.u2f.data.messages.key.util.U2fB64Encoding;

/**
 * A U2F device which has been registered to a user
 */
@SQLTable(schema = BergamotDB.class, name = "u2f_device_registration", since = @SQLVersion({ 3, 38, 0 }))
public class U2FDeviceRegistration implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 3,38, 0 }))
    @SQLPrimaryKey
    private UUID id;
    
    /**
     * The contact whom this token represents
     */
    @SQLColumn(index = 2, name = "contact_id", since = @SQLVersion({ 3, 38, 0 }))
    @SQLForeignKey(references = Contact.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    private UUID contactId;
    
    @SQLColumn(index = 3, name = "key_handle", notNull = true, since = @SQLVersion({ 3, 38, 0 }))
    private String keyHandle;

    @SQLColumn(index = 4, name = "public_key", notNull = true, since = @SQLVersion({ 3, 38, 0 }))
    private String publicKey;
    
    @SQLColumn(index = 5, name = "attestation_cert", since = @SQLVersion({ 3, 38, 0 }))
    private String attestationCert;
    
    @SQLColumn(index = 6, name = "counter", notNull = true, since = @SQLVersion({ 3, 38, 0 }))
    private long counter;
    
    /**
     * When did we register this device
     */
    @SQLColumn(index = 7, name = "created", since = @SQLVersion({ 3, 38, 0 }))
    private Timestamp created = new Timestamp(System.currentTimeMillis());
    
    /**
     * When was this last updated
     */
    @SQLColumn(index = 8, name = "updated", since = @SQLVersion({ 3, 38, 0 }))
    private Timestamp updated = null;
    
    /**
     * Has this device been revoked
     */
    @SQLColumn(index = 9, name = "revoked", since = @SQLVersion({ 3, 38, 0 }))
    private boolean revoked = false;
    
    /**
     * When was it revoked
     */
    @SQLColumn(index = 10, name = "revoked_at", since = @SQLVersion({ 3, 38, 0 }))
    private Timestamp revokedAt = null;
    
    @SQLColumn(index = 11, name = "vendor", since = @SQLVersion({ 3, 38, 0 }))
    private String vendor;
    
    @SQLColumn(index = 12, name = "device", since = @SQLVersion({ 3, 38, 0 }))
    private String device;
    
    @SQLColumn(index = 13, name = "device_image", since = @SQLVersion({ 3, 38, 0 }))
    private String deviceImage;
    
    /**
     * A name for this method
     */
    @SQLColumn(index = 14, name = "summary", since = @SQLVersion({ 3, 39, 0 }))
    private String summary;
    
    public U2FDeviceRegistration()
    {
        super();
    }
    
    public U2FDeviceRegistration(Contact contact, DeviceRegistration devReg, String vendor, String device, String deviceImage, String name)
    {
        this.id = Site.randomId(contact.getSiteId());
        this.contactId = contact.getId();
        this.created = new Timestamp(System.currentTimeMillis());
        this.updated = null;
        this.revoked = false;
        this.fromDeviceRegistration(devReg);
        this.vendor = vendor;
        this.device = device;
        this.deviceImage = deviceImage;
        this.summary = name;
    }

    public UUID getContactId()
    {
        return contactId;
    }

    public void setContactId(UUID contactId)
    {
        this.contactId = contactId;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public String getKeyHandle()
    {
        return keyHandle;
    }

    public void setKeyHandle(String keyHandle)
    {
        this.keyHandle = keyHandle;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }

    public String getAttestationCert()
    {
        return attestationCert;
    }

    public void setAttestationCert(String attestationCert)
    {
        this.attestationCert = attestationCert;
    }
    
    public void saveAttestationCert(X509Certificate attestationCert) throws CertificateEncodingException
    {
        this.attestationCert = U2fB64Encoding.encode(attestationCert.getEncoded());
    }
    
    public X509Certificate loadAttestationCert() throws CertificateException
    {
        if (Util.isEmpty(this.attestationCert)) return null;
        // parse the cert
        return CertificateParser.parseDer(U2fB64Encoding.decode(this.attestationCert));
    }

    public long getCounter()
    {
        return counter;
    }

    public void setCounter(long counter)
    {
        this.counter = counter;
    }

    public Timestamp getCreated()
    {
        return created;
    }

    public void setCreated(Timestamp created)
    {
        this.created = created;
    }

    public Timestamp getUpdated()
    {
        return updated;
    }

    public void setUpdated(Timestamp updated)
    {
        this.updated = updated;
    }

    public boolean isRevoked()
    {
        return revoked;
    }

    public void setRevoked(boolean revoked)
    {
        this.revoked = revoked;
    }

    public Timestamp getRevokedAt()
    {
        return revokedAt;
    }

    public void setRevokedAt(Timestamp revokedAt)
    {
        this.revokedAt = revokedAt;
    }

    public String getVendor()
    {
        return vendor;
    }

    public void setVendor(String vendor)
    {
        this.vendor = vendor;
    }

    public String getDevice()
    {
        return device;
    }

    public void setDevice(String device)
    {
        this.device = device;
    }

    public String getDeviceImage()
    {
        return deviceImage;
    }

    public void setDeviceImage(String deviceImage)
    {
        this.deviceImage = deviceImage;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public Contact getContact()
    {
        if (this.getContactId() == null) return null;
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getContact(this.getContactId());
        }
    }
    
    public void fromDeviceRegistration(DeviceRegistration dev)
    {
        this.setKeyHandle(dev.getKeyHandle());
        this.setPublicKey(dev.getPublicKey());
        this.setCounter(dev.getCounter());
        try
        {
            this.saveAttestationCert(dev.getAttestationCertificate());
        }
        catch (Exception e)
        {
            throw new DataException("Failed to save attestation certificate", e);
        }
    }
    
    public DeviceRegistration toDeviceRegistration()
    {
        try
        {
            DeviceRegistration dev = new DeviceRegistration(this.keyHandle, this.publicKey, this.loadAttestationCert(), this.counter);
            if (this.isRevoked()) dev.markCompromised();
            return dev;
        }
        catch (Exception e)
        {
            throw new DataException("Failed to load attestation certificate", e);
        }
    }
    
    public U2FDeviceRegistration revoke()
    {
        this.revoked = true;
        this.revokedAt = new Timestamp(System.currentTimeMillis());
        return this;
    }
    
    public U2FDeviceRegistration used(long newCounter)
    {
        this.counter = newCounter;
        this.updated = new Timestamp(System.currentTimeMillis());
        return this;
    }
    
    public String toString()
    {
        return "U2FDevice { " + this.keyHandle + ", " + this.publicKey + "}";
    }
}

package com.intrbiz.bergamot.model.agent;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AgentAuthenticationKey implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final int VERSION_1 = 0x01;
    
    public static final byte MAGIC1 = (byte) 0x1B;
    
    public static final byte MAGIC2 = (byte) 0xA0;
    
    public static final String HMAC_SHA256 = "HmacSHA256";
    
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    private UUID id;
    
    private byte[] secret;
    
    public AgentAuthenticationKey()
    {
        super();
    }
    
    public AgentAuthenticationKey(UUID id, byte[] secret)
    {
        super();
        this.id = Objects.requireNonNull(id);
        this.secret = Objects.requireNonNull(secret);
    }
    
    public AgentAuthenticationKey(String encoded)
    {
        super();
        this.decode(encoded);
    }

    public UUID getId()
    {
        return this.id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public byte[] getSecret()
    {
        return this.secret;
    }

    public void setSecret(byte[] secret)
    {
        this.secret = secret;
    }
    
    public Mac createHMAC() throws InvalidKeyException, NoSuchAlgorithmException
    {
        Mac hmac = Mac.getInstance(HMAC_SHA256);
        hmac.init(new SecretKeySpec(this.secret, HMAC_SHA256));
        return hmac;
    }
    
    public byte[] sign(long timestamp, UUID agentId, String hostName, String templateName) throws InvalidKeyException, NoSuchAlgorithmException
    {
        Objects.requireNonNull(agentId);
        Objects.requireNonNull(hostName);
        if (templateName == null) templateName = "";
        Mac hmac = this.createHMAC();
        hmac.update(
            ByteBuffer.wrap(new byte[24])
            .putLong(timestamp)
            .putLong(agentId.getMostSignificantBits())
            .putLong(agentId.getLeastSignificantBits())
            .array()
        );
        hmac.update(hostName.getBytes(UTF8));
        hmac.update(templateName.getBytes(UTF8));
        return hmac.doFinal();
    }
    
    public String signBase64(long timestamp, UUID agentId, String hostName, String templateName) throws InvalidKeyException, NoSuchAlgorithmException
    {
        return Base64.encodeBase64URLSafeString(this.sign(timestamp, agentId, hostName, templateName));
    }
    
    public boolean check(long timestamp, UUID agentId, String hostName, String templateName, byte[] signature) throws InvalidKeyException, NoSuchAlgorithmException
    {
        return Arrays.equals(Objects.requireNonNull(signature), this.sign(timestamp, agentId, hostName, templateName));
    }
    
    public boolean checkBase64(long timestamp, UUID agentId, String hostName, String templateName, String signature) throws InvalidKeyException, NoSuchAlgorithmException
    {
        return this.check(timestamp, agentId, hostName, templateName, Base64.decodeBase64(signature));
    }
    
    public String encode()
    {
        Objects.requireNonNull(this.id);
        Objects.requireNonNull(this.secret);
        // Pack the id and secret
        ByteBuffer packed = ByteBuffer.wrap(new byte[2 + 16 + this.secret.length]);
        packed.put(MAGIC1);
        packed.put((byte) (MAGIC2 | VERSION_1));
        packed.putLong(id.getMostSignificantBits());
        packed.putLong(id.getLeastSignificantBits());
        packed.put(this.secret);
        // Encode it
        return Base64.encodeBase64URLSafeString(packed.array());
    }
    
    public AgentAuthenticationKey decode(String value)
    {
        Objects.requireNonNull(value);
        // Decode
        ByteBuffer packed = ByteBuffer.wrap(Base64.decodeBase64(value));
        // Unpack
        byte magic1 = packed.get();
        byte magic2 = packed.get();
        if (magic1 != MAGIC1 || ((magic2 & 0xF0) != (MAGIC2 & 0xF0))) throw new IllegalArgumentException("Got bad Bergamot Agent key magic");
        int version = magic2 & 0xF;
        if (version == VERSION_1)
        {
            this.id = new UUID(packed.getLong(), packed.getLong());
            this.secret = new byte[packed.remaining()];
            packed.get(this.secret);
        }
        else
        {
            throw new IllegalArgumentException("Unknown Bergamot Agent key version: " + version);
        }
        return this;
    }
    
    public String toString()
    {
        return this.encode();
    }
}

package com.intrbiz.bergamot.proxy.model;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AuthenticationKey
{
    public static final int VERSION_1 = 0x01;
    
    public static final byte MAGIC1 = (byte) 0x1B;
    
    public static final byte MAGIC2 = (byte) 0xB0;
    
    public static final String HMAC_SHA256 = "HmacSHA256";
    
    private UUID id;
    
    private UUID siteId;
    
    private byte[] secret;
    
    public AuthenticationKey()
    {
        super();
    }
    
    public AuthenticationKey(UUID id, UUID siteId, byte[] secret)
    {
        super();
        this.id = Objects.requireNonNull(id);
        this.siteId = siteId;
        this.secret = Objects.requireNonNull(secret);
    }
    
    public AuthenticationKey(String encoded)
    {
        super();
        this.decode(encoded);
    }

    public UUID getId()
    {
        return this.id;
    }
    
    public UUID getSiteId()
    {
        return this.siteId;
    }

    public byte[] getSecret()
    {
        return this.secret;
    }
    
    public Mac createHMAC() throws InvalidKeyException, NoSuchAlgorithmException
    {
        Mac hmac = Mac.getInstance(HMAC_SHA256);
        hmac.init(new SecretKeySpec(this.secret, HMAC_SHA256));
        return hmac;
    }
    
    public byte[] sign(long timestamp, UUID id, Map<String, String> attributes) throws InvalidKeyException, NoSuchAlgorithmException
    {
        Objects.requireNonNull(id);
        Mac hmac = this.createHMAC();
        hmac.update(
            ByteBuffer.wrap(new byte[24])
            .putLong(timestamp)
            .putLong(id.getMostSignificantBits())
            .putLong(id.getLeastSignificantBits())
            .array()
        );
        attributes.keySet().stream().sorted().forEach((key) -> {
            hmac.update(key.getBytes(StandardCharsets.UTF_8));
            String value = attributes.get(key);
            if (value != null) hmac.update(value.getBytes(StandardCharsets.UTF_8));
        });
        return hmac.doFinal();
    }
    
    public String signBase64(long timestamp, UUID id, Map<String, String> attributes) throws InvalidKeyException, NoSuchAlgorithmException
    {
        return Base64.encodeBase64URLSafeString(this.sign(timestamp, id, attributes));
    }
    
    public boolean check(long timestamp, UUID id, Map<String, String> attributes, byte[] signature) throws InvalidKeyException, NoSuchAlgorithmException
    {
        return Arrays.equals(Objects.requireNonNull(signature), this.sign(timestamp, id, attributes));
    }
    
    public boolean checkBase64(long timestamp, UUID id, Map<String, String> attributes, String signature) throws InvalidKeyException, NoSuchAlgorithmException
    {
        return this.check(timestamp, id, attributes, Base64.decodeBase64(signature));
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
    
    public AuthenticationKey decode(String value)
    {
        Objects.requireNonNull(value);
        // Decode
        ByteBuffer packed = ByteBuffer.wrap(Base64.decodeBase64(value));
        // Unpack
        byte magic1 = packed.get();
        byte magic2 = packed.get();
        if (magic1 != MAGIC1 || ((magic2 & 0xF0) != (MAGIC2 & 0xF0))) throw new IllegalArgumentException("Got bad Bergamot key magic");
        int version = magic2 & 0xF;
        if (version == VERSION_1)
        {
            this.id = new UUID(packed.getLong(), packed.getLong());
            this.secret = new byte[packed.remaining()];
            packed.get(this.secret);
        }
        else
        {
            throw new IllegalArgumentException("Unknown Bergamot key version: " + version);
        }
        return this;
    }
    
    public String toString()
    {
        return this.encode();
    }
}

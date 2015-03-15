package com.intrbiz.bergamot.crypto.util;

import java.security.PublicKey;

public class CertificateRequest
{
    private final String commonName;
    
    private final PublicKey key;
    
    public CertificateRequest(String commonName, PublicKey key)
    {
        this.commonName = commonName;
        this.key = key;
    }

    public String getCommonName()
    {
        return commonName;
    }

    public PublicKey getKey()
    {
        return key;
    }
}

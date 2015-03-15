package com.intrbiz.bergamot.crypto.util;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public final class CertificatePair
{
    private final X509Certificate certificate;
    
    private final PrivateKey key;
    
    public CertificatePair(X509Certificate certificate, PrivateKey key)
    {
        this.certificate = certificate;
        this.key = key;
    }
    
    public CertificatePair(String certificate, String key) throws IOException
    {
        this.certificate = (X509Certificate) PEMUtil.loadCertificate(certificate);
        this.key = PEMUtil.loadKey(key);
    }
    
    public CertificatePair(File certificate, File key) throws IOException
    {
        this.certificate = (X509Certificate) PEMUtil.loadCertificate(certificate);
        this.key = PEMUtil.loadKey(key);
    }

    public X509Certificate getCertificate()
    {
        return certificate;
    }
    
    public String getCertificateAsPEM()
    {
        return PEMUtil.saveCertificate(this.certificate);
    }
    
    public void saveCertificate(Writer to) throws IOException
    {
        PEMUtil.saveCertificate(this.certificate, to);
    }
    
    public void saveCertificate(File to) throws IOException
    {
        PEMUtil.saveCertificate(this.certificate, to);
    }

    public PrivateKey getKey()
    {
        return key;
    }
    
    public String getKeyAsPEM()
    {
        return PEMUtil.saveKey(this.key);
    }
    
    public void saveKey(Writer to) throws IOException
    {
        PEMUtil.saveKey(this.key, to);
    }
    
    public void saveKey(File to) throws IOException
    {
        PEMUtil.saveKey(this.key, to);
    }
    
    public String toString()
    {
        return PEMUtil.saveCertificate(this.certificate);
    }
}
package com.intrbiz.bergamot.crypto.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class PEMUtil
{
    public static Certificate loadCertificate(File file) throws IOException
    {
        try (FileInputStream in = new FileInputStream(file))
        {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate cert =  factory.generateCertificate(in);
            return cert;
        }
        catch (Exception e)
        {
            throw new IOException("Error loading certificate", e);
        }
    }
    
    public static PrivateKey loadKey(File file) throws IOException
    {
        try
        {
            // need to use some BC classes to parse PEM files
            // fecking Java, POS at times
            try (PemReader pr = new PemReader(new FileReader(file)))
            {
                PemObject obj = pr.readPemObject();
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PrivateKey key = kf.generatePrivate(new PKCS8EncodedKeySpec(obj.getContent()));
                return key;
            }
        }
        catch (Exception e)
        {
            throw new IOException("Error loading key", e);
        }
    }
}

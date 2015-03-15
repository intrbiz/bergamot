package com.intrbiz.bergamot.crypto.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class PEMUtil
{
    public static String saveCertificate(Certificate cert)
    {
        StringWriter sw = new StringWriter();
        try (PEMWriter pw = new PEMWriter(sw))
        {
            pw.writeObject(cert);
        }
        catch (IOException e)
        {
        }
        return sw.toString();
    }
    
    public static void saveCertificate(Certificate cert, Writer to) throws IOException
    {
        try (PEMWriter pw = new PEMWriter(to))
        {
            pw.writeObject(cert);
        }
    }
    
    public static void saveCertificate(Certificate cert, File to) throws IOException
    {
        try (PEMWriter pw = new PEMWriter(new FileWriter(to)))
        {
            pw.writeObject(cert);
        }
    }
    
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
    
    public static Certificate loadCertificate(String data) throws IOException
    {
        try (InputStream in = new ByteArrayInputStream(data.getBytes()))
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
    
    public static PrivateKey loadKey(String data) throws IOException
    {
        try
        {
            // need to use some BC classes to parse PEM files
            // fecking Java, POS at times
            try (PemReader pr = new PemReader(new StringReader(data)))
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
    
    public static String saveKey(PrivateKey key)
    {
        StringWriter sw = new StringWriter();
        try (PEMWriter pw = new PEMWriter(sw))
        {
            pw.writeObject(key);
        }
        catch (IOException e)
        {
        }
        return sw.toString();
    }
    
    public static void saveKey(PrivateKey key, Writer to) throws IOException
    {
        try (PEMWriter pw = new PEMWriter(to))
        {
            pw.writeObject(key);
        }
    }
    
    public static void saveKey(PrivateKey key, File to) throws IOException
    {
        try (PEMWriter pw = new PEMWriter(new FileWriter(to)))
        {
            pw.writeObject(key);
        }
    }
}

package com.intrbiz.bergamot.crypto.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class PEMUtil
{
    static
    {
        // as much as I dislike this, we need the BC provider to load a PEM formatted cert reliably!
        Security.addProvider(new BouncyCastleProvider());
    }
    
    public static String saveCertificate(Certificate cert)
    {
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter pw = new JcaPEMWriter(sw))
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
        try (JcaPEMWriter pw = new JcaPEMWriter(to))
        {
            pw.writeObject(cert);
        }
    }
    
    public static void saveCertificate(Certificate cert, File to) throws IOException
    {
        try (JcaPEMWriter pw = new JcaPEMWriter(new FileWriter(to)))
        {
            pw.writeObject(cert);
        }
    }
    
    public static Certificate loadCertificate(File file) throws IOException
    {
        try (PEMParser pr = new PEMParser(new FileReader(file)))
        {
            return (Certificate) pr.readObject();
        }
    }
    
    public static Certificate loadCertificate(String data) throws IOException
    {
        try (PEMParser pr = new PEMParser(new StringReader(data)))
        {
            return (Certificate) pr.readObject();
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
        try (JcaPEMWriter pw = new JcaPEMWriter(sw))
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
        try (JcaPEMWriter pw = new JcaPEMWriter(to))
        {
            pw.writeObject(key);
        }
    }
    
    public static void saveKey(PrivateKey key, File to) throws IOException
    {
        try (JcaPEMWriter pw = new JcaPEMWriter(new FileWriter(to)))
        {
            pw.writeObject(key);
        }
    }
    
    public static CertificateRequest loadCertificateRequest(Reader reader) throws IOException
    {
        try (PEMParser pr = new PEMParser(reader))
        {
            CertificationRequest req = (CertificationRequest) pr.readObject();
            // get the CN
            String cn = IETFUtils.valueToString(req.getCertificationRequestInfo().getSubject().getRDNs(BCStyle.CN)[0].getFirst().getValue());
            // build the key
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey key = kf.generatePublic(new RSAPublicKeySpec(
                    ((ASN1Integer)((DERSequence) req.getCertificationRequestInfo().getSubjectPublicKeyInfo().parsePublicKey()).getObjectAt(0)).getValue(), 
                    ((ASN1Integer)((DERSequence) req.getCertificationRequestInfo().getSubjectPublicKeyInfo().parsePublicKey()).getObjectAt(1)).getValue()
            ));
            return new CertificateRequest(cn, key);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            throw new IOException("Failed to parse certificate request", e);
        }
    }
    
    public static CertificateRequest loadCertificateRequest(File file) throws IOException
    {
        return loadCertificateRequest(new FileReader(file));
    }
    
    public static CertificateRequest loadCertificateRequest(String data) throws IOException
    {
        return loadCertificateRequest(new StringReader(data));
    }
    
    public static String savePublicKey(PublicKey key) throws IOException
    {
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter pw = new JcaPEMWriter(sw))
        {
            pw.writeObject(key);
        }
        return sw.toString();
    }
    
    public static void savePublicKey(PublicKey key, Writer to) throws IOException
    {
        try (JcaPEMWriter pw = new JcaPEMWriter(to))
        {
            pw.writeObject(key);
        }
    }
    
    public static void savePublicKey(PublicKey key, File to) throws IOException
    {
        savePublicKey(key, new FileWriter(to));
    }
    
    public static PublicKey loadPublicKey(Reader from) throws IOException
    {
        try
        {
            // need to use some BC classes to parse PEM files
            // fecking Java, POS at times
            try (PemReader pr = new PemReader(from))
            {
                // System.out.println(pr.readPemObject().getType());
                
                PemObject obj = pr.readPemObject();
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PublicKey key = kf.generatePublic(new X509EncodedKeySpec(obj.getContent()));
                return key;
            }
        }
        catch (Exception e)
        {
            throw new IOException("Error loading key", e);
        }
    }
    
    public static PublicKey loadPublicKey(String data) throws IOException
    {
        return loadPublicKey(new StringReader(data));
    }
    
    public static PublicKey loadPublicKey(File data) throws IOException
    {
        return loadPublicKey(new FileReader(data));
    }
}

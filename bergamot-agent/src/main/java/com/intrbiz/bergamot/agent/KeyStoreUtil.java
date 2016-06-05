package com.intrbiz.bergamot.agent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class KeyStoreUtil
{
    static
    {
        // as much as I dislike this, we need the BC provider to load a PEM formatted cert reliably!
        Security.addProvider(new BouncyCastleProvider());
    }
    
    public static KeyStore loadClientAuthKeyStore(String password, File clientKeyFile, File clientCertFile, File caCertFile) throws IOException
    {
        try
        {
            Certificate caCert     = loadCertificate(caCertFile);
            Certificate clientCert = loadCertificate(clientCertFile);
            Key         clientKey  = loadKey(clientKeyFile);
            // create the keystore
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, null);
            // add the ca
            ks.setCertificateEntry("ca", caCert);
            // add the client cert
            ks.setCertificateEntry("client", clientCert);
            // add the client key
            ks.setKeyEntry("client-key", clientKey, password.toCharArray(), new Certificate[] { clientCert, caCert });
            return ks;
        }
        catch (Exception e)
        {
            throw new IOException("Failed to create client auth keystore", e);
        }
    }
    
    public static KeyStore loadClientAuthKeyStore(String password, String clientKeyFileData, String clientCertFileData, String caCertFileData) throws IOException
    {
        try
        {
            Certificate caCert     = loadCertificate(caCertFileData);
            Certificate clientCert = loadCertificate(clientCertFileData);
            Key         clientKey  = loadKey(clientKeyFileData);
            // create the keystore
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, null);
            // add the ca
            ks.setCertificateEntry("ca", caCert);
            // add the client cert
            ks.setCertificateEntry("client", clientCert);
            // add the client key
            ks.setKeyEntry("client-key", clientKey, password.toCharArray(), new Certificate[] { clientCert, caCert });
            return ks;
        }
        catch (Exception e)
        {
            throw new IOException("Failed to create client auth keystore", e);
        }
    }
    
    public static KeyStore loadClientAuthKeyStore(String password, String clientKeyFileData, String clientCertFileData, String siteCaCertFileData, String caCertFileData) throws IOException
    {
        try
        {
            Certificate caCert     = loadCertificate(caCertFileData);
            Certificate siteCaCert = loadCertificate(siteCaCertFileData);
            Certificate clientCert = loadCertificate(clientCertFileData);
            Key         clientKey  = loadKey(clientKeyFileData);
            // create the keystore
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, null);
            // add the ca
            ks.setCertificateEntry("ca", caCert);
            // add the site ca
            ks.setCertificateEntry("site-ca", siteCaCert);
            // add the client cert
            ks.setCertificateEntry("client", clientCert);
            // add the client key
            ks.setKeyEntry("client-key", clientKey, password.toCharArray(), new Certificate[] { clientCert, siteCaCert, caCert });
            return ks;
        }
        catch (Exception e)
        {
            throw new IOException("Failed to create client auth keystore", e);
        }
    }
    
    public static KeyStore loadTrustKeyStore(File caCertFile) throws IOException
    {
        try
        {
            Certificate caCert     = loadCertificate(caCertFile);
            // create the keystore
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, null);
            // add the ca
            ks.setCertificateEntry("ca", caCert);
            return ks;
        }
        catch (Exception e)
        {
            throw new IOException("Failed to create trust keystore", e);
        }
    }
    
    public static KeyStore loadTrustKeyStore(String caCertFileData) throws IOException
    {
        try
        {
            Certificate caCert     = loadCertificate(caCertFileData);
            // create the keystore
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, null);
            // add the ca
            ks.setCertificateEntry("ca", caCert);
            return ks;
        }
        catch (Exception e)
        {
            throw new IOException("Failed to create trust keystore", e);
        }
    }
    
    public static PrivateKey loadKey(File file) throws IOException
    {
        try
        {
            // need to use some BC classes to parse PEM files
            // fecking Java, POS at times
            PemReader pr = new PemReader(new FileReader(file));
            try
            {
                PemObject obj = pr.readPemObject();
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PrivateKey key = kf.generatePrivate(new PKCS8EncodedKeySpec(obj.getContent()));
                return key;
            }
            finally
            {
                pr.close();
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
            PemReader pr = new PemReader(new StringReader(data));
            try
            {
                PemObject obj = pr.readPemObject();
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PrivateKey key = kf.generatePrivate(new PKCS8EncodedKeySpec(obj.getContent()));
                return key;
            }
            finally
            {
                pr.close();
            }
        }
        catch (Exception e)
        {
            throw new IOException("Error loading key", e);
        }
    }
    
    
    public static Certificate loadCertificate(File file) throws IOException
    {
        PEMReader pr = new PEMReader(new FileReader(file));
        try
        {
            return (Certificate) pr.readObject();
        }
        finally
        {
            pr.close();
        }
    }
    
    public static Certificate loadCertificate(String data) throws IOException
    {
        PEMReader pr = new PEMReader(new StringReader(data));
        try
        {
            return (Certificate) pr.readObject();
        }
        finally
        {
            pr.close();
        }
    }
    
    public static String savePublicKey(PublicKey key)
    {
        try
        {
            StringWriter sw = new StringWriter();
            PEMWriter pw = new PEMWriter(sw);
            try
            {
                pw.writeObject(key);
                return sw.toString();
            }
            finally
            {
                pw.close();
            }
        }
        catch (IOException e)
        {
        }
        return null;
    }
    
    public static String saveKey(PrivateKey key)
    {
        try
        {
            StringWriter sw = new StringWriter();
            PEMWriter pw = new PEMWriter(sw);
            try
            {
                pw.writeObject(key);
                return sw.toString();
            }
            finally
            {
                pw.close();
            }
        }
        catch (IOException e)
        {
        }
        return null;
    }
    
    public static String saveCertificate(Certificate cert)
    {
        try
        {
            StringWriter sw = new StringWriter();
            PEMWriter pw = new PEMWriter(sw);
            try
            {
                pw.writeObject(cert);
                return sw.toString();
            }
            finally
            {
                pw.close();
            }
        }
        catch (IOException e)
        {
        }
        return null;
    }
    
}

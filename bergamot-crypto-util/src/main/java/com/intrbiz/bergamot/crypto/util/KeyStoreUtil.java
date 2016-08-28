package com.intrbiz.bergamot.crypto.util;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;

public class KeyStoreUtil
{
    public static KeyStore loadServerKeyStore(String password, File clientKeyFile, File clientCertFile, File caCertFile) throws IOException
    {
        try
        {
            Certificate caCert     = PEMUtil.loadCertificate(caCertFile);
            Certificate clientCert = PEMUtil.loadCertificate(clientCertFile);
            Key         clientKey  = PEMUtil.loadKey(clientKeyFile);
            // create the keystore
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, null);
            // add the ca
            ks.setCertificateEntry("ca", caCert);
            // add the client cert
            ks.setCertificateEntry("server", clientCert);
            // add the client key
            ks.setKeyEntry("server-key", clientKey, password.toCharArray(), new Certificate[] { clientCert, caCert });
            return ks;
        }
        catch (Exception e)
        {
            throw new IOException("Failed to create server auth keystore", e);
        }
    }
    
    public static KeyStore loadServerKeyStore(String password, String clientKeyFileData, String clientCertFileData, String caCertFileData) throws IOException
    {
        try
        {
            Certificate caCert     = PEMUtil.loadCertificate(caCertFileData);
            Certificate clientCert = PEMUtil.loadCertificate(clientCertFileData);
            Key         clientKey  = PEMUtil.loadKey(clientKeyFileData);
            // create the keystore
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, null);
            // add the ca
            ks.setCertificateEntry("ca", caCert);
            // add the client cert
            ks.setCertificateEntry("server", clientCert);
            // add the client key
            ks.setKeyEntry("server-key", clientKey, password.toCharArray(), new Certificate[] { clientCert, caCert });
            return ks;
        }
        catch (Exception e)
        {
            throw new IOException("Failed to create server auth keystore", e);
        }
    }
    
    public static KeyStore loadClientAuthKeyStore(String password, File clientKeyFile, File clientCertFile, File caCertFile) throws IOException
    {
        try
        {
            Certificate caCert     = PEMUtil.loadCertificate(caCertFile);
            Certificate clientCert = PEMUtil.loadCertificate(clientCertFile);
            Key         clientKey  = PEMUtil.loadKey(clientKeyFile);
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
            Certificate caCert     = PEMUtil.loadCertificate(caCertFileData);
            Certificate clientCert = PEMUtil.loadCertificate(clientCertFileData);
            Key         clientKey  = PEMUtil.loadKey(clientKeyFileData);
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
            Certificate caCert     = PEMUtil.loadCertificate(caCertFileData);
            Certificate siteCaCert = PEMUtil.loadCertificate(siteCaCertFileData);
            Certificate clientCert = PEMUtil.loadCertificate(clientCertFileData);
            Key         clientKey  = PEMUtil.loadKey(clientKeyFileData);
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
            Certificate caCert     = PEMUtil.loadCertificate(caCertFile);
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
            Certificate caCert     = PEMUtil.loadCertificate(caCertFileData);
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
}

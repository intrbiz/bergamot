package com.intrbiz.bergamot.worker.check.http;

import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

/**
 * A customised trust manager which by default uses the Mozilla trust 
 * store and permits the suppression of invalid certificate errors
 */
public class BergamotTrustManager extends X509ExtendedTrustManager implements X509TrustManager
{
    private static final X509ExtendedTrustManager MOZILLA_TRUST_STORE = loadMozillaTrustStore();
    
    public static final X509ExtendedTrustManager getMozillaTrustStore()
    {
        return MOZILLA_TRUST_STORE;
    }

    /*
     * Load the Mozilla trust store that we bundle
     */
    private static final X509ExtendedTrustManager loadMozillaTrustStore()
    {
        try
        {
            InputStream trustStoreStream = HTTPChecker.class.getResourceAsStream("trust_store.jks");
            // Create our trust key store
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(trustStoreStream, "bergamot".toCharArray());
            // Create the trust manager
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(trustStore);
            // fecking obscured types are not helpful
            TrustManager[] managers = trustFactory.getTrustManagers();
            return (X509ExtendedTrustManager) managers[0];
        }
        catch (Exception e)
        {
            Logger.getLogger(BergamotTrustManager.class).fatal("Failed to load bundled Mozilla trust store!");
            throw new RuntimeException("Failed to load trust store");
        }
    }
    
    private Logger logger = Logger.getLogger(BergamotTrustManager.class);
    
    private final X509ExtendedTrustManager parent;
    
    private final boolean permitInvalid;
    
    public BergamotTrustManager(X509ExtendedTrustManager parent, boolean permitInvalid)
    {
        super();
        this.parent = parent;
        this.permitInvalid = permitInvalid;
    }
    
    public BergamotTrustManager(boolean permitInvalid)
    {
        this(getMozillaTrustStore(), permitInvalid);
    }
    
    public BergamotTrustManager()
    {
        this(false);
    }
    
    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException
    {
        try
        {
            this.parent.checkClientTrusted(certs, authType);
        }
        catch (CertificateException ce)
        {
            if (this.permitInvalid) logger.warn("Suppressing invalid certificate: " + ce.getMessage());
            else throw ce;
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType, Socket socket) throws CertificateException
    {
        try
        {
            this.parent.checkClientTrusted(certs, authType, socket);
        }
        catch (CertificateException ce)
        {
            if (this.permitInvalid) logger.warn("Suppressing invalid certificate: " + ce.getMessage());
            else throw ce;
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] certs, String authType, SSLEngine engine) throws CertificateException
    {
        try
        {
            this.parent.checkClientTrusted(certs, authType, engine);
        }
        catch (CertificateException ce)
        {
            if (this.permitInvalid) logger.warn("Suppressing invalid certificate: " + ce.getMessage());
            else throw ce;
        }
    }
    

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException
    {
        try
        {
            this.parent.checkServerTrusted(certs, authType);
        }
        catch (CertificateException ce)
        {
            if (this.permitInvalid) logger.warn("Suppressing invalid certificate: " + ce.getMessage());
            else throw ce;
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType, Socket socket) throws CertificateException
    {
        try
        {
            this.parent.checkServerTrusted(certs, authType, socket);
        }
        catch (CertificateException ce)
        {
            if (this.permitInvalid) logger.warn("Suppressing invalid certificate: " + ce.getMessage());
            else throw ce;
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String authType, SSLEngine engine) throws CertificateException
    {
        try
        {
            this.parent.checkServerTrusted(certs, authType, engine);
        }
        catch (CertificateException ce)
        {
            if (this.permitInvalid) logger.warn("Suppressing invalid certificate: " + ce.getMessage());
            else throw ce;
        }
    }
    
    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return this.parent == null ? new X509Certificate[0] : this.parent.getAcceptedIssuers();
    }
}

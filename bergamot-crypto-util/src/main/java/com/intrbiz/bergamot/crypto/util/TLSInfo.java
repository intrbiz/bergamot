package com.intrbiz.bergamot.crypto.util;

import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

/**
 * Information about the TLS aspects of the connection
 */
public class TLSInfo
{
    protected String host;

    protected int port;

    protected String protocol;

    protected String cipher;

    protected Principal principal;

    protected X509Certificate[] certificateChain;
    
    protected CertInfo[] certificateInfo;
    
    protected boolean validCertificate;
    
    protected CertificateException certificateValidationError;

    protected TLSInfo()
    {
        super();
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public String getCipher()
    {
        return cipher;
    }

    public Principal getPrincipal()
    {
        return principal;
    }

    public X509Certificate[] getCertificateChain()
    {
        return certificateChain;
    }

    public CertInfo[] getCertificateInfo()
    {
        return certificateInfo;
    }

    public X509Certificate getServerCertificate()
    {
        return this.certificateChain[0];
    }
    
    public CertInfo getServerCertInfo()
    {
        return this.certificateInfo[0];
    }
    
    public boolean isValidCertificate()
    {
        return validCertificate;
    }

    public CertificateException getCertificateValidationError()
    {
        return certificateValidationError;
    }

    public String toString()
    {
        return "tls-info {\n" +
                "protocol: " + this.protocol + "\n" +
                "cipher: " + this.cipher + "\n" +
                "host: " + this.host + "\n" +
                "port: " + this.port + "\n" +
                "cert: " + this.certificateInfo[0] + "\n" + 
                "valid: " + this.validCertificate + (this.certificateValidationError != null ? " (" + this.certificateValidationError.getMessage() + ")" : "") + "\n" +
               "}";
    }
    
    public static TLSInfo fromSSLEngine(SSLEngine engine)
    {
        return fromSSLSession(engine.getSession());
    }
    
    public static TLSInfo fromSSLSession(SSLSession session)
    {
        TLSInfo info = new TLSInfo();
        info.host = session.getPeerHost();
        info.port = session.getPeerPort();
        info.protocol = session.getProtocol();
        info.cipher = session.getCipherSuite();
        try
        {
            info.principal = session.getPeerPrincipal();
            // the cert chain
            Certificate[] certChain = session.getPeerCertificates();
            info.certificateChain = new X509Certificate[certChain.length];
            info.certificateInfo = new CertInfo[certChain.length];
            for (int i = 0 ; i < certChain.length; i++)
            {
                info.certificateChain[i] = (X509Certificate) certChain[i];
                info.certificateInfo[i] = CertInfo.fromX509Certificate(info.certificateChain[i]);
            }
            // validate the the cert chain with the Mozilla Trust Store
            try
            {
                info.validCertificate = false;
                X509TrustManager mozillaTrustStore = BergamotTrustManager.getMozillaTrustStore();
                mozillaTrustStore.checkServerTrusted(info.certificateChain, TLSConstants.getCipherInfo(info.getCipher()).getAuth());
                info.validCertificate = true;
            }
            catch (CertificateException e)
            {
                info.certificateValidationError = e;
            }
        }
        catch (Exception e)
        {
            Logger.getLogger(TLSInfo.class).error("Failed to build certificate info", e);
        }
        return info;
    }
}

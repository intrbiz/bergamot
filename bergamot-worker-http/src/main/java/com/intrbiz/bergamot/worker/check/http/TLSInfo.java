package com.intrbiz.bergamot.worker.check.http;

import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

/**
 * Information about the TLS aspects of the connection
 */
public class TLSInfo
{
    private String host;

    private int port;

    private String protocol;

    private String cipher;

    private Principal principal;

    private X509Certificate[] certificateChain;
    
    private CertInfo[] certificateInfo;
    
    private boolean validCertificate;
    
    private CertificateException certificateValidationError;

    public TLSInfo(SSLEngine engine) throws SSLPeerUnverifiedException
    {
        super();
        SSLSession session = engine.getSession();
        this.host = session.getPeerHost();
        this.port = session.getPeerPort();
        this.protocol = session.getProtocol();
        this.cipher = session.getCipherSuite();
        try
        {
            this.principal = session.getPeerPrincipal();
            // the cert chain
            Certificate[] certChain = session.getPeerCertificates();
            this.certificateChain = new X509Certificate[certChain.length];
            this.certificateInfo = new CertInfo[certChain.length];
            for (int i = 0 ; i < certChain.length; i++)
            {
                this.certificateChain[i] = (X509Certificate) certChain[i];
                this.certificateInfo[i] = new CertInfo(this.certificateChain[i]);
            }
            // validate the the cert chain with the Mozilla Trust Store
            try
            {
                this.validCertificate = false;
                X509TrustManager mozillaTrustStore = BergamotTrustManager.getMozillaTrustStore();
                mozillaTrustStore.checkServerTrusted(this.certificateChain, TLSConstants.getCipherInfo(this.getCipher()).getAuth());
                this.validCertificate = true;
            }
            catch (CertificateException e)
            {
                this.certificateValidationError = e;
            }
        }
        catch (Exception e)
        {
            Logger.getLogger(TLSInfo.class).error("Failed to build certificate info", e);
        }
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
}

package com.intrbiz.bergamot.nrpe.util;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;

public class NRPETLSContext
{
    public static final String[] CIPHERS = new String[] { "TLS_DH_anon_WITH_AES_128_CBC_SHA" };

    public static final String[] PROTOCOLS = new String[] { "TLSv1" };
    
    private static final String TLS = "TLS";
    
    private static final String TLS_DISABLED_ALGORITHMS = "jdk.tls.disabledAlgorithms";
    
    private static final String TLS_LEGACY_ALGORITHMS = "jdk.tls.legacyAlgorithms";

    private final SSLContext context;
    
    public NRPETLSContext()
    {
        String disabledTlsAlg = Security.getProperty(TLS_DISABLED_ALGORITHMS);
        String legacyTlsAlg = Security.getProperty(TLS_LEGACY_ALGORITHMS);
        try
        {
            // NRPE uses TLS DH ANON which is terribly insecure and disabled by default
            // enable it temporarily whilst we create a context
            Security.setProperty(TLS_DISABLED_ALGORITHMS, "");
            Security.setProperty(TLS_LEGACY_ALGORITHMS, "");
            this.context = SSLContext.getInstance(TLS);
            context.init(null, null, new SecureRandom());
        }
        catch (NoSuchAlgorithmException | KeyManagementException e)
        {
            throw new RuntimeException("Failed to init SSLEngine", e);
        }
        finally
        {
            if (disabledTlsAlg != null)
            {
                Security.setProperty(TLS_DISABLED_ALGORITHMS, disabledTlsAlg);
            }
            if (legacyTlsAlg != null)
            {
                Security.setProperty(TLS_LEGACY_ALGORITHMS, legacyTlsAlg);
            }
        }
    }
 
    public SSLEngine createSSLEngine()
    {
        SSLEngine sslEngine = this.context.createSSLEngine();
        sslEngine.setEnabledCipherSuites(CIPHERS);
        sslEngine.setEnabledProtocols(PROTOCOLS);
        sslEngine.setUseClientMode(true);
        return sslEngine;
    }
    
    public SSLSocket createSSLSocket() throws IOException
    {
        SSLSocket socket = (SSLSocket) this.context.getSocketFactory().createSocket();
        socket.setEnabledProtocols(PROTOCOLS);
        socket.setEnabledCipherSuites(CIPHERS);
        return socket;
    }
    
}

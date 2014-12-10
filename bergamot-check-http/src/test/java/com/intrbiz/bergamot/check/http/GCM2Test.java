package com.intrbiz.bergamot.check.http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

public class GCM2Test
{
    public static void main(String[] args) throws Exception
    {
        // Setup trust manager to ignore cert errors
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] { new BadTrustManager() }, new SecureRandom());
        // create SSL socket
        SSLSocketFactory fact = context.getSocketFactory();
        SSLSocket sock = (SSLSocket) fact.createSocket("sias.riskadvisory.net", 443);
        //
        System.out.println("Using CipherSuite: " + sock.getSession().getCipherSuite());
        // io
        Writer         out = new OutputStreamWriter(new BufferedOutputStream(sock.getOutputStream()));
        BufferedReader inp = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        // send the request
        out.write("GET /index.php/auth HTTP/1.1\r\n");
        out.write("Connection: close\r\n");
        out.write("Host: sias.riskadvisory.net\r\n");
        out.write("\r\n");
        out.flush();
        // read the response
        String l;
        while ((l = inp.readLine()) != null)
        {
            System.out.println(l);
        }
        sock.close();
    }
    
    public static class BadTrustManager extends X509ExtendedTrustManager implements X509TrustManager
    {        
        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException
        {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType, Socket socket) throws CertificateException
        {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType, SSLEngine engine) throws CertificateException
        {
        }
        

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException
        {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType, Socket socket) throws CertificateException
        {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType, SSLEngine engine) throws CertificateException
        {
        }
        
        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[0];
        }
    }
}

package com.intrbiz.bergamot.crypto.util;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.security.KeyPair;

import org.junit.Test;

import com.intrbiz.bergamot.crypto.util.RSAUtil.KeyType;

public class TestRSAUtil
{
    @Test
    public void testGenerateRSAKeyPair()
    {
        KeyPair pair = RSAUtil.generateRSAKeyPair(512);
        assertThat(pair, is(notNullValue()));
    }
    
    @Test
    public void testBuildDN()
    {
        String dn = RSAUtil.buildDN("GB", "SomeState", "SomeLocality", "MyOrg", "Basement", "testing.123");
        assertThat(dn, is(notNullValue()));
        assertThat(dn, is(equalTo("C=GB, ST=SomeState, L=SomeLocality, O=MyOrg, OU=Basement, CN=testing.123")));
    }
    
    @Test
    public void testGenerateCertificate() throws Exception
    {
        String rootDn = RSAUtil.buildDN("GB", "SomeState", "SomeLocality", "MyOrg", "Basement", "testing.root");
        // generate our root cert
        CertificatePair root = RSAUtil.generateCertificate(rootDn, SerialNum.randomSerialNum(), 365 * 15, 512, KeyType.CA, null);
        assertThat(root, is(notNullValue()));
        assertThat(root.getCertificate(), is(notNullValue()));
        assertThat(root.getKey(), is(notNullValue()));
        System.out.println("Root:\n" + root);
        // generate a server cert
        String serverDn = RSAUtil.buildDN("GB", "SomeState", "SomeLocality", "MyOrg", "Basement", "testing.server");
        CertificatePair server = RSAUtil.generateCertificate(serverDn, SerialNum.randomSerialNum(), 365 * 15, 512, KeyType.SERVER, root);
        assertThat(server, is(notNullValue()));
        assertThat(server.getCertificate(), is(notNullValue()));
        assertThat(server.getKey(), is(notNullValue()));
        server.getCertificate().verify(root.getCertificate().getPublicKey());
        System.out.println("Server:\n" + server);
        
    }
}

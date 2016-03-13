package com.intrbiz.bergamot.agent.manager;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyPair;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.intrbiz.bergamot.agent.manager.config.CertDNCfg;
import com.intrbiz.bergamot.agent.manager.signer.CertificateManager;
import com.intrbiz.bergamot.agent.manager.store.impl.FileKeyStore;
import com.intrbiz.bergamot.crypto.util.CertificatePair;
import com.intrbiz.bergamot.crypto.util.RSAUtil;
import com.intrbiz.bergamot.crypto.util.SerialNum;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestSigningWithFileKeyStore
{
    public static final UUID SITE_ID = UUID.fromString("ffcaf844-8592-4a47-91ef-5f0ab4fb3ce8");
    
    public static final String SITE_NAME = "bergamot.unit.test";
    
    public static final String SERVER_NAME = "hub.bergamot.unit.test";
    
    public static final UUID AGENT_ID = UUID.fromString("ec459826-d941-419f-8889-a5782609830d");
    
    public static final String AGENT_NAME = "agent.site.bergamot.unit.test";
    
public static final UUID TEMPLATE_ID = UUID.fromString("568dece2-0e27-474e-9473-bfbe4a3cbdda");
    
    public static final String TEMPLATE_NAME = "dummy_host_template";
    
    private static File base;
    
    private FileKeyStore keyStore;
    
    private CertDNCfg config;
    
    private CertificateManager certManager;
    
    @BeforeClass
    public static void setupBaseFile() throws IOException
    {
        base = new File(System.getProperty("java.io.tmpdir"), "test_file_key_store_" + System.currentTimeMillis() + "_base");
        base.mkdirs();
    }
    
    @Before
    public void setup()
    {
        this.keyStore = new FileKeyStore(base);
        this.config = new CertDNCfg();
        this.config.setCountry("GB");
        this.config.setState("Somewhere");
        this.config.setLocality("Sometown");
        this.config.setOrganisation("Somecompany");
        this.certManager = new CertificateManager(this.keyStore, this.config);
    }
    
    @AfterClass
    public static void cleanupFiles()
    {
        cleanup(base);
    }
    
    private static void cleanup(File file)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files != null)
            {
                for (File child : files)
                {
                    cleanup(child);
                }
            }
        }
        file.delete();
    }
    
    @Test
    public void test01SetupDirs()
    {
        assertThat(new File(base, "root").isDirectory(), is(equalTo(true)));
        assertThat(new File(base, "site").isDirectory(), is(equalTo(true)));
        assertThat(new File(base, "server").isDirectory(), is(equalTo(true)));
        assertThat(new File(base, "agent").isDirectory(), is(equalTo(true)));
    }
    
    @Test
    public void test02GenerateRootCA()
    {
        this.certManager.generateRootCA();
        assertThat(this.keyStore.hasRootCA(), is(equalTo(true)));
        // do the files exist
        assertThat(new File(new File(base, "root"), "ca.crt").exists(), is(equalTo(true)));
        assertThat(new File(new File(base, "root"), "ca.key").exists(), is(equalTo(true)));
        // load the cert
        CertificatePair caPair = this.keyStore.loadRootCA();
        assertThat(caPair, is(notNullValue()));
        assertThat(caPair.getCertificate(), is(notNullValue()));
        assertThat(caPair.getKey(), is(notNullValue()));
        assertThat(caPair.getCertificate().getSubjectDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=Bergamot Monitoring Root CA")));
        assertThat(caPair.getCertificate().getIssuerDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=Bergamot Monitoring Root CA")));
        assertThat(caPair.getCertificate().getPublicKey().getAlgorithm(), is(equalTo("RSA")));
        assertThat(caPair.getKey().getAlgorithm(), is(equalTo("RSA")));
    }
    
    @Test
    public void test03GenerateSiteCA()
    {
        this.certManager.generateSiteCA(SITE_ID, SITE_NAME);
        assertThat(this.keyStore.hasSiteCA(SITE_ID), is(equalTo(true)));
        // do the files exist
        assertThat(new File(new File(base, "site"), SITE_ID.toString() + ".crt").exists(), is(equalTo(true)));
        assertThat(new File(new File(base, "site"), SITE_ID.toString() + ".key").exists(), is(equalTo(true)));
        // load the cert
        CertificatePair sitePair = this.keyStore.loadSiteCA(SITE_ID);
        assertThat(sitePair, is(notNullValue()));
        assertThat(sitePair.getCertificate(), is(notNullValue()));
        assertThat(sitePair.getKey(), is(notNullValue()));
        assertThat(sitePair.getCertificate().getSubjectDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=bergamot.unit.test Site CA")));
        assertThat(sitePair.getCertificate().getIssuerDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=Bergamot Monitoring Root CA")));
    }
    
    @Test
    public void test04GenerateServer()
    {
        // generate RSA keypair
        KeyPair serverKeyPair = RSAUtil.generateRSAKeyPair(2048);
        SerialNum expectedSerial = SerialNum.fromName(SERVER_NAME);
        // sign
        this.certManager.signServer(SERVER_NAME, serverKeyPair.getPublic());
        assertThat(this.keyStore.hasServer(SERVER_NAME), is(equalTo(true)));
        // do the files exist
        assertThat(Files.isSymbolicLink(new File(new File(base, "server"), SERVER_NAME + ".crt").toPath()), is(equalTo(true)));
        assertThat(new File(new File(base, "server"), expectedSerial.getId().toString()).isDirectory(), is(equalTo(true)));
        assertThat(new File(new File(new File(base, "server"), expectedSerial.getId().toString()), expectedSerial.getId() + "." + expectedSerial.getRev() + ".crt").exists(), is(equalTo(true)));
        // load the cert
        CertificatePair serverCrt = this.keyStore.loadServer(SERVER_NAME);
        assertThat(serverCrt, is(notNullValue()));
        assertThat(serverCrt.getCertificate(), is(notNullValue()));
        assertThat(serverCrt.getKey(), is(nullValue()));
        assertThat(serverCrt.getCertificate().getSubjectDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=hub.bergamot.unit.test")));
        assertThat(serverCrt.getCertificate().getIssuerDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=Bergamot Monitoring Root CA")));
        assertThat(serverCrt.getCertificate().getPublicKey(), is(equalTo(serverKeyPair.getPublic())));
        assertThat(SerialNum.fromBigInt(serverCrt.getCertificate().getSerialNumber()), is(equalTo(expectedSerial)));
    }
    
    @Test
    public void test05RegenerateServer()
    {
        // precondition
        assertThat(this.keyStore.hasServer(SERVER_NAME), is(equalTo(true)));
        // generate RSA keypair
        KeyPair serverKeyPair = RSAUtil.generateRSAKeyPair(2048);
        SerialNum expectedSerial = SerialNum.fromName(SERVER_NAME).revision();
        // sign
        this.certManager.signServer(SERVER_NAME, serverKeyPair.getPublic());
        assertThat(this.keyStore.hasServer(SERVER_NAME), is(equalTo(true)));
        // do the files exist
        assertThat(Files.isSymbolicLink(new File(new File(base, "server"), SERVER_NAME + ".crt").toPath()), is(equalTo(true)));
        assertThat(new File(new File(base, "server"), expectedSerial.getId().toString()).isDirectory(), is(equalTo(true)));
        assertThat(new File(new File(new File(base, "server"), expectedSerial.getId().toString()), expectedSerial.getId() + "." + expectedSerial.getRev() + ".crt").exists(), is(equalTo(true)));
        // load the cert
        CertificatePair serverCrt = this.keyStore.loadServer(SERVER_NAME);
        assertThat(serverCrt, is(notNullValue()));
        assertThat(serverCrt.getCertificate(), is(notNullValue()));
        assertThat(serverCrt.getKey(), is(nullValue()));
        assertThat(serverCrt.getCertificate().getSubjectDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=hub.bergamot.unit.test")));
        assertThat(serverCrt.getCertificate().getIssuerDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=Bergamot Monitoring Root CA")));
        assertThat(serverCrt.getCertificate().getPublicKey(), is(equalTo(serverKeyPair.getPublic())));
        assertThat(SerialNum.fromBigInt(serverCrt.getCertificate().getSerialNumber()), is(equalTo(expectedSerial)));
    }
    
    @Test
    public void test06GenerateAgent()
    {
        // generate RSA keypair
        KeyPair agentKeyPair = RSAUtil.generateRSAKeyPair(2048);
        SerialNum expectedSerial = SerialNum.version2(AGENT_ID, 1, SerialNum.MODE_AGENT);
        // sign
        this.certManager.signAgent(SITE_ID, AGENT_ID, AGENT_NAME, agentKeyPair.getPublic());
        assertThat(this.keyStore.hasAgent(SITE_ID, AGENT_ID), is(equalTo(true)));
        // do the files exist
        assertThat(Files.isSymbolicLink(new File(new File(new File(base, "agent"), SITE_ID.toString()), AGENT_ID + ".crt").toPath()), is(equalTo(true)));
        assertThat(new File(new File(new File(base, "agent"), SITE_ID.toString()), expectedSerial.getId().toString()).isDirectory(), is(equalTo(true)));
        assertThat(new File(new File(new File(new File(base, "agent"), SITE_ID.toString()), expectedSerial.getId().toString()), expectedSerial.getId() + "." + expectedSerial.getRev() + ".crt").exists(), is(equalTo(true)));
        // load the cert
        CertificatePair agentCrt = this.keyStore.loadAgent(SITE_ID, AGENT_ID);
        assertThat(agentCrt, is(notNullValue()));
        assertThat(agentCrt.getCertificate(), is(notNullValue()));
        assertThat(agentCrt.getKey(), is(nullValue()));
        assertThat(agentCrt.getCertificate().getSubjectDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=agent.site.bergamot.unit.test")));
        assertThat(agentCrt.getCertificate().getIssuerDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=bergamot.unit.test Site CA")));
        assertThat(agentCrt.getCertificate().getPublicKey(), is(equalTo(agentKeyPair.getPublic())));
        assertThat(SerialNum.fromBigInt(agentCrt.getCertificate().getSerialNumber()), is(equalTo(expectedSerial)));
        assertThat(SerialNum.fromBigInt(agentCrt.getCertificate().getSerialNumber()).getId(), is(equalTo(AGENT_ID)));
    }
    
    @Test
    public void test07RegenerateAgent()
    {
        // precondition
        assertThat(this.keyStore.hasAgent(SITE_ID, AGENT_ID), is(equalTo(true)));
        // generate RSA keypair
        KeyPair agentKeyPair = RSAUtil.generateRSAKeyPair(2048);
        SerialNum expectedSerial = SerialNum.version2(AGENT_ID, 1, SerialNum.MODE_AGENT).revision();
        // sign
        this.certManager.signAgent(SITE_ID, AGENT_ID, AGENT_NAME, agentKeyPair.getPublic());
        assertThat(this.keyStore.hasAgent(SITE_ID, AGENT_ID), is(equalTo(true)));
        // do the files exist
        assertThat(Files.isSymbolicLink(new File(new File(new File(base, "agent"), SITE_ID.toString()), AGENT_ID + ".crt").toPath()), is(equalTo(true)));
        assertThat(new File(new File(new File(base, "agent"), SITE_ID.toString()), expectedSerial.getId().toString()).isDirectory(), is(equalTo(true)));
        assertThat(new File(new File(new File(new File(base, "agent"), SITE_ID.toString()), expectedSerial.getId().toString()), expectedSerial.getId() + "." + expectedSerial.getRev() + ".crt").exists(), is(equalTo(true)));
        // load the cert
        CertificatePair agentCrt = this.keyStore.loadAgent(SITE_ID, AGENT_ID);
        assertThat(agentCrt, is(notNullValue()));
        assertThat(agentCrt.getCertificate(), is(notNullValue()));
        assertThat(agentCrt.getKey(), is(nullValue()));
        assertThat(agentCrt.getCertificate().getSubjectDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=agent.site.bergamot.unit.test")));
        assertThat(agentCrt.getCertificate().getIssuerDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=bergamot.unit.test Site CA")));
        assertThat(agentCrt.getCertificate().getPublicKey(), is(equalTo(agentKeyPair.getPublic())));
        assertThat(SerialNum.fromBigInt(agentCrt.getCertificate().getSerialNumber()), is(equalTo(expectedSerial)));
        assertThat(SerialNum.fromBigInt(agentCrt.getCertificate().getSerialNumber()).getId(), is(equalTo(AGENT_ID)));
    }
    
    @Test
    public void test08GenerateTemplate()
    {
        // generate RSA keypair
        KeyPair agentKeyPair = RSAUtil.generateRSAKeyPair(2048);
        SerialNum expectedSerial = SerialNum.version2(TEMPLATE_ID, 1, SerialNum.MODE_TEMPLATE);
        // sign
        this.certManager.signTemplate(SITE_ID, TEMPLATE_ID, TEMPLATE_NAME, agentKeyPair.getPublic());
        assertThat(this.keyStore.hasTemplate(SITE_ID, TEMPLATE_ID), is(equalTo(true)));
        // do the files exist
        assertThat(Files.isSymbolicLink(new File(new File(new File(base, "template"), SITE_ID.toString()), TEMPLATE_ID + ".crt").toPath()), is(equalTo(true)));
        assertThat(new File(new File(new File(base, "template"), SITE_ID.toString()), expectedSerial.getId().toString()).isDirectory(), is(equalTo(true)));
        assertThat(new File(new File(new File(new File(base, "template"), SITE_ID.toString()), expectedSerial.getId().toString()), expectedSerial.getId() + "." + expectedSerial.getRev() + ".crt").exists(), is(equalTo(true)));
        // load the cert
        CertificatePair agentCrt = this.keyStore.loadTemplate(SITE_ID, TEMPLATE_ID);
        assertThat(agentCrt, is(notNullValue()));
        assertThat(agentCrt.getCertificate(), is(notNullValue()));
        assertThat(agentCrt.getKey(), is(nullValue()));
        assertThat(agentCrt.getCertificate().getSubjectDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=Template: dummy_host_template")));
        assertThat(agentCrt.getCertificate().getIssuerDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=bergamot.unit.test Site CA")));
        assertThat(agentCrt.getCertificate().getPublicKey(), is(equalTo(agentKeyPair.getPublic())));
        assertThat(SerialNum.fromBigInt(agentCrt.getCertificate().getSerialNumber()), is(equalTo(expectedSerial)));
        assertThat(SerialNum.fromBigInt(agentCrt.getCertificate().getSerialNumber()).getId(), is(equalTo(TEMPLATE_ID)));
    }
    
    @Test
    public void test09RegenerateTemplate()
    {
        // generate RSA keypair
        KeyPair agentKeyPair = RSAUtil.generateRSAKeyPair(2048);
        SerialNum expectedSerial = SerialNum.version2(TEMPLATE_ID, 1, SerialNum.MODE_TEMPLATE).revision();
        // sign
        this.certManager.signTemplate(SITE_ID, TEMPLATE_ID, TEMPLATE_NAME, agentKeyPair.getPublic());
        assertThat(this.keyStore.hasTemplate(SITE_ID, TEMPLATE_ID), is(equalTo(true)));
        // do the files exist
        assertThat(Files.isSymbolicLink(new File(new File(new File(base, "template"), SITE_ID.toString()), TEMPLATE_ID + ".crt").toPath()), is(equalTo(true)));
        assertThat(new File(new File(new File(base, "template"), SITE_ID.toString()), expectedSerial.getId().toString()).isDirectory(), is(equalTo(true)));
        assertThat(new File(new File(new File(new File(base, "template"), SITE_ID.toString()), expectedSerial.getId().toString()), expectedSerial.getId() + "." + expectedSerial.getRev() + ".crt").exists(), is(equalTo(true)));
        // load the cert
        CertificatePair agentCrt = this.keyStore.loadTemplate(SITE_ID, TEMPLATE_ID);
        assertThat(agentCrt, is(notNullValue()));
        assertThat(agentCrt.getCertificate(), is(notNullValue()));
        assertThat(agentCrt.getKey(), is(nullValue()));
        assertThat(agentCrt.getCertificate().getSubjectDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=Template: dummy_host_template")));
        assertThat(agentCrt.getCertificate().getIssuerDN().getName(), is(equalTo("C=GB,ST=Somewhere,L=Sometown,O=Somecompany,OU=Bergamot Monitoring,CN=bergamot.unit.test Site CA")));
        assertThat(agentCrt.getCertificate().getPublicKey(), is(equalTo(agentKeyPair.getPublic())));
        assertThat(SerialNum.fromBigInt(agentCrt.getCertificate().getSerialNumber()), is(equalTo(expectedSerial)));
        assertThat(SerialNum.fromBigInt(agentCrt.getCertificate().getSerialNumber()).getId(), is(equalTo(TEMPLATE_ID)));
    }
    
    @Test
    public void test10heck()
    {
        this.keyStore.check();
    }
    
    
}

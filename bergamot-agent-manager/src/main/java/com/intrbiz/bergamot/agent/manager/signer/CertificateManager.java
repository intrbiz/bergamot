package com.intrbiz.bergamot.agent.manager.signer;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.manager.config.CertDNCfg;
import com.intrbiz.bergamot.agent.manager.store.BergamotKeyStore;
import com.intrbiz.bergamot.crypto.util.CertificatePair;
import com.intrbiz.bergamot.crypto.util.RSAUtil;
import com.intrbiz.bergamot.crypto.util.RSAUtil.KeyType;
import com.intrbiz.bergamot.crypto.util.SerialNum;

public class CertificateManager
{
    private Logger logger = Logger.getLogger(CertificateManager.class);
    
    private final BergamotKeyStore keyStore;
    
    private final CertDNCfg certDN;
    
    public CertificateManager(BergamotKeyStore keyStore, CertDNCfg certDN)
    {
        this.keyStore = keyStore;
        this.certDN = certDN;
    }
    
    public String buildDN(String orgUnit, String commonName)
    {
        return RSAUtil.buildDN(
                this.certDN.getCountry(), 
                this.certDN.getState(), 
                this.certDN.getLocality(), 
                this.certDN.getOrganisation(), 
                orgUnit, 
                commonName
        );
    }
    
    public String buildRootCADN()
    {
        return this.buildDN("Bergamot Monitoring", "Bergamot Monitoring Root CA");
    }
    
    public String buildSiteCADN(String siteName)
    {
        return this.buildDN("Bergamot Monitoring", siteName + " Site CA");
    }
    
    public String buildDN(String commonName)
    {
        return this.buildDN("Bergamot Monitoring", commonName);
    }
    
    public void generateRootCA()
    {
        synchronized (this)
        {
            if (! this.keyStore.hasRootCA())
            {
                try
                {
                    logger.info("Generating Root CA: " + this.buildRootCADN());
                    // generate the root CA
                    CertificatePair root = RSAUtil.generateCertificate(this.buildRootCADN(), SerialNum.randomSerialNum(), 365 * 15, 4096, KeyType.CA, null);
                    // store
                    this.keyStore.storeRootCA(root);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Failed to generate Root CA", e);
                }
            }
        }
    }
    
    public Certificate generateSiteCA(UUID siteId, String siteName)
    {
        if (this.keyStore.hasSiteCA(siteId)) throw new RuntimeException("Certificate already exists for site: " + siteId);
        // first we need the root CA
        CertificatePair root = this.keyStore.loadRootCA();
        // generate the site CA
        try
        {
            logger.info("Generating Site CA: " + this.buildSiteCADN(siteName));
            // generate the site CA
            CertificatePair site = RSAUtil.generateCertificate(this.buildSiteCADN(siteName), new SerialNum(siteId, 1), 365 * 10, 2048, KeyType.INTERMEDIATE, root);
            // store
            this.keyStore.storeSiteCA(siteId, site);
            // return the cert
            return site.getCertificate();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to generate Site CA", e);
        }
    }
    
    public Certificate signAgent(UUID siteId, UUID agentId, String commonName, PublicKey key)
    {
        if (! this.keyStore.hasSiteCA(siteId)) throw new RuntimeException("No certificate exists for site: " + siteId);
        // do we already have a agent cert
        if (this.keyStore.hasAgent(siteId, agentId))
        {
            // load the current cert to get the serial number to revision
            CertificatePair currentCrt = this.keyStore.loadAgent(siteId, agentId);
            SerialNum currentSerial = SerialNum.fromBigInt(currentCrt.getCertificate().getSerialNumber());
            // we need the CA to sign
            CertificatePair site = this.keyStore.loadSiteCA(siteId);
            // sign the agent cert
            try
            {
                String dn = this.buildDN(commonName);
                logger.info("Signing Agent: " + siteId + "::" + agentId + " " + dn);
                // sign the agent
                CertificatePair agent = RSAUtil.generateCertificate(dn, currentSerial.revision(), 365 * 5, 2048, KeyType.CLIENT, key, site);
                // store
                this.keyStore.storeAgent(siteId, agentId, agent);
                // return the cert
                return agent.getCertificate();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to sign agent", e);
            }
        }
        else
        {
            // first we need the root CA
            CertificatePair site = this.keyStore.loadSiteCA(siteId);
            // sign the agent cert
            try
            {
                String dn = this.buildDN(commonName);
                logger.info("Signing Agent: " + siteId + "::" + agentId + " " + dn);
                // sign the agent
                CertificatePair agent = RSAUtil.generateCertificate(dn, SerialNum.version2(agentId, 1, SerialNum.MODE_AGENT), 365 * 5, 2048, KeyType.CLIENT, key, site);
                // store
                this.keyStore.storeAgent(siteId, agentId, agent);
                // return the cert
                return agent.getCertificate();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to sign agent", e);
            }
        }
    }
    
    public Certificate signTemplate(UUID siteId, UUID templateId, String templateName, PublicKey key)
    {
        if (! this.keyStore.hasSiteCA(siteId)) throw new RuntimeException("No certificate exists for site: " + siteId);
        // do we already have a agent cert
        if (this.keyStore.hasTemplate(siteId, templateId))
        {
            // load the current cert to get the serial number to revision
            CertificatePair currentCrt = this.keyStore.loadTemplate(siteId, templateId);
            SerialNum currentSerial = SerialNum.fromBigInt(currentCrt.getCertificate().getSerialNumber());
            // we need the CA to sign
            CertificatePair site = this.keyStore.loadSiteCA(siteId);
            // sign the agent cert
            try
            {
                String dn = this.buildDN("Template: " + templateName);
                logger.info("Signing Template: " + siteId + "::" + templateId + " " + dn);
                // sign the agent
                CertificatePair agent = RSAUtil.generateCertificate(dn, currentSerial.revision(), 365 * 5, 2048, KeyType.CLIENT, key, site);
                // store
                this.keyStore.storeTemplate(siteId, templateId, agent);
                // return the cert
                return agent.getCertificate();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to sign template", e);
            }
        }
        else
        {
            // first we need the root CA
            CertificatePair site = this.keyStore.loadSiteCA(siteId);
            // sign the agent cert
            try
            {
                String dn = this.buildDN("Template: " + templateName);
                logger.info("Signing Template: " + siteId + "::" + templateId + " " + dn);
                // sign the agent
                CertificatePair agent = RSAUtil.generateCertificate(dn, SerialNum.version2(templateId, 1, SerialNum.MODE_TEMPLATE), 365 * 5, 2048, KeyType.CLIENT, key, site);
                // store
                this.keyStore.storeTemplate(siteId, templateId, agent);
                // return the cert
                return agent.getCertificate();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to sign agent", e);
            }
        }
    }
    
    public Certificate signServer(String commonName, PublicKey key)
    {
        if (this.keyStore.hasServer(commonName))
        {
            // load the old cert to revision the serial
            CertificatePair current = this.keyStore.loadServer(commonName);
            SerialNum currentSerial = SerialNum.fromBigInt(current.getCertificate().getSerialNumber());
            // we need the root CA to sign
            CertificatePair root = this.keyStore.loadRootCA();
            // sign the cert incrementing the serial
            try
            {
                logger.info("Signing Server: " + commonName + " " + this.buildDN(commonName));
                // sign the agent
                CertificatePair server = RSAUtil.generateCertificate(this.buildDN(commonName), currentSerial.revision(), 365 * 5, 2048, KeyType.SERVER, key, root);
                // store
                this.keyStore.storeServer(commonName, server);
                // return the cert
                return server.getCertificate();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to sign agent", e);
            }
        }
        else
        {
            // first we need the root CA
            CertificatePair root = this.keyStore.loadRootCA();
            // sign the server cert
            try
            {
                logger.info("Signing Server: " + commonName + " " + this.buildDN(commonName));
                // sign the agent
                CertificatePair server = RSAUtil.generateCertificate(this.buildDN(commonName), SerialNum.fromName(commonName), 365 * 5, 2048, KeyType.SERVER, key, root);
                // store
                this.keyStore.storeServer(commonName, server);
                // return the cert
                return server.getCertificate();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to sign agent", e);
            }
        }
    }
}

package com.intrbiz.bergamot.agent.manager.store.impl;

import java.io.File;
import java.util.UUID;

import com.intrbiz.bergamot.agent.manager.store.BergamotKeyStore;
import com.intrbiz.bergamot.crypto.util.CertificatePair;

/**
 * A simple, low security file based key store, which 
 * stores the certificates and keys in raw, unprotected 
 * PEM format.
 * 
 * If using this key store, it is suggested to use an 
 * encrypted file system.
 * 
 */
public class FileKeyStore implements BergamotKeyStore
{
    private final File base;
    
    private final File root;
    
    private final File site;
    
    private final File server;
    
    private final File agent;
    
    public FileKeyStore(File base)
    {
        this.base   = base;
        this.root   = new File(this.base, "root");
        this.site   = new File(this.base, "site");
        this.server = new File(this.base, "server");
        this.agent  = new File(this.base, "agent");
        // ensure dirs exists
        if (! this.base.exists())   this.base.mkdirs();
        if (! this.root.exists())   this.root.mkdirs();
        if (! this.site.exists())   this.site.mkdirs();
        if (! this.server.exists()) this.server.mkdirs();
        if (! this.agent.exists())  this.agent.mkdirs();
    }
    
    @Override
    public boolean hasRootCA()
    {
        return new File(this.root, "ca.crt").exists() || new File(this.root, "ca.key").exists();
    }

    @Override
    public CertificatePair loadRootCA()
    {
        try
        {
            return new CertificatePair(new File(this.root, "ca.crt"), new File(this.root, "ca.key"));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load Root CA", e);
        }
    }

    @Override
    public void storeRootCA(CertificatePair pair)
    {
        synchronized (this)
        {
            if (this.hasRootCA()) throw new RuntimeException("Root CA already exists, not overstoring!");
            try
            {
                pair.saveCertificate(new File(this.root, "ca.crt"));
                pair.saveKey(new File(this.root, "ca.key"));
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to store Root CA", e);
            }
        }
    }
    
    @Override
    public boolean hasSiteCA(UUID siteId)
    {
        return new File(this.site, siteId + ".crt").exists() || new File(this.site, siteId + ".key").exists();   
    }

    @Override
    public CertificatePair loadSiteCA(UUID siteId)
    {
        try
        {
            return new CertificatePair(new File(this.site, siteId + ".crt"), new File(this.site, siteId + ".key"));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load Site CA for site: " + siteId, e);
        }
    }

    @Override
    public void storeSiteCA(UUID siteId, CertificatePair pair)
    {
        synchronized (this)
        {
            if (this.hasSiteCA(siteId)) throw new RuntimeException("Site CA already exists for site " + siteId + ", not overstoring!");
            try
            {
                pair.saveCertificate(new File(this.site, siteId + ".crt"));
                pair.saveKey(new File(this.site, siteId + ".key"));
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to store Site CA for site: " + siteId, e);
            }
        }
    }

    @Override
    public boolean hasAgent(UUID siteId, UUID agentId)
    {
        return new File(new File(this.agent, siteId.toString()), agentId + ".crt").exists();
    }

    @Override
    public CertificatePair loadAgent(UUID siteId, UUID agentId)
    {
        try
        {
            if (new File(new File(this.agent, siteId.toString()), agentId + ".key").exists())
            {
                return new CertificatePair(new File(new File(this.agent, siteId.toString()), agentId + ".crt"), new File(new File(this.agent, siteId.toString()), agentId + ".key"));
            }
            else
            {
                return new CertificatePair(new File(new File(this.agent, siteId.toString()), agentId + ".crt"), null);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load certificate for agent: " + siteId + "::" + agentId, e);
        }
    }

    @Override
    public void storeAgent(UUID siteId, UUID agentId, CertificatePair pair)
    {
        synchronized (this)
        {
            if (this.hasAgent(siteId, agentId)) throw new RuntimeException("Agent certificate already exists for agent " + siteId + "::" + agentId);
            new File(this.agent, siteId.toString()).mkdirs();
            try
            {
                pair.saveCertificate(new File(new File(this.agent, siteId.toString()), agentId + ".crt"));
                if (pair.getKey() != null) pair.saveKey(new File(new File(this.agent, siteId.toString()), agentId + ".key"));
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to store certificate for agent: " + siteId + "::" + agentId, e);
            }
        }
    }

    @Override
    public boolean hasServer(String commonName)
    {
        return new File(this.server, commonName + ".crt").exists();
    }

    @Override
    public CertificatePair loadServer(String commonName)
    {
        try
        {
            if (new File(this.server, commonName + ".key").exists())
            {
                return new CertificatePair(new File(this.server, commonName + ".crt"), new File(this.server, commonName + ".key"));
            }
            else
            {
                return new CertificatePair(new File(this.server, commonName + ".crt"), null);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load certificate for server: " + commonName, e);
        }
    }

    @Override
    public void storeServer(String commonName, CertificatePair pair)
    {
        synchronized (this)
        {
            if (this.hasServer(commonName)) throw new RuntimeException("Server certificate already exists for server " + commonName);
            try
            {
                pair.saveCertificate(new File(this.server, commonName + ".crt"));
                if (pair.getKey() != null) pair.saveKey(new File(this.server, commonName + ".key"));
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to store server certificate: " + commonName, e);
            }
        }
    }
}

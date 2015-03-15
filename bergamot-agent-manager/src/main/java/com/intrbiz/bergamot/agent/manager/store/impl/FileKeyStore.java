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
    
    
}

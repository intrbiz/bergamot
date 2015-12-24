package com.intrbiz.bergamot.agent.manager.store.impl;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

import com.intrbiz.bergamot.agent.manager.store.BergamotKeyStore;
import com.intrbiz.bergamot.crypto.util.CertificatePair;
import com.intrbiz.bergamot.crypto.util.SerialNum;

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
    
    private File agentCrtFile(UUID siteId, UUID agentId)
    {
        return new File(new File(this.agent, siteId.toString()), agentId + ".crt");
    }
    
    private File agentCrtFile(UUID siteId, SerialNum agentSerial)
    {
        return new File(new File(this.agent, siteId.toString()), agentSerial.getId() + "." + agentSerial.getRev() + ".crt");
    }
    
    private File agentKeyFile(UUID siteId, UUID agentId)
    {
        return new File(new File(this.agent, siteId.toString()), agentId + ".key");
    }
    
    private File agentKeyFile(UUID siteId, SerialNum agentSerial)
    {
        return new File(new File(this.agent, siteId.toString()), agentSerial.getId() + "." + agentSerial.getRev() + ".key");
    }

    @Override
    public boolean hasAgent(UUID siteId, UUID agentId)
    {
        return this.agentCrtFile(siteId, agentId).exists();
    }

    @Override
    public CertificatePair loadAgent(UUID siteId, UUID agentId)
    {
        try
        {
            if (this.agentKeyFile(siteId, agentId).exists())
            {
                return new CertificatePair(this.agentCrtFile(siteId, agentId), this.agentKeyFile(siteId, agentId));
            }
            else
            {
                return new CertificatePair(this.agentCrtFile(siteId, agentId), null);
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
            new File(this.agent, siteId.toString()).mkdirs();
            try
            {
                SerialNum serial = SerialNum.fromBigInt(pair.getCertificate().getSerialNumber());
                // store the certificate under the serial number
                File crtFile = this.agentCrtFile(siteId, serial);
                pair.saveCertificate(crtFile);
                // link the certificate to the agent
                Files.createSymbolicLink(this.agentCrtFile(siteId, agentId).toPath(), crtFile.toPath());
                // key?
                if (pair.getKey() != null)
                {
                    // store the key under the serial number
                    File keyFile = this.agentKeyFile(siteId, serial);
                    pair.saveKey(keyFile);
                    // link the key to the agent
                    Files.createSymbolicLink(this.agentKeyFile(siteId, agentId).toPath(), keyFile.toPath());
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to store certificate for agent: " + siteId + "::" + agentId, e);
            }
        }
    }
    
    private File serverCrtFile(String commonName)
    {
        return new File(this.server, commonName + ".crt");
    }
    
    private File serverCrtFile(SerialNum serverSerial)
    {
        return new File(this.server, serverSerial.getId() + "." + serverSerial.getRev() + ".crt");
    }
    
    private File serverKeyFile(String commonName)
    {
        return new File(this.server, commonName + ".crt");
    }
    
    private File serverKeyFile(SerialNum serverSerial)
    {
        return new File(this.server, serverSerial.getId() + "." + serverSerial.getRev() + ".key");
    }

    @Override
    public boolean hasServer(String commonName)
    {
        return this.serverCrtFile(commonName).exists();
    }

    @Override
    public CertificatePair loadServer(String commonName)
    {
        try
        {
            if (this.serverKeyFile(commonName).exists())
            {
                return new CertificatePair(this.serverCrtFile(commonName), this.serverKeyFile(commonName));
            }
            else
            {
                return new CertificatePair(this.serverCrtFile(commonName), null);
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
            try
            {
                SerialNum serial = SerialNum.fromBigInt(pair.getCertificate().getSerialNumber());
                // store and link the certificate
                File crtFile = this.serverCrtFile(serial);
                pair.saveCertificate(crtFile);
                Files.createSymbolicLink(this.serverCrtFile(commonName).toPath(), crtFile.toPath());
                // store and link the key
                if (pair.getKey() != null)
                {
                    File keyFile = this.serverKeyFile(serial);
                    pair.saveKey(keyFile);
                    Files.createSymbolicLink(this.serverKeyFile(commonName).toPath(), keyFile.toPath());
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to store server certificate: " + commonName, e);
            }
        }
    }
}

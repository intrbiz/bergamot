package com.intrbiz.bergamot.agent.manager.store.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.apache.log4j.Logger;

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
 * On disk layout
 * 
 * base/
 *     /root/
 *          /ca.crt
 *          /ca.key
 *     /site/
 *          /<site_uuid>.crt
 *          /<site_uuid>.key
 *     /server/
 *            /<crt_uuid>/
 *                       /<crt_uuid>.<rev>.crt
 *                       /<crt_uuid>.<rev>.key
 *            /<common_name>.crt -> /server/<crt_uuid>/<crt_uuid>.<rev>.crt
 *            /<common_name>.key -> /server/<crt_uuid>/<crt_uuid>.<rev>.key
 *     /agent/
 *           /<crt_uuid>/
 *                      /<crt_uuid>.<rev>.crt
 *                      /<crt_uuid>.<rev>.key
 *           /<agent_uuid>.crt -> /agent/<crt_uuid>/<crt_uuid>.<rev>.crt
 *           /<agent_uuid>.key -> /agent/<crt_uuid>/<crt_uuid>.<rev>.key
 *     /template/
 *           /<crt_uuid>/
 *                      /<crt_uuid>.<rev>.crt
 *                      /<crt_uuid>.<rev>.key
 *           /<template_uuid>.crt -> /template/<crt_uuid>/<crt_uuid>.<rev>.crt
 *           /<template_uuid>.key -> /template/<crt_uuid>/<crt_uuid>.<rev>.key
 * 
 */
public class FileKeyStore implements BergamotKeyStore
{
    private Logger logger = Logger.getLogger(FileKeyStore.class);
    
    private final File base;
    
    private final File root;
    
    private final File site;
    
    private final File server;
    
    private final File agent;
    
    private final File template;
    
    public FileKeyStore(File base)
    {
        this.base   = base;
        this.root   = new File(this.base, "root");
        this.site   = new File(this.base, "site");
        this.server = new File(this.base, "server");
        this.agent  = new File(this.base, "agent");
        this.template  = new File(this.base, "template");
        // ensure dirs exists
        if (! this.base.exists())   this.base.mkdirs();
        if (! this.root.exists())   this.root.mkdirs();
        if (! this.site.exists())   this.site.mkdirs();
        if (! this.server.exists()) this.server.mkdirs();
        if (! this.agent.exists())  this.agent.mkdirs();
        if (! this.template.exists())  this.template.mkdirs();
    }
    
    /**
     * Check this keystore structure is ok
     */
    public void check()
    {
        // validate the root
        if (! this.hasRootCA())
        {
            logger.error("No root CA keypair exists");
        }
        // sites
        File[] sites = this.site.listFiles();
        if (sites != null)
        {
            for (File site : sites)
            {
                if (site.isFile() && site.getName().endsWith(".crt"))
                {
                    try
                    {
                        UUID siteId = UUID.fromString(site.getName().replace(".crt", ""));
                        if (! this.hasSiteCA(siteId))
                        {
                            logger.error("No site CA keypair exists for site " + siteId);
                        }
                    }
                    catch (IllegalArgumentException e)
                    {
                    }
                }
            }
        }
        // servers
        File[] servers = this.server.listFiles();
        if (servers != null)
        {
            for (File server : servers)
            {
                if ((! Files.isSymbolicLink(server.toPath())) && (! server.isDirectory()) && server.getName().endsWith(".crt"))
                {
                    try
                    {
                        // get the common name
                        String commonName = server.getName().replace(".crt", "");
                        // looks like we have a regular file, convert to links
                        logger.info("Migrating server " + commonName + " into V2 file layout");
                        // migrate
                        CertificatePair crtPair = new CertificatePair(server, null);
                        SerialNum crtSerial = SerialNum.fromBigInt(crtPair.getCertificate().getSerialNumber());
                        File crtFile = this.serverCrtFile(crtSerial);
                        File keyFile = this.serverKeyFile(crtSerial);
                        File crtLink = this.serverCrtFile(commonName);
                        File keyLink = this.serverKeyFile(commonName);
                        // move stuff
                        crtFile.getParentFile().mkdirs();
                        Files.move(server.toPath(), crtFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        Files.createSymbolicLink(crtLink.toPath(), crtFile.toPath());
                        if (keyLink.exists())
                        {
                            keyFile.getParentFile().mkdirs();
                            Files.move(keyLink.toPath(), keyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            Files.createSymbolicLink(keyLink.toPath(), keyFile.toPath());
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("Failed to migrate server " + server.getName() + " to V2 file layout");
                    }
                }
            }
        }
        // agents
        File[] agentSites = this.agent.listFiles();
        if (agentSites != null)
        {
            for (File agentSite : agentSites)
            {
                if (agentSite.isDirectory())
                {
                    try
                    {
                        UUID agentSiteId = UUID.fromString(agentSite.getName());
                        File[] agents = agentSite.listFiles();
                        if (agents != null)
                        {
                            for (File agent : agents)
                            {
                                if ((! Files.isSymbolicLink(agent.toPath())) && (! agent.isDirectory()) && agent.getName().endsWith(".crt"))
                                {
                                    try
                                    {
                                        // get the agent id
                                        UUID agentId = UUID.fromString(agent.getName().replace(".crt", ""));
                                        logger.info("Migrating agent " + agentId + " into V2 file layout");
                                        // migrate
                                        CertificatePair crtPair = new CertificatePair(agent, null);
                                        SerialNum crtSerial = SerialNum.fromBigInt(crtPair.getCertificate().getSerialNumber());
                                        File crtFile = this.agentCrtFile(agentSiteId, crtSerial);
                                        File keyFile = this.agentKeyFile(agentSiteId, crtSerial);
                                        File crtLink = this.agentCrtFile(agentSiteId, agentId);
                                        File keyLink = this.agentKeyFile(agentSiteId, agentId);
                                        // move stuff
                                        crtFile.getParentFile().mkdirs();
                                        Files.move(server.toPath(), crtFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        Files.createSymbolicLink(crtLink.toPath(), crtFile.toPath());
                                        if (keyLink.exists())
                                        {
                                            keyFile.getParentFile().mkdirs();
                                            Files.move(keyLink.toPath(), keyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                            Files.createSymbolicLink(keyLink.toPath(), keyFile.toPath());
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        logger.error("Failed to migrate agent " + agent.getName() + " to V2 file layout");
                                    }
                                }
                            }
                        }
                    }
                    catch (IllegalArgumentException e)
                    {
                    }
                }
            }
        }
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
        return new File(new File(new File(this.agent, siteId.toString()), agentSerial.getId().toString()), agentSerial.getId() + "." + agentSerial.getRev() + ".crt");
    }
    
    private File agentKeyFile(UUID siteId, UUID agentId)
    {
        return new File(new File(this.agent, siteId.toString()), agentId + ".key");
    }
    
    private File agentKeyFile(UUID siteId, SerialNum agentSerial)
    {
        return new File(new File(new File(this.agent, siteId.toString()), agentSerial.getId().toString()), agentSerial.getId() + "." + agentSerial.getRev() + ".key");
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
                return new CertificatePair(this.agentCrtFile(siteId, agentId));
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
            try
            {
                SerialNum serial = SerialNum.fromBigInt(pair.getCertificate().getSerialNumber());
                // store the certificate under the serial number
                File crtFile = this.agentCrtFile(siteId, serial);
                crtFile.getParentFile().mkdirs();
                pair.saveCertificate(crtFile);
                // link the certificate to the agent
                Path crtLink = this.agentCrtFile(siteId, agentId).toPath();
                try
                {
                    Files.delete(crtLink);
                }
                catch (NoSuchFileException e)
                {
                }
                Files.createSymbolicLink(crtLink, crtFile.toPath());
                // key?
                if (pair.getKey() != null)
                {
                    // store the key under the serial number
                    File keyFile = this.agentKeyFile(siteId, serial);
                    keyFile.getParentFile().mkdirs();
                    pair.saveKey(keyFile);
                    // link the key to the agent
                    Path keyLink = this.agentKeyFile(siteId, agentId).toPath();
                    try
                    {
                        Files.delete(keyLink);
                    }
                    catch (NoSuchFileException e)
                    {
                    }
                    Files.createSymbolicLink(keyLink, keyFile.toPath());
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
        return new File(new File(this.server, serverSerial.getId().toString()), serverSerial.getId() + "." + serverSerial.getRev() + ".crt");
    }
    
    private File serverKeyFile(String commonName)
    {
        return new File(this.server, commonName + ".key");
    }
    
    private File serverKeyFile(SerialNum serverSerial)
    {
        return new File(new File(this.server, serverSerial.getId().toString()), serverSerial.getId() + "." + serverSerial.getRev() + ".key");
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
                return new CertificatePair(this.serverCrtFile(commonName));
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
                crtFile.getParentFile().mkdirs();
                pair.saveCertificate(crtFile);
                Path crtLink = this.serverCrtFile(commonName).toPath();
                try
                {
                    Files.delete(crtLink);
                }
                catch (NoSuchFileException e)
                {
                }
                Files.createSymbolicLink(crtLink, crtFile.toPath());
                // store and link the key
                if (pair.getKey() != null)
                {
                    File keyFile = this.serverKeyFile(serial);
                    keyFile.getParentFile().mkdirs();
                    pair.saveKey(keyFile);
                    Path keyLink = this.serverKeyFile(commonName).toPath();
                    try
                    {
                        Files.delete(keyLink);
                    }
                    catch (NoSuchFileException e)
                    {
                    }
                    Files.createSymbolicLink(keyLink, keyFile.toPath());
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to store server certificate: " + commonName, e);
            }
        }
    }
    
    private File templateCrtFile(UUID siteId, UUID templateId)
    {
        return new File(new File(this.template, siteId.toString()), templateId + ".crt");
    }
    
    private File templateCrtFile(UUID siteId, SerialNum templateSerial)
    {
        return new File(new File(new File(this.template, siteId.toString()), templateSerial.getId().toString()), templateSerial.getId() + "." + templateSerial.getRev() + ".crt");
    }
    
    private File templateKeyFile(UUID siteId, UUID templateId)
    {
        return new File(new File(this.template, siteId.toString()), templateId + ".key");
    }
    
    private File templateKeyFile(UUID siteId, SerialNum templateSerial)
    {
        return new File(new File(new File(this.template, siteId.toString()), templateSerial.getId().toString()), templateSerial.getId() + "." + templateSerial.getRev() + ".key");
    }
    
    @Override
    public boolean hasTemplate(UUID siteId, UUID templateId)
    {
        return this.templateCrtFile(siteId, templateId).exists();
    }

    @Override
    public CertificatePair loadTemplate(UUID siteId, UUID templateId)
    {
        try
        {
            if (this.templateKeyFile(siteId, templateId).exists())
            {
                return new CertificatePair(this.templateCrtFile(siteId, templateId), this.templateKeyFile(siteId, templateId));
            }
            else
            {
                return new CertificatePair(this.templateCrtFile(siteId, templateId));
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load certificate for template: " + siteId + "::" + templateId, e);
        }
    }

    @Override
    public void storeTemplate(UUID siteId, UUID templateId, CertificatePair pair)
    {
        synchronized (this)
        {
            try
            {
                SerialNum serial = SerialNum.fromBigInt(pair.getCertificate().getSerialNumber());
                // store the certificate under the serial number
                File crtFile = this.templateCrtFile(siteId, serial);
                crtFile.getParentFile().mkdirs();
                pair.saveCertificate(crtFile);
                // link the certificate to the template
                Path crtLink = this.templateCrtFile(siteId, templateId).toPath();
                try
                {
                    Files.delete(crtLink);
                }
                catch (NoSuchFileException e)
                {
                }
                Files.createSymbolicLink(crtLink, crtFile.toPath());
                // key?
                if (pair.getKey() != null)
                {
                    // store the key under the serial number
                    File keyFile = this.templateKeyFile(siteId, serial);
                    keyFile.getParentFile().mkdirs();
                    pair.saveKey(keyFile);
                    // link the key to the template
                    Path keyLink = this.templateKeyFile(siteId, templateId).toPath();
                    try
                    {
                        Files.delete(keyLink);
                    }
                    catch (NoSuchFileException e)
                    {
                    }
                    Files.createSymbolicLink(keyLink, keyFile.toPath());
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to store certificate for template: " + siteId + "::" + templateId, e);
            }
        }
    }
}

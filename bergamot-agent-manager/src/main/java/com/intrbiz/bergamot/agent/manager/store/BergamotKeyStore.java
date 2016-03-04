package com.intrbiz.bergamot.agent.manager.store;

import java.util.UUID;

import com.intrbiz.bergamot.crypto.util.CertificatePair;


/**
 * Store key and certificate material
 */
public interface BergamotKeyStore
{
    /**
     * Sanity check this keystore
     */
    void check();
    
    /**
     * Do we have a Root CA stored
     */
    boolean hasRootCA();
    
    /**
     * Load the Root CA certificate pair
     */
    CertificatePair loadRootCA();
    
    /**
     * Store the Root CA certificate pair
     */
    void storeRootCA(CertificatePair pair);
    
    /**
     * Do we have a Site CA stored for the given site
     */
    boolean hasSiteCA(UUID siteId);
    
    /**
     * Load the Site CA certificate pair
     */
    CertificatePair loadSiteCA(UUID siteId);
    
    /**
     * Store the given certificate pair as the CA for the given site
     */
    void storeSiteCA(UUID siteId, CertificatePair pair);
    
    /**
     * Do we have an agent stored for the given site and id
     */
    boolean hasAgent(UUID siteId, UUID agentId);
    
    /**
     * Load the agent certificate, private key is optional
     */
    CertificatePair loadAgent(UUID siteId, UUID agentId);
    
    /**
     * Store the given agent certificate, private key is optional
     */
    void storeAgent(UUID siteId, UUID agentId, CertificatePair pair);
    
    /**
     * Do we have an template stored for the given site and id
     */
    boolean hasTemplate(UUID siteId, UUID templateId);
    
    /**
     * Load the template certificate, private key is optional
     */
    CertificatePair loadTemplate(UUID siteId, UUID templateId);
    
    /**
     * Store the given template certificate, private key is optional
     */
    void storeTemplate(UUID siteId, UUID templateId, CertificatePair pair);
    
    /**
     * Do we have a server stored for the given site and name
     */
    boolean hasServer(String commonName);
    
    /**
     * Load the server certificate, private key is optional
     */
    CertificatePair loadServer(String commonName);
    
    /**
     * Store the given server certificate, private key is optional
     */
    void storeServer(String commonName, CertificatePair pair);
}

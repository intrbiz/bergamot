package com.intrbiz.bergamot.agent.manager.store;

import java.util.UUID;

import com.intrbiz.bergamot.crypto.util.CertificatePair;


/**
 * Store key and certificate material
 */
public interface BergamotKeyStore
{
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
}

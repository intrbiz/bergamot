package com.intrbiz.bergamot.model;

import java.util.List;
import java.util.UUID;

public interface Secured
{
    UUID getId();
    
    /**
     * Get the security domains this group exists within
     */
    List<SecurityDomain> getSecurityDomains();
}

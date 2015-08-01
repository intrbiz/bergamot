package com.intrbiz.bergamot.model;

import java.util.List;

public interface Secured
{
    /**
     * Get the security domains this group exists within
     */
    List<SecurityDomain> getSecurityDomains();
}

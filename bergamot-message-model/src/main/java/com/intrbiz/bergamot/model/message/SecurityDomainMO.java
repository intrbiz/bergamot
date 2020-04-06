package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A security domain, grouping checks for access controls
 */
@JsonTypeName("bergamot.security_domain")
public class SecurityDomainMO extends NamedObjectMO
{   
    private static final long serialVersionUID = 1L;
    
    public SecurityDomainMO()
    {
        super();
    }
}

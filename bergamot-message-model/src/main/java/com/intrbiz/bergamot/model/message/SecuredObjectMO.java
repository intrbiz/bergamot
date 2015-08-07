package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class SecuredObjectMO extends NamedObjectMO
{
    @JsonProperty("security_domains")
    private List<SecurityDomainMO> securityDomains = new LinkedList<SecurityDomainMO>();
    
    public SecuredObjectMO()
    {
        super();
    }

    public List<SecurityDomainMO> getSecurityDomains()
    {
        return securityDomains;
    }

    public void setSecurityDomains(List<SecurityDomainMO> securityDomains)
    {
        this.securityDomains = securityDomains;
    }
}

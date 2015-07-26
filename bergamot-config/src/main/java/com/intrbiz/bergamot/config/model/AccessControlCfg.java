package com.intrbiz.bergamot.config.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "access-control")
@XmlRootElement(name = "access-control")
public class AccessControlCfg implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String securityDomain;
    
    private Set<String> grantedPermissions = new LinkedHashSet<String>();

    private Set<String> revokedPermissions = new LinkedHashSet<String>();

    public AccessControlCfg()
    {
        super();
    }

    public String getSecurityDomain()
    {
        return securityDomain;
    }

    public void setSecurityDomain(String securityDomain)
    {
        this.securityDomain = securityDomain;
    }

    public Set<String> getGrantedPermissions()
    {
        return grantedPermissions;
    }

    public void setGrantedPermissions(Set<String> grantedPermissions)
    {
        this.grantedPermissions = grantedPermissions;
    }

    public Set<String> getRevokedPermissions()
    {
        return revokedPermissions;
    }

    public void setRevokedPermissions(Set<String> revokedPermissions)
    {
        this.revokedPermissions = revokedPermissions;
    }
}

package com.intrbiz.bergamot.config.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.SmartMergeSet;

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

    @XmlAttribute(name = "security-domain")
    public String getSecurityDomain()
    {
        return securityDomain;
    }

    public void setSecurityDomain(String securityDomain)
    {
        this.securityDomain = securityDomain;
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "grants")
    @ResolveWith(SmartMergeSet.class)
    public Set<String> getGrantedPermissions()
    {
        return grantedPermissions;
    }

    public void setGrantedPermissions(Set<String> grantedPermissions)
    {
        this.grantedPermissions = grantedPermissions;
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "revokes")
    @ResolveWith(SmartMergeSet.class)
    public Set<String> getRevokedPermissions()
    {
        return revokedPermissions;
    }

    public void setRevokedPermissions(Set<String> revokedPermissions)
    {
        this.revokedPermissions = revokedPermissions;
    }
}

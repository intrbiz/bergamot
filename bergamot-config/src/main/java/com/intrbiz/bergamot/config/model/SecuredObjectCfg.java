package com.intrbiz.bergamot.config.model;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyCollection;

public abstract class SecuredObjectCfg<P extends SecuredObjectCfg<P>> extends NamedObjectCfg<P>
{
    private static final long serialVersionUID = 1L;
    
    private Set<String> securityDomains = new LinkedHashSet<String>();

    public SecuredObjectCfg()
    {
        super();
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "security-domains")
    @ResolveWith(CoalesceEmptyCollection.class)
    public Set<String> getSecurityDomains()
    {
        return securityDomains;
    }

    public void setSecurityDomains(Set<String> securityDomains)
    {
        this.securityDomains = securityDomains;
    }
}

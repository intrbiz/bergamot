package com.intrbiz.bergamot.config.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "security-domain")
@XmlRootElement(name = "security-domain")
public class SecurityDomainCfg extends NamedObjectCfg<SecurityDomainCfg>
{
    private static final long serialVersionUID = 1L;

    public SecurityDomainCfg()
    {
        super();
    }
}

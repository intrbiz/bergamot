package com.intrbiz.bergamot.config.model;

import java.util.LinkedList;
import java.util.List;

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
    
    @Override
    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        return new LinkedList<TemplatedObjectCfg<?>>();
    }
}

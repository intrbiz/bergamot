package com.intrbiz.bergamot.config.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "service")
@XmlRootElement(name = "service")
public class ServiceCfg extends ActiveCheckCfg<ServiceCfg>
{
    public ServiceCfg()
    {
        super();
    }
}

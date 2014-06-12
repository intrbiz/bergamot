package com.intrbiz.bergamot.config.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "trap")
@XmlRootElement(name = "trap")
public class TrapCfg extends PassiveCheckCfg<TrapCfg>
{
    private static final long serialVersionUID = 1L;
    
    public TrapCfg()
    {
        super();
    }
}

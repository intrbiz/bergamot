package com.intrbiz.bergamot.config.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "fixed-period")
@XmlRootElement(name = "fixed-period")
public class SLAFixedPeriodCfg extends SLAPeriodCfg
{
    private static final long serialVersionUID = 1L;

    public SLAFixedPeriodCfg()
    {
        super();
    }
}

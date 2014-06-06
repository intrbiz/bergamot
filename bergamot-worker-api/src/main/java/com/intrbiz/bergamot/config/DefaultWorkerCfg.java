package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "worker")
@XmlRootElement(name = "worker")
public class DefaultWorkerCfg extends WorkerCfg
{
    public DefaultWorkerCfg()
    {
        super();
    }
}

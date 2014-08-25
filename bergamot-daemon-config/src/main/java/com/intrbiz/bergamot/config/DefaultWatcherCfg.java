package com.intrbiz.bergamot.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "watcher")
@XmlRootElement(name = "watcher")
public class DefaultWatcherCfg extends WatcherCfg
{
    private static final long serialVersionUID = 1L;
    
    public DefaultWatcherCfg()
    {
        super();
    }
}

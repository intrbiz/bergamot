package com.intrbiz.bergamot.watcher.config;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.config.WatcherCfg;
import com.intrbiz.bergamot.watcher.engine.snmp.LinkStateExecutor;
import com.intrbiz.bergamot.watcher.engine.snmp.SNMPEngine;

@XmlType(name = "watcher")
@XmlRootElement(name = "watcher")
public class SNMPWatcherCfg extends WatcherCfg
{
    private static final long serialVersionUID = 1L;

    public SNMPWatcherCfg()
    {
        super();
    }

    @Override
    public void applyDefaults()
    {
        // add our default engines to avoid needing to configure them
        this.getEngines().add(new EngineCfg(SNMPEngine.class, new ExecutorCfg(LinkStateExecutor.class)));
        // apply defaults from super class
        super.applyDefaults();
    }
}

package com.intrbiz.bergamot.watcher;

import com.intrbiz.bergamot.watcher.DefaultWatcher;
import com.intrbiz.bergamot.watcher.Watcher;
import com.intrbiz.bergamot.watcher.config.SNMPWatcherCfg;
 
/**
 * A watcher to receive SNMP traps
 */
public class SNMPWatcher extends DefaultWatcher
{
    public SNMPWatcher()
    {
        super(SNMPWatcherCfg.class, "/etc/bergamot/watcher/snmp.xml");
    }
    
    public static void main(String[] args) throws Exception
    {
        Watcher watcher = new SNMPWatcher();
        watcher.start();
    }
}

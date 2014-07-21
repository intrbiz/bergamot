package com.intrbiz.bergamot.watcher;

import java.util.Collection;
import java.util.UUID;

import com.intrbiz.bergamot.config.WatcherCfg;
import com.intrbiz.bergamot.watcher.engine.Engine;
import com.intrbiz.configuration.Configurable;

/**
 * A watcher is responsible for listening for events 
 * and updating traps
 * 
 */
public interface Watcher extends Configurable<WatcherCfg>
{
    UUID getSite();
    
    UUID getLocation();
    
    UUID getId();
    
    Collection<Engine> getEngines();
    
    void start() throws Exception;
}

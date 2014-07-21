package com.intrbiz.bergamot.watcher.engine;

import java.util.Collection;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.watcher.Watcher;
import com.intrbiz.configuration.Configurable;

public interface Engine extends Configurable<EngineCfg>
{
    public Collection<Executor<?>> getExecutors();
    
    String getName();

    Watcher getWatcher();

    void setWatcher(Watcher watcher);
    
    void start() throws Exception;
}

package com.intrbiz.bergamot.watcher.engine;

import java.util.function.Consumer;

import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;
import com.intrbiz.bergamot.model.message.event.watcher.RegisterCheck;
import com.intrbiz.bergamot.model.message.event.watcher.UnregisterCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.configuration.Configurable;

/**
 * Listeners setup the listening services for a particular type of trap
 */
public interface Executor<T extends Engine> extends Configurable<ExecutorCfg>
{
    T getEngine();
    
    void setEngine(T engine);
    
    boolean accept(CheckEvent check);
    
    void register(RegisterCheck check, Consumer<Result> resultConsumer);
    
    void unregister(UnregisterCheck check);
    
    void start() throws Exception;
}

package com.intrbiz.bergamot.worker.engine;

import java.util.Collection;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.Worker;
import com.intrbiz.configuration.Configurable;

public interface Engine extends Configurable<EngineCfg>
{
    public Collection<Executor<?>> getExecutors();
    
    String getName();

    Worker getWorker();

    void setWorker(Worker worker);
    
    void execute(ExecuteCheck task);
    
    void start() throws Exception;
}

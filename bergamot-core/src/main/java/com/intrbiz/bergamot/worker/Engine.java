package com.intrbiz.bergamot.worker;

import java.util.Collection;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.model.message.task.Task;
import com.intrbiz.configuration.Configurable;

public interface Engine extends Configurable<EngineCfg>
{
    public Collection<Executor<?>> getExecutors();
    
    String getName();

    Worker getWorker();

    void setWorker(Worker worker);
    
    void execute(Task task);
}

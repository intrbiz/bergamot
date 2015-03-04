package com.intrbiz.bergamot.worker.engine;

import java.util.Collection;
import java.util.function.Consumer;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.worker.Worker;
import com.intrbiz.configuration.Configurable;

public interface Engine extends Configurable<EngineCfg>
{
    public Collection<Executor<?>> getExecutors();
    
    String getName();

    Worker getWorker();

    void setWorker(Worker worker);
    
    void execute(ExecuteCheck task);
    
    /**
     * For testing
     */
    void execute(ExecuteCheck task, Consumer<ResultMO> onResult);
    
    /**
     * Publish a result out of band
     */
    void publishResult(ResultKey key, ResultMO resultMO);
    
    void start() throws Exception;
}

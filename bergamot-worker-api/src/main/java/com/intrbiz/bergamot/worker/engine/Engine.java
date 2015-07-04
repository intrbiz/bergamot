package com.intrbiz.bergamot.worker.engine;

import java.util.Collection;
import java.util.UUID;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.ReadingKey;
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
    
    boolean isAgentRouted();
    
    /**
     * Publish a result out of band
     */
    void publishResult(ResultKey key, ResultMO resultMO);
    
    /**
     * Publish a reading
     * @param key - the routing information
     * @param readingParcelMO - the parcel of readings to send
     */
    public void publishReading(ReadingKey key, ReadingParcelMO readingParcelMO);
    
    void bindAgent(UUID agentId);
    
    void unbindAgent(UUID agentId);
    
    void start() throws Exception;
}

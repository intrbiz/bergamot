package com.intrbiz.bergamot.worker;

import java.util.Collection;
import java.util.UUID;

import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.worker.engine.Engine;
import com.intrbiz.configuration.Configurable;

/**
 * A worker is responsible for performing tasks.
 * 
 * Workers have a number of engines registered, 
 * engines are specialised, eg: nagios, snmp, etc.
 * 
 */
public interface Worker extends Configurable<WorkerCfg>
{
    UUID getSite();
    
    UUID getId();
    
    String getWorkerPool();
    
    Collection<Engine> getEngines();
    
    void start() throws Exception;
}

package com.intrbiz.bergamot.worker;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.worker.engine.Engine;

public abstract class AbstractWorker implements Worker
{
    private Logger logger = Logger.getLogger(AbstractWorker.class);

    private Map<String, Engine> engines = new TreeMap<String, Engine>();

    private UUID site;
    
    private UUID id = UUID.randomUUID();

    private String workerPool;

    private WorkerCfg configuration;

    public AbstractWorker()
    {
        super();
    }

    @Override
    public final void configure(WorkerCfg cfg) throws Exception
    {
        this.configuration = cfg;
        this.configure();
    }

    @Override
    public final WorkerCfg getConfiguration()
    {
        return this.configuration;
    }

    @Override
    public final UUID getSite()
    {
        return this.site;
    }
    
    @Override
    public final UUID getId()
    {
        return this.id;
    }

    @Override
    public final String getWorkerPool()
    {
        return this.workerPool;
    }

    protected void configure() throws Exception
    {
        this.site = this.configuration.getSite();
        this.workerPool = this.configuration.getWorkerPool();
        // load our runners
        for (EngineCfg engineCfg : this.configuration.getEngines())
        {
            Engine engine = (Engine) engineCfg.create();
            engine.setWorker(this);
            logger.debug("Adding engine: " + engine);
            this.engines.put(engine.getName(), engine);
        }
    }

    public Collection<Engine> getEngines()
    {
        return this.engines.values();
    }

    @Override
    public void start() throws Exception
    {
        for (Engine engine : this.getEngines())
        {
            engine.start();
        }
    }
    
    public abstract String getDaemonName();
}

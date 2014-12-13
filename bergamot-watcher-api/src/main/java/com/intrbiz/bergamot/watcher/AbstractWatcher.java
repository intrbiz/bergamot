package com.intrbiz.bergamot.watcher;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.WatcherCfg;
import com.intrbiz.bergamot.watcher.engine.Engine;

public abstract class AbstractWatcher implements Watcher
{
    private Logger logger = Logger.getLogger(AbstractWatcher.class);

    private Map<String, Engine> engines = new TreeMap<String, Engine>();

    private UUID site;

    private UUID location;

    private WatcherCfg configuration;
    
    private final UUID watcherId = UUID.randomUUID();

    public AbstractWatcher()
    {
        super();
    }

    @Override
    public final void configure(WatcherCfg cfg) throws Exception
    {
        this.configuration = cfg;
        this.configure();
    }

    @Override
    public final WatcherCfg getConfiguration()
    {
        return this.configuration;
    }

    @Override
    public final UUID getSite()
    {
        return this.site;
    }

    @Override
    public final UUID getLocation()
    {
        return this.location;
    }
    
    @Override
    public final UUID getId()
    {
        return this.watcherId;
    }

    protected void configure() throws Exception
    {
        this.site = this.configuration.getSite();
        this.location = this.configuration.getLocation();
        // load our runners
        for (EngineCfg engineCfg : this.configuration.getEngines())
        {
            Engine engine = (Engine) engineCfg.create();
            engine.setWatcher(this);
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
}

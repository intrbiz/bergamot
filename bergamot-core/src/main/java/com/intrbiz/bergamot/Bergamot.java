package com.intrbiz.bergamot;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.BergamotDaemonCfg;
import com.intrbiz.bergamot.manifold.Manifold;
import com.intrbiz.bergamot.notification.Notifier;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.bergamot.store.ObjectStore;
import com.intrbiz.bergamot.worker.Worker;
import com.intrbiz.configuration.Configurable;

/**
 * Bergamot, a simple monitoring system.
 */
public abstract class Bergamot implements Configurable<BergamotDaemonCfg>
{
    private Logger logger = Logger.getLogger(Bergamot.class);

    protected String name = UUID.randomUUID().toString();

    protected BergamotDaemonCfg config;

    protected ObjectStore store;

    // master

    protected Manifold manifold;

    protected ResultProcessor resultProcessor;

    protected Scheduler scheduler;

    // worker

    protected List<Worker> workers = new LinkedList<Worker>();

    // notifier

    protected Notifier notifier;

    public Bergamot()
    {
        super();
    }

    public String getName()
    {
        return this.name;
    }

    public ObjectStore getObjectStore()
    {
        return this.store;
    }

    public Manifold getManifold()
    {
        return manifold;
    }

    public ResultProcessor getResultProcessor()
    {
        return resultProcessor;
    }

    public Scheduler getScheduler()
    {
        return scheduler;
    }

    public List<Worker> getWorkers()
    {
        return workers;
    }

    public Notifier getNotifier()
    {
        return notifier;
    }

    @Override
    public BergamotDaemonCfg getConfiguration()
    {
        return this.config;
    }

    @Override
    public void configure(BergamotDaemonCfg config) throws Exception
    {
        if (config == null) throw new IllegalArgumentException("Cannot configure with null configuration!");
        this.config = config;
        this.config.applyDefaults();
        logger.info("Using daemon configuration: ");
        logger.info(this.config.toString());
    }

    public abstract void start();
}

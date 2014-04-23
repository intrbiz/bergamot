package com.intrbiz.bergamot;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.compat.NagiosConfigImporter;
import com.intrbiz.bergamot.compat.config.builder.NagiosConfigBuilder;
import com.intrbiz.bergamot.compat.macro.MacroFrame;
import com.intrbiz.bergamot.config.BergamotCfg;
import com.intrbiz.bergamot.config.BergamotCfg.DaemonMode;
import com.intrbiz.bergamot.config.Macro;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.manifold.Manifold;
import com.intrbiz.bergamot.manifold.RabbitManifold;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.result.DefaultResultProcessor;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.bergamot.scheduler.WheelScheduler;
import com.intrbiz.bergamot.store.ObjectStore;
import com.intrbiz.bergamot.worker.DefaultWorker;
import com.intrbiz.bergamot.worker.Worker;
import com.intrbiz.configuration.Configurable;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.rabbit.RabbitPool;

/**
 * Bergamot, a simple monitoring system.
 */
public class Bergamot implements Configurable<BergamotCfg>
{   
    private Logger logger = Logger.getLogger(Bergamot.class);

    private String name = UUID.randomUUID().toString();

    private BergamotCfg config;

    private ObjectStore store;

    // master

    private Manifold manifold;

    private ResultProcessor resultProcessor;

    private Scheduler scheduler;

    // worker

    private List<Worker> workers = new LinkedList<Worker>();

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

    @Override
    public BergamotCfg getConfiguration()
    {
        return this.config;
    }

    @Override
    public void configure(BergamotCfg config) throws Exception
    {
        if (config == null) throw new IllegalArgumentException("Cannot configure with null configuration!");
        this.config = config;
        this.config.applyDefaults();
        System.out.println("Using configuration: ");
        System.out.println(this.config.toString());
        // sanity check
        if (this.config.getManifold() == null) throw new RuntimeException("A manifold must be configured");
        if (this.config.getMode() == DaemonMode.MASTER)
        {
            if (this.config.getScheduler() == null) throw new RuntimeException("A scheduler must be configured");
        }
        // setup basic stuff
        this.name = config.getName();
        // load global macros
        for (Macro macro : this.config.getMacros())
        {
            MacroFrame.GLOBAL_MACROS.put(macro.getName(), macro.getValue());
        }
        // setup the queue manager
        System.out.println("  Using RabbitMQ broker: " + this.config.getBroker().getUrl());
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool(this.config.getBroker().getUrl()));
        // setup the database manager
        if (this.config.getMode() == DaemonMode.MASTER || this.config.getMode() == DaemonMode.BOTH)
        {
            System.out.println("  Using PostgreSQL database: ");
        }
        // manifold
        if (this.config.getManifold() != null)
        {
            this.manifold = new RabbitManifold();
            this.manifold.setBergamot(this);
            this.manifold.configure(this.config.getManifold());
        }
        // result processor
        if (this.config.getResultProcessor() != null)
        {
            this.resultProcessor = new DefaultResultProcessor();
            this.resultProcessor.setBergamot(this);
            this.resultProcessor.configure(this.config.getResultProcessor());
        }
        // scheduler
        if (this.config.getScheduler() != null)
        {
            this.scheduler = new WheelScheduler();
            this.scheduler.setBergamot(this);
            this.scheduler.configure(this.config.getScheduler());
        }
        // workers
        for (WorkerCfg workerCfg : this.config.getWorkers())
        {
            Worker worker = new DefaultWorker();
            worker.setBergamot(this);
            this.workers.add(worker);
            worker.configure(workerCfg);
        }
        // import the configuration ?
        if (this.config.getMode() == DaemonMode.MASTER || this.config.getMode() == DaemonMode.BOTH)
        {
            this.importConfiguration();
        }
    }

    protected void importConfiguration()
    {
        try
        {
            // parse the config
            File cfgDir = new File(this.config.getConfigurationDirectory());
            NagiosConfigBuilder builder = new NagiosConfigBuilder(cfgDir).includeDir(cfgDir).parse();
            // build the object graph
            NagiosConfigImporter imp = new NagiosConfigImporter(builder);
            // build the object model
            this.store = imp.compute();
            // stats
            logger.info("Imported commands: " + store.getCommandCount());
            logger.info("Imported timeperiods: " + store.getTimePeriodCount());
            logger.info("Imported hostgroups: " + store.getHostgroupCount());
            logger.info("Imported servicegroups: " + store.getServicegroupCount());
            logger.info("Imported hosts: " + store.getHostCount());
            logger.info("Imported services: " + store.getServiceCount());
            // register all the services with the scheduler
            for (Host host : store.getHosts())
            {
                for (Service service : host.getServices())
                {
                    this.scheduler.schedule(service);
                }
                this.scheduler.schedule(host);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to import configuration");
        }
    }

    public void start()
    {
        try
        {
            // manifold
            this.manifold.start();
            // result processor
            if (this.resultProcessor != null) this.resultProcessor.start();
            // workers
            for (Worker worker : this.workers)
            {
                worker.start();
            }
            // scheduler
            if (this.scheduler != null) this.scheduler.start();
            //
            System.out.println("Bergamot " + this.getName() + " (" + String.valueOf(this.config.getMode()).toLowerCase() + ") up and running!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Failed to start Bergamot, exiting");
            System.exit(1);
        }
    }

    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        // read the config file
        BergamotCfg config = BergamotCfg.read(new File(System.getProperty("bergamot.cfg", "/etc/bergamot.xml")));
        // start up
        Bergamot bergamot = new Bergamot();
        bergamot.configure(config);
        bergamot.start();
    }
}

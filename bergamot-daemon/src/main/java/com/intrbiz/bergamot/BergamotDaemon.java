package com.intrbiz.bergamot;

import java.io.File;
import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.compat.macro.MacroFrame;
import com.intrbiz.bergamot.config.BergamotConfigReader;
import com.intrbiz.bergamot.config.BergamotDaemonCfg;
import com.intrbiz.bergamot.config.BergamotDaemonCfg.DaemonMode;
import com.intrbiz.bergamot.config.Macro;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.manifold.RabbitManifold;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.notification.DefaultNotifier;
import com.intrbiz.bergamot.result.DefaultResultProcessor;
import com.intrbiz.bergamot.scheduler.WheelScheduler;
import com.intrbiz.bergamot.store.BergamotConfigLoader;
import com.intrbiz.bergamot.store.ObjectStore;
import com.intrbiz.bergamot.worker.DefaultWorker;
import com.intrbiz.bergamot.worker.Worker;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.rabbit.RabbitPool;

public class BergamotDaemon extends Bergamot
{
    private Logger logger = Logger.getLogger(BergamotDaemon.class);
    
    public BergamotDaemon()
    {
        super();
    }
    
    @Override
    public void configure(BergamotDaemonCfg config) throws Exception
    {
        super.configure(config);
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
        logger.info("  Using RabbitMQ broker: " + this.config.getBroker().getUrl());
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool(this.config.getBroker().getUrl()));
        // setup the database manager
        if (this.config.getMode() == DaemonMode.MASTER || this.config.getMode() == DaemonMode.BOTH)
        {
            logger.info("  Using PostgreSQL database: ");
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
        // notifier
        if (this.config.getNotifier() != null)
        {
            this.notifier = new DefaultNotifier();
            this.notifier.setBergamot(this);
            this.notifier.configure(this.config.getNotifier());
        }
        // import the configuration ?
        if (this.config.getMode() == DaemonMode.MASTER || this.config.getMode() == DaemonMode.BOTH)
        {
            this.importConfiguration();
        }
    }

    protected void importConfiguration()
    {
        this.store = new ObjectStore();
        // parse the config
        File cfgDir = new File(this.config.getConfigurationDirectory());
        // read the config
        Collection<BergamotCfg> configs = new BergamotConfigReader().includeDir(cfgDir).build();
        for (BergamotCfg config : configs)
        {
            logger.info("Using configuration for " + config.getSite() + "\r\n" + config);
            // load
            new BergamotConfigLoader(this.store, config).load();
        }
        // stats
        logger.info("Imported commands: " + store.getCommandCount());
        logger.info("Imported timeperiods: " + store.getTimePeriodCount());
        logger.info("Imported groups: " + store.getGroupCount());
        logger.info("Imported locations: " + store.getLocationCount());
        logger.info("Imported hosts: " + store.getHostCount());
        logger.info("Imported services: " + store.getServiceCount());
        logger.info("Imported contacts: " + store.getContactsCount());
        logger.info("Imported teams: " + store.getTeamCount());
        // register all the services with the scheduler
        for (Host host : this.store.getHosts())
        {
            for (Service service : host.getServices())
            {
                this.scheduler.schedule(service);
            }
            this.scheduler.schedule(host);
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
            // notifier
            if (this.notifier != null) this.notifier.start();
            // workers
            for (Worker worker : this.workers)
            {
                worker.start();
            }
            // scheduler
            if (this.scheduler != null) this.scheduler.start();
            //
            logger.info("Bergamot " + this.getName() + " (" + String.valueOf(this.config.getMode()).toLowerCase() + ") up and running!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Failed to start Bergamot, exiting");
            System.exit(1);
        }
    }

    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        // read the config file
        BergamotDaemonCfg config = BergamotDaemonCfg.read(new File(System.getProperty("bergamot.cfg", "/etc/bergamot.xml")));
        // start up
        Bergamot bergamot = new BergamotDaemon();
        bergamot.configure(config);
        bergamot.start();
    }
}

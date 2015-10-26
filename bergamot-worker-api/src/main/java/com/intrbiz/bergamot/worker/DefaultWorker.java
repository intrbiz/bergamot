package com.intrbiz.bergamot.worker;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.intrbiz.accounting.AccountingManager;
import com.intrbiz.bergamot.accounting.BergamotAccountingQueueConsumer;
import com.intrbiz.bergamot.accounting.consumer.BergamotLoggingConsumer;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.health.HealthAgent;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.rabbit.RabbitPool;


public class DefaultWorker extends AbstractWorker
{
    protected final Class<? extends WorkerCfg> configurationClass;
    
    protected final String defaultConfigFile;
    
    protected final String daemonName;
    
    public DefaultWorker(Class<? extends WorkerCfg> configurationClass, String defaultConfigFile, String daemonName)
    {
        super();
        this.configurationClass = configurationClass;
        this.defaultConfigFile = defaultConfigFile;
        this.daemonName = daemonName;
    }
    
    @Override
    public String getDaemonName()
    {
        return this.daemonName;
    }
    
    protected void configureLogging() throws Exception
    {
        String logging = System.getProperty("bergamot.logging", "console");
        if ("console".equals(logging))
        {
            // configure logging to terminal
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(Level.toLevel(System.getProperty("bergamot.logging.level", "trace").toUpperCase()));
        }
        else
        {
            // configure from file
            PropertyConfigurator.configure(new File(logging).getAbsolutePath());
        }
    }
    
    protected WorkerCfg loadConfiguration() throws Exception
    {
        WorkerCfg config = null;
        // try the config file?
        File configFile = new File(System.getProperty("bergamot.config", this.defaultConfigFile));
        if (configFile.exists())
        {
            Logger.getLogger(Worker.class).info("Reading configuration file " + configFile.getAbsolutePath());
            config = Configuration.read(this.configurationClass, new FileInputStream(configFile));
        }
        else
        {
            config = this.configurationClass.newInstance();
        }
        config.applyDefaults();
        return config;
    }
    
    @Override
    public void start() throws Exception
    {
        // setup logging
        this.configureLogging();
        Logger logger = Logger.getLogger(Worker.class);
        // configure accounting
        // load the config
        WorkerCfg config = this.loadConfiguration();
        logger.debug("Bergamot worker, using configuration:\r\n" + config.toString());
        // setup the queue broker
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool(config.getBroker().getUrl(), config.getBroker().getUsername(), config.getBroker().getPassword()));
        // setup accounting
        AccountingManager.getInstance().registerConsumer("logger", new BergamotLoggingConsumer());
        AccountingManager.getInstance().registerConsumer("queue", new BergamotAccountingQueueConsumer());
        AccountingManager.getInstance().bindRootConsumer("logger");
        AccountingManager.getInstance().bindRootConsumer("queue");
        // configure the worker
        this.configure(config);
        // go go go
        logger.info("Bergamot worker starting.");
        // start the health agent
        HealthAgent.getInstance().init(this.getDaemonName());
        // start the actual worker
        super.start();
    }
}

package com.intrbiz.bergamot.worker;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.accounting.AccountingManager;
import com.intrbiz.bergamot.accounting.BergamotAccountingQueueConsumer;
import com.intrbiz.bergamot.accounting.consumer.BergamotLoggingConsumer;
import com.intrbiz.bergamot.config.LoggingCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.health.HealthAgent;
import com.intrbiz.bergamot.queue.util.QueueUtil;
import com.intrbiz.configuration.Configuration;


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
    
    protected void configureLogging(LoggingCfg config) throws Exception
    {
        if (config == null) config = new LoggingCfg();
        // configure logging to terminal
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.toLevel(Util.coalesceEmpty(config.getLevel(), "info").toUpperCase()));
    }
    
    /**
     * Search for the configuration file
     */
    protected File getConfigurationFile()
    {
        return new File(Util.coalesceEmpty(System.getProperty("bergamot.config"), System.getenv("bergamot_config"), System.getenv("BERGAMOT_CONFIG"), this.defaultConfigFile));
    }
    
    protected WorkerCfg loadConfiguration() throws Exception
    {
        WorkerCfg config = null;
        // try the config file?
        File configFile = this.getConfigurationFile();
        if (configFile.exists())
        {
            System.out.println("Using configuration file " + configFile.getAbsolutePath());
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
        // load the config
        WorkerCfg config = this.loadConfiguration();
        // setup logging
        this.configureLogging(config.getLogging());
        Logger logger = Logger.getLogger(Worker.class);
        logger.debug("Bergamot worker, using configuration:\r\n" + config.toString());
        // setup the queue broker
        QueueUtil.setupQueueBroker(config.getBroker(), this.getDaemonName());
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
        HealthAgent.getInstance().init("worker", this.getDaemonName());
        // start the actual worker
        super.start();
    }
}

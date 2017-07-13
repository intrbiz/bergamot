package com.intrbiz.bergamot.watcher;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.WatcherCfg;
import com.intrbiz.bergamot.queue.util.QueueUtil;
import com.intrbiz.configuration.Configuration;


public class DefaultWatcher extends AbstractWatcher
{
    protected final Class<? extends WatcherCfg> configurationClass;
    
    protected final String defaultConfigFile;
    
    protected final String daemonName;
    
    public DefaultWatcher(Class<? extends WatcherCfg> configurationClass, String defaultConfigFile, String daemonName)
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
    
    /**
     * Search for the configuration file
     */
    protected File getConfigurationFile()
    {
        return new File(Util.coalesceEmpty(System.getProperty("bergamot.config"), System.getenv("bergamot_config"), System.getenv("BERGAMOT_CONFIG"), this.defaultConfigFile));
    }
    
    protected WatcherCfg loadConfiguration() throws Exception
    {
        WatcherCfg config = null;
        // try the config file?
        File configFile = this.getConfigurationFile();
        if (configFile.exists())
        {
            Logger.getLogger(Watcher.class).info("Reading configuration file " + configFile.getAbsolutePath());
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
        Logger logger = Logger.getLogger(Watcher.class);
        // load the config
        WatcherCfg config = this.loadConfiguration();
        logger.debug("Bergamot watcher, using configuration:\r\n" + config.toString());
        // setup the queue broker
        QueueUtil.setupQueueBroker(config.getBroker(), this.getDaemonName());
        // configure the worker
        this.configure(config);
        // go go go
        logger.info("Bergamot watcher starting.");
        super.start();
    }
}

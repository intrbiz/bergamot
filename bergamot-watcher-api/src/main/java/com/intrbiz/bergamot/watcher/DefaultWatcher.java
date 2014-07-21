package com.intrbiz.bergamot.watcher;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.intrbiz.bergamot.config.DefaultWatcherCfg;
import com.intrbiz.bergamot.config.WatcherCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.rabbit.RabbitPool;


public class DefaultWatcher extends AbstractWatcher
{
    protected final Class<? extends WatcherCfg> configurationClass;
    
    protected final String defaultConfigFile;
    
    public DefaultWatcher(Class<? extends WatcherCfg> configurationClass, String defaultConfigFile)
    {
        super();
        this.configurationClass = configurationClass;
        this.defaultConfigFile = defaultConfigFile;
    }
    
    public DefaultWatcher()
    {
        this(DefaultWatcherCfg.class, "/etc/bergamot/watcher/default.xml");
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
    
    protected WatcherCfg loadConfiguration() throws Exception
    {
        WatcherCfg config = null;
        // try the config file?
        File configFile = new File(System.getProperty("bergamot.config", this.defaultConfigFile));
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
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool(config.getBroker().getUrl()));
        // configure the worker
        this.configure(config);
        // go go go
        logger.info("Bergamot watcher starting.");
        super.start();
    }
    
    public static void main(String[] args) throws Exception
    {
        Watcher watcher = new DefaultWatcher();
        watcher.start();
    }
}

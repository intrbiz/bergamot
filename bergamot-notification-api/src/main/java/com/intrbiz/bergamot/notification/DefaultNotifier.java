package com.intrbiz.bergamot.notification;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.intrbiz.bergamot.config.DefaultNotifierCfg;
import com.intrbiz.bergamot.config.NotifierCfg;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.rabbit.RabbitPool;

public class DefaultNotifier extends AbstractNotifier
{
    protected final Class<? extends NotifierCfg> configurationClass;

    protected final String defaultConfigFile;
    
    protected final String notifierName;

    public DefaultNotifier(Class<? extends NotifierCfg> configurationClass, String defaultConfigFile, String notifierName)
    {
        super();
        this.configurationClass = configurationClass;
        this.defaultConfigFile = defaultConfigFile;
        this.notifierName = notifierName;
    }

    public DefaultNotifier()
    {
        this(DefaultNotifierCfg.class, "/etc/bergamot/notifier/default.xml", "default");
    }

    @Override
    protected String getNotifierName()
    {
        return this.notifierName;
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

    protected NotifierCfg loadConfiguration() throws Exception
    {
        NotifierCfg config = null;
        // try the config file?
        File configFile = new File(System.getProperty("bergamot.config", this.defaultConfigFile));
        if (configFile.exists())
        {
            Logger.getLogger(Notifier.class).info("Reading configuration file " + configFile.getAbsolutePath());
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
        Logger logger = Logger.getLogger(Notifier.class);
        // load the config
        NotifierCfg config = this.loadConfiguration();
        logger.debug("Bergamot notifier, using configuration:\r\n" + config.toString());
        // setup the queue broker
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool(config.getBroker().getUrl(), config.getBroker().getUsername(), config.getBroker().getPassword()));
        // configure the worker
        this.configure(config);
        // go go go
        logger.info("Bergamot notifier starting.");
        super.start();
    }

    public static void main(String[] args) throws Exception
    {
        Notifier notifier = new DefaultNotifier();
        notifier.start();
    }
}

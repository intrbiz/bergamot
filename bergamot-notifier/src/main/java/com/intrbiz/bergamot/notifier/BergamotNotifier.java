package com.intrbiz.bergamot.notifier;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.hazelcast.client.HazelcastClientNotActiveException;
import com.hazelcast.spi.exception.TargetDisconnectedException;
import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.client.NotifierClient;
import com.intrbiz.bergamot.config.LoggingCfg;
import com.intrbiz.bergamot.config.NotifierCfg;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.notification.NotificationEngine;
import com.intrbiz.bergamot.notification.NotificationEngineContext;
import com.intrbiz.bergamot.notification.engine.email.EmailEngine;
import com.intrbiz.bergamot.notification.engine.slack.SlackEngine;
import com.intrbiz.bergamot.notification.engine.sms.SMSEngine;
import com.intrbiz.bergamot.notification.engine.webhook.WebHookEngine;
import com.intrbiz.configuration.Configurable;
import com.intrbiz.configuration.Configuration;

public class BergamotNotifier implements Configurable<NotifierCfg>
{   
    public static final String DEFAULT_CONFIGURATION_FILE = "/etc/bergamot/notifier/default.xml";
    
    public static final String DAEMON_NAME = "bergamot-notifier";
    
    private static final Logger logger = Logger.getLogger(BergamotNotifier.class);
    
    private static final List<AvailableEngine> AVAILABLE_ENGINES = new ArrayList<>();
    
    static
    {
        AVAILABLE_ENGINES.add(new AvailableEngine(EmailEngine.NAME,   EmailEngine::new,   true));
        AVAILABLE_ENGINES.add(new AvailableEngine(SMSEngine.NAME,     SMSEngine::new,     false));
        AVAILABLE_ENGINES.add(new AvailableEngine(SlackEngine.NAME,   SlackEngine::new,   true));
        AVAILABLE_ENGINES.add(new AvailableEngine(WebHookEngine.NAME, WebHookEngine::new, true));
    }
    
    private static final int CONNECTION_ERROR_LIMIT = 60;
    
    private final UUID id = UUID.randomUUID();
    
    private NotifierCfg configuration;
    
    private Map<String, NotificationEngine> engines = new TreeMap<String, NotificationEngine>();

    private Set<UUID> sites = new HashSet<>();
    
    private String info;
    
    private String hostName;
    
    private int threadCount;
    
    private NotifierClient client;
    
    private Thread[] threads;

    private CountDownLatch executorLatch;
    
    private CountDownLatch shutdownLatch;
    
    private AtomicBoolean run;
    
    public BergamotNotifier()
    {
        super();
    }

    public final NotifierCfg getConfiguration()
    {
        return this.configuration;
    }

    public final Set<UUID> getSites()
    {
        return this.sites;
    }
    
    public final UUID getId()
    {
        return this.id;
    }
    
    public final String getInfo()
    {
        return this.info;
    }

    public final Collection<NotificationEngine> getEngines()
    {
        return this.engines.values();
    }
    
    protected String getConfigurationParameter(String name, Supplier<String> cfgValue, String defaultValue)
    {
        /*
         * Fetch configuration in order of:
         *  - Env var
         *  - System property
         *  - Configuration parameter
         *  - Specific configuration method
         *  - Default value
         */
        return Util.coalesceEmpty(
            System.getenv(name.toUpperCase().replace('.', '_').replace('-', '_')),
            System.getProperty(name),
            configuration.getStringParameterValue(name, null),
            cfgValue == null ? null : cfgValue.get(),
            defaultValue
        );
    }
    
    protected String getConfigurationParameter(String name, String defaultValue)
    {
        return this.getConfigurationParameter(name, null, defaultValue);
    }
    
    @Override
    public final void configure(NotifierCfg cfg) throws Exception
    {
        this.configuration = cfg;
        // TODO: support multiple sites
        String site = this.getConfigurationParameter("site", this.configuration::getSite, null);
        if (! Util.isEmpty(site))
        {
            this.sites.add(UUID.fromString(site));
        }
        this.info = this.getConfigurationParameter("info", this.configuration::getInfo, null);
        this.hostName = InetAddress.getLocalHost().getHostName();
        this.threadCount = Integer.parseInt(this.getConfigurationParameter("threads", String.valueOf(this.configuration.getThreads())));
        // register engines 
        for (AvailableEngine availableEngine : AVAILABLE_ENGINES)
        {
            if (this.configuration.isEngineEnabled(availableEngine.name, availableEngine.enabledByDefault))
            {
                NotificationEngine engine = availableEngine.constructor.get();
                this.engines.put(engine.getName(), engine);
                logger.info("Registering notification engine: " + engine.getName());
            }
        }
    }

    public void start() throws Exception
    {
        logger.info("Bergamot Notifier starting....");
        this.shutdownLatch = new CountDownLatch(1);
        // prepare our engines
        this.prepareEngines();
        // prepare our executors
        this.createExecutors();
        // connect to the scheduler
        this.connectScheduler();
        // start our engines
        this.startEngines();
        // start our executors
        this.startExecutors();
    }
    
    protected void prepareEngines() throws Exception
    {
        for (NotificationEngine engine : this.getEngines())
        {
            logger.info("Preparing notification engine: " + engine.getName());
            engine.prepare(this.createEngineContext(engine));
        }
    }

    protected NotificationEngineContext createEngineContext(final NotificationEngine e)
    {
        return new NotificationEngineContext() {
            @Override
            public String getParameter(String name, String defaultValue)
            {
                return getConfigurationParameter(name, defaultValue);
            }
        };
    }
    
    protected void connectScheduler() throws Exception
    {
        this.client = new NotifierClient(this.configuration.getCluster(), this::clusterPanic, DAEMON_NAME, this.info, this.hostName);
        this.client.register(this.sites, this.engines.keySet());
    }
    
    protected void createExecutors() throws Exception
    {
        logger.info("Creating " + this.threadCount + " notification executors");
        this.executorLatch = new CountDownLatch(this.threadCount);
        this.run = new AtomicBoolean(false);
        this.threads = new Thread[this.threadCount];
        for (int i = 0; i < this.threads.length; i++)
        {
            final int threadNum = i;
            this.threads[i] = new Thread(() -> {
                int connectionErrors = 0;
                try
                {
                    logger.debug("Notifier executor " + threadNum + " starting.");
                    while (this.run.get())
                    {
                        try
                        {
                            // get a notification to send
                            Notification notification = this.client.getConsumer().poll(5, TimeUnit.SECONDS);
                            if (notification != null)
                            {
                                if (logger.isTraceEnabled())
                                    logger.trace("Sending notification: " + notification);
                                // send the notification for the requested engine
                                NotificationEngine engine = this.engines.get(notification.getEngine());
                                if (engine != null && engine.accept(notification))
                                {
                                    engine.sendNotification(notification);
                                }
                            }
                            connectionErrors = 0;
                        }
                        catch (TargetDisconnectedException | HazelcastClientNotActiveException e)
                        {
                            if (this.run.get())
                            {
                                connectionErrors ++;
                                if (connectionErrors > CONNECTION_ERROR_LIMIT)
                                {
                                    logger.fatal("Got too many connection errors from Hazelcast, shutting down!");
                                    this.clusterPanic(null);
                                }
                                try
                                {
                                    Thread.sleep(5_000);
                                }
                                catch (InterruptedException ie)
                                {
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            logger.error("Error sending notification", e);
                        }
                    }
                    logger.debug("Notifier executor " + threadNum + " stopped.");
                }
                finally
                {
                    this.executorLatch.countDown();
                }
            }, "Bergamot-Notifier-Executor-" + i);
        }
    }
    
    protected void clusterPanic(Void v)
    {
        logger.fatal("Connection to cluster lost, forcing shutdown now!");
        // Trigger a shutdown
        this.triggerShutdown(false);
    }
    
    protected void startExecutors() throws Exception
    {
        logger.info("Starting " + this.threads.length + " notification executors");
        this.run.set(true);
        for (int i = 0; i < this.threads.length; i++)
        {
            this.threads[i].start();
        }
    }
    
    protected void startEngines() throws Exception
    {
        for (NotificationEngine engine : this.getEngines())
        {
            logger.info("Starting notification engine: " + engine.getName());
            engine.start(this.createEngineContext(engine));
        }
    }
    
    public void run() throws Exception
    {
        // Start this worker
        this.start();
        // Await for all execution threads
        this.awaitExecutors();
        // Stop this worker
        this.stop();
    }
    
    protected void stop()
    {
        try
        {
            logger.info("Bergamot Notifier stopping....");
            // Shutdown all engines
            this.shutdownEngines();
            // Disconnect from the scheduler
            this.disconnectScheduler();
            logger.info("Bergamot Notifier stopped.");
        }
        finally
        {
            this.shutdownLatch.countDown();
        }
    }
    
    protected void awaitExecutors()
    {
        // Wait for all executors to notify the latch
        while (true)
        {
            try
            {
                this.executorLatch.await();
                break;
            }
            catch (InterruptedException e)
            {
            }
        }
        this.threads = null;
        this.executorLatch = null;
    }
    
    protected void shutdownEngines()
    {
        for (NotificationEngine engine : this.getEngines())
        {
            logger.info("Shutting down notification engine: " + engine.getName());
            engine.shutdown(this.createEngineContext(engine));
        }
    }
    
    public void triggerShutdown(boolean await)
    {
        if (this.run.compareAndSet(true, false))
        {
            logger.info("Shutting down Bergamot Worker");
            // Wait for the shutdown to complete
            while (await)
            {
                try
                {
                    this.shutdownLatch.await();
                    break;
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }
    
    protected void disconnectScheduler()
    {
        logger.info("Disconnecting from cluster");
        try
        {
            this.client.unregister();
        }
        catch (Exception e)
        {
            // ignore
        }
        this.client.close();
    }
    
    /**
     * Start this worker daemon
     */
    public static void main(String[] args) throws Exception
    {
        try
        {
            // Load configuration
            NotifierCfg config = loadConfiguration();
            // Configure logging
            configureLogging(config.getLogging());
            Logger logger = Logger.getLogger(BergamotNotifier.class);
            logger.info("Bergamot Notifier, using configuration:\r\n" + config.toString());
            // Create the worker
            BergamotNotifier notifier = new BergamotNotifier();
            notifier.configure(config);
            // Register a shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Triggering shutdown of Bergamot Notifier");
                notifier.triggerShutdown(true);
            }));
            // Start our worker
            logger.info("Bergamot Notifier starting.");
            notifier.run();
            // Terminate normally
            Thread.sleep(15_000);
            System.exit(0);
        }
        catch (Exception e)
        {
            System.err.println("Failed to start Bergamot Worker!");
            e.printStackTrace();
            Thread.sleep(15_000);
            System.exit(1);
        }
    }
    
    /**
     * Search for the configuration file
     */
    private static File getConfigurationFile()
    {
        return new File(Util.coalesceEmpty(System.getProperty("bergamot.config"), System.getenv("BERGAMOT_CONFIG"), DEFAULT_CONFIGURATION_FILE));
    }
    
    private static NotifierCfg loadConfiguration() throws Exception
    {
        NotifierCfg config = null;
        // try the config file?
        File configFile = getConfigurationFile();
        if (configFile.exists())
        {
            System.out.println("Using configuration file " + configFile.getAbsolutePath());
            config = Configuration.read(NotifierCfg.class, new FileInputStream(configFile));
        }
        else
        {
            config = new NotifierCfg();
        }
        config.applyDefaults();
        return config;
    }
    
    private static void configureLogging(LoggingCfg config) throws Exception
    {
        if (config == null) config = new LoggingCfg();
        // configure logging to terminal
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.toLevel(Util.coalesceEmpty(config.getLevel(), "info").toUpperCase()));
    }
    
    private static class AvailableEngine
    {
        public final String name;
        
        public final Supplier<NotificationEngine> constructor;
        
        public final boolean enabledByDefault;

        public AvailableEngine(String name, Supplier<NotificationEngine> constructor, boolean enabledByDefault)
        {
            super();
            this.name = name;
            this.constructor = constructor;
            this.enabledByDefault = enabledByDefault;
        }
    }
}

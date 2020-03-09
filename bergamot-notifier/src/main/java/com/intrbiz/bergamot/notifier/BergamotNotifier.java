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
import java.util.function.Supplier;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spi.exception.TargetDisconnectedException;
import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.coordinator.NotifierClientCoordinator;
import com.intrbiz.bergamot.cluster.queue.NotifierConsumer;
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
    public static final String DEFAULT_CONFIGURATION_FILE = "/etc/bergamot/notifier/config.xml";
    
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
    
    private final UUID id = UUID.randomUUID();
    
    private NotifierCfg configuration;
    
    private Map<String, NotificationEngine> engines = new TreeMap<String, NotificationEngine>();

    private Set<UUID> sites = new HashSet<>();
    
    private String info;
    
    private String hostName;
    
    private int threadCount;
    
    private HazelcastInstance hazelcast;
    
    private NotifierClientCoordinator notifierCoordinator;
    
    private NotifierConsumer consumer;
    
    private Thread[] threads;
    
    private volatile boolean run = false;
    
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
    
    //
    
    @Override
    public final void configure(NotifierCfg cfg) throws Exception
    {
        this.configuration = cfg;
        // TODO: support multiple sites
        if (this.configuration.getSite() != null)
        {
            this.sites.add(this.configuration.getSite());
        }
        this.info = this.configuration.getInfo();
        this.hostName = InetAddress.getLocalHost().getHostName();
        this.threadCount = this.configuration.getThreads();
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
        // connect to the scheduler
        this.connectScheduler();
        // prepare our engines
        this.prepareEngines();
        // prepare our executors
        this.createExecutors();
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
            public Configuration getConfiguration()
            {
                return configuration;
            }
        };
    }
    
    protected void connectScheduler() throws Exception
    {
        logger.info("Connecting to cluster");
        ClientNetworkConfig netCfg = new ClientNetworkConfig();
        netCfg.setAddresses(this.configuration.getHazelcastClient().getNodes());
        netCfg.setSmartRouting(false);
        ClientConfig cliCfg = new ClientConfig();
        cliCfg.setInstanceName("notifier");
        cliCfg.setNetworkConfig(netCfg);
        // Connect to Hazelcast
        this.hazelcast = HazelcastClient.newHazelcastClient(cliCfg);
        // Create our notifier coordinator
        this.notifierCoordinator = new NotifierClientCoordinator(this.hazelcast);
        // Register ourselves
        this.consumer = this.notifierCoordinator.registerNotifier(this.id, false, DAEMON_NAME, this.info, this.hostName, this.sites, this.engines.keySet()); 
    }
    
    protected void createExecutors() throws Exception
    {
        logger.info("Creating " + this.threadCount + " notification executors");
        this.threads = new Thread[this.threadCount];
        for (int i = 0; i < this.threads.length; i++)
        {
            final int threadNum = i;
            this.threads[i] = new Thread(() -> {
                logger.debug("Notifier executor " + threadNum + " starting.");
                while (this.run)
                {
                    try
                    {
                        // get a notification to send
                        Notification notification = this.consumer.poll();
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
                    }
                    catch (TargetDisconnectedException e)
                    {
                        // IGNORE
                    }
                    catch (Exception e)
                    {
                        logger.error("Error sending notification", e);
                    }
                }
                logger.debug("Notifier executor " + threadNum + " stopped.");
            }, "Bergamot-Notifier-Executor-" + i);
        }
    }
    
    protected void startExecutors() throws Exception
    {
        logger.info("Starting " + this.threads.length + " notification executors");
        this.run = true;
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
    
    public void shutdown()
    {
        logger.info("Shutting down notifier");
        // Shutdown all executors
        this.shutdownExecutors();
        // Shutdown all engines
        this.shutdownEngines();
        // Disconnect from the scheduler
        this.disconnectScheduler();
    }
    
    protected void shutdownExecutors()
    {
        // Stop execution threads
        if (this.run)
        {
            this.run = false;
            for (Thread thread : this.threads)
            {
                try
                {
                    thread.join();
                }
                catch (InterruptedException e)
                {
                }
            }
            this.threads = null;
        }
    }
    
    protected void shutdownEngines()
    {
        for (NotificationEngine engine : this.getEngines())
        {
            logger.info("Shutting down notification engine: " + engine.getName());
            engine.shutdown(this.createEngineContext(engine));
        }
    }
    
    protected void disconnectScheduler()
    {
        logger.info("Disconnecting from cluster");
        // Shutdown our consumer
        if (this.consumer != null)
            this.consumer.close();
        // Shutdown hazelcast
        if (this.hazelcast != null)
            this.hazelcast.shutdown();
        // Reset components
        this.consumer = null;
        this.notifierCoordinator = null;
        this.hazelcast = null;
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
            logger.debug("Bergamot Notifier, using configuration:\r\n" + config.toString());
            // Create the worker
            BergamotNotifier worker = new BergamotNotifier();
            worker.configure(config);
            // Register a shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(worker::shutdown));
            // Start our worker
            logger.info("Bergamot Notifier starting.");
            worker.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Search for the configuration file
     */
    private static File getConfigurationFile()
    {
        return new File(Util.coalesceEmpty(System.getProperty("bergamot.config"), System.getenv("bergamot_config"), System.getenv("BERGAMOT_CONFIG"), DEFAULT_CONFIGURATION_FILE));
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

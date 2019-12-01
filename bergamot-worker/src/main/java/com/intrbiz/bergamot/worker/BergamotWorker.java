package com.intrbiz.bergamot.worker;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.coordinator.ProcessingPoolCoordinator;
import com.intrbiz.bergamot.cluster.coordinator.WorkerCoordinator;
import com.intrbiz.bergamot.cluster.queue.ProcessingPoolProducer;
import com.intrbiz.bergamot.cluster.queue.WorkerConsumer;
import com.intrbiz.bergamot.config.LoggingCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.Engine;
import com.intrbiz.bergamot.worker.engine.EngineContext;
import com.intrbiz.bergamot.worker.engine.dummy.DummyEngine;
import com.intrbiz.bergamot.worker.engine.http.HTTPEngine;
import com.intrbiz.bergamot.worker.engine.jdbc.JDBCEngine;
import com.intrbiz.bergamot.worker.engine.jmx.JMXEngine;
import com.intrbiz.bergamot.worker.engine.nagios.NagiosEngine;
import com.intrbiz.bergamot.worker.engine.nrpe.NRPEEngine;
import com.intrbiz.bergamot.worker.engine.sftp.SFTPEngine;
import com.intrbiz.bergamot.worker.engine.snmp.SNMPEngine;
import com.intrbiz.bergamot.worker.engine.ssh.SSHEngine;
import com.intrbiz.configuration.Configurable;
import com.intrbiz.configuration.Configuration;

/**
 * A worker to execute nagios check engines
 */
public class BergamotWorker implements Configurable<WorkerCfg>
{   
    public static final String DEFAULT_CONFIGURATION_FILE = "/etc/bergamot/worker/config.xml";
    
    public static final String DAEMON_NAME = "bergamot-worker";
    
    private static final Logger logger = Logger.getLogger(BergamotWorker.class);
    
    private static final Map<String, Supplier<Engine>> AVAILABLE_ENGINES = new HashMap<>();
    
    static
    {
        // register all engines which are available
        // dummy
        AVAILABLE_ENGINES.put(DummyEngine.NAME, DummyEngine::new);
        // nagios
        AVAILABLE_ENGINES.put(NagiosEngine.NAME, NagiosEngine::new);
        AVAILABLE_ENGINES.put(NRPEEngine.NAME, NRPEEngine::new);
        // http
        AVAILABLE_ENGINES.put(HTTPEngine.NAME, HTTPEngine::new);
        // jdbc
        AVAILABLE_ENGINES.put(JDBCEngine.NAME, JDBCEngine::new);
        // jmx
        AVAILABLE_ENGINES.put(JMXEngine.NAME, JMXEngine::new);
        // ssh
        AVAILABLE_ENGINES.put(SSHEngine.NAME, SSHEngine::new);
        AVAILABLE_ENGINES.put(SFTPEngine.NAME, SFTPEngine::new);
        // snmp
        AVAILABLE_ENGINES.put(SNMPEngine.NAME, SNMPEngine::new);
        // TODO: add more shizz here
    }
    
    private final UUID id = UUID.randomUUID();
    
    private WorkerCfg configuration;
    
    private Map<String, Engine> engines = new TreeMap<String, Engine>();

    private Set<UUID> sites = new HashSet<>();

    private String workerPool;
    
    private String info;
    
    private int threadCount;
    
    private HazelcastInstance hazelcast;
    
    private WorkerCoordinator workerCoordinator;
    
    private ProcessingPoolCoordinator poolCoordinator;
    
    private WorkerConsumer consumer;
    
    private ProcessingPoolProducer producer;
    
    private Thread[] threads;
    
    private volatile boolean run = false;
    
    public BergamotWorker()
    {
        super();
    }

    public final WorkerCfg getConfiguration()
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

    public final String getWorkerPool()
    {
        return this.workerPool;
    }
    
    public final String getInfo()
    {
        return this.info;
    }

    public final Collection<Engine> getEngines()
    {
        return this.engines.values();
    }
    
    //
    
    @Override
    public final void configure(WorkerCfg cfg) throws Exception
    {
        this.configuration = cfg;
        // TODO: support multiple sites
        if (this.configuration.getSite() != null)
        {
            this.sites.add(this.configuration.getSite());
        }
        this.workerPool = this.configuration.getWorkerPool();
        this.info = this.configuration.getInfo();
        this.threadCount = this.configuration.getThreads();
        // register engines 
        for (Entry<String, Supplier<Engine>> availableEngine : AVAILABLE_ENGINES.entrySet())
        {
            if (this.configuration.isEngineEnabled(availableEngine.getKey()))
            {
                Engine engine = availableEngine.getValue().get();
                this.engines.put(engine.getName(), engine);
                logger.info("Registering check engine: " + engine.getName());
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
        for (Engine engine : this.getEngines())
        {
            logger.info("Preparing check engine: " + engine.getName());
            engine.prepare(this.createEngineContext(engine));
        }
    }

    protected EngineContext createEngineContext(final Engine e)
    {
        return new EngineContext() {
            @Override
            public Configuration getConfiguration()
            {
                return configuration;
            }
        };
    }
    
    protected void connectScheduler() throws Exception
    {
        logger.info("Connecting to scheduler");
        ClientNetworkConfig netCfg = new ClientNetworkConfig();
        netCfg.setAddresses(this.configuration.getHazelcastClient().getNodes());
        netCfg.setSmartRouting(false);
        ClientConfig cliCfg = new ClientConfig();
        cliCfg.setInstanceName("worker");
        cliCfg.setNetworkConfig(netCfg);
        // Connect to Hazelcast
        this.hazelcast = HazelcastClient.newHazelcastClient(cliCfg);
        // Create our worker coordinator
        this.workerCoordinator = new WorkerCoordinator(this.hazelcast);
        this.poolCoordinator = new ProcessingPoolCoordinator(this.hazelcast);
        // Register ourselves
        this.producer = this.poolCoordinator.createProcessingPoolProducer();
        this.consumer = this.workerCoordinator.registerWorker(this.id, false, DAEMON_NAME, this.info, this.sites, this.workerPool, this.engines.keySet()); 
    }
    
    protected void createExecutors() throws Exception
    {
        logger.info("Creating " + this.threadCount + " check executors");
        this.threads = new Thread[this.threadCount];
        for (int i = 0; i < this.threads.length; i++)
        {
            final int threadNum = i;
            this.threads[i] = new Thread(() -> {
                logger.debug("Worker executor " + threadNum + " starting.");
                while (this.run)
                {
                    try
                    {
                        // get a check to execute
                        ExecuteCheck check = this.consumer.poll();
                        if (check != null)
                        {
                            if (logger.isTraceEnabled())
                                logger.trace("Executing check: " + check);
                            // execute the check
                            CheckExecutionContext context = createExecutionContext(check);
                            Engine engine = this.engines.get(check.getEngine());
                            if (engine != null)
                            {
                                engine.execute(check, context);
                            }
                            else
                            {
                                context.publishResult(new ActiveResultMO().fromCheck(check).error("No engine found to execute check"));
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("Error executing check", e);
                    }
                }
                logger.debug("Worker executor " + threadNum + " stopped.");
            }, "Bergamot-Worker-Executor-" + i);
        }
    }
    
    protected void startExecutors() throws Exception
    {
        logger.info("Starting " + this.threads.length + " check executors");
        this.run = true;
        for (int i = 0; i < this.threads.length; i++)
        {
            this.threads[i].start();
        }
    }
    
    protected void startEngines() throws Exception
    {
        for (Engine engine : this.getEngines())
        {
            logger.info("Starting check engine: " + engine.getName());
            engine.start(this.createEngineContext(engine));
        }
    }
    
    protected CheckExecutionContext createExecutionContext(final ExecuteCheck check)
    {
        return new CheckExecutionContext() {
            @Override
            public void publishResult(ResultMO resultMO)
            {
                producer.publishResult(check.getProcessingPool(), resultMO);
            }

            @Override
            public void publishReading(ReadingParcelMO readingParcelMO)
            {
                producer.publishReading(check.getProcessingPool(), readingParcelMO);
            }            
        };
    }
    
    public void shutdown()
    {
        logger.info("Shutting down worker");
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
        for (Engine engine : this.getEngines())
        {
            logger.info("shutting down check engine: " + engine.getName());
            engine.shutdown(this.createEngineContext(engine));
        }
    }
    
    protected void disconnectScheduler()
    {
        logger.info("Disconnecting from scheduler");
        // Shutdown our consumer
        if (this.consumer != null)
            this.consumer.close();
        // Shutdown hazelcast
        if (this.hazelcast != null)
            this.hazelcast.shutdown();
        // Reset components
        this.consumer = null;
        this.workerCoordinator = null;
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
            WorkerCfg config = loadConfiguration();
            // Configure logging
            configureLogging(config.getLogging());
            Logger logger = Logger.getLogger(BergamotWorker.class);
            logger.debug("Bergamot Worker, using configuration:\r\n" + config.toString());
            // Create the worker
            BergamotWorker worker = new BergamotWorker();
            worker.configure(config);
            // Register a shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(worker::shutdown));
            // Start our worker
            logger.info("Bergamot Worker starting.");
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
    
    private static WorkerCfg loadConfiguration() throws Exception
    {
        WorkerCfg config = null;
        // try the config file?
        File configFile = getConfigurationFile();
        if (configFile.exists())
        {
            System.out.println("Using configuration file " + configFile.getAbsolutePath());
            config = Configuration.read(WorkerCfg.class, new FileInputStream(configFile));
        }
        else
        {
            config = new WorkerCfg();
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
}

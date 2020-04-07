package com.intrbiz.bergamot.processor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.AgentRegistrationService;
import com.intrbiz.bergamot.cluster.broker.SiteEventTopic;
import com.intrbiz.bergamot.cluster.broker.SiteNotificationTopic;
import com.intrbiz.bergamot.cluster.broker.SiteUpdateTopic;
import com.intrbiz.bergamot.cluster.consumer.PoolConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.CheckDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.NotificationDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.PoolDispatcher;
import com.intrbiz.bergamot.cluster.election.LeaderElector;
import com.intrbiz.bergamot.cluster.election.PoolElector;
import com.intrbiz.bergamot.cluster.election.model.ElectionState;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyClusterLookup;
import com.intrbiz.bergamot.cluster.member.BergamotMember;
import com.intrbiz.bergamot.cluster.registry.AgentRegistry;
import com.intrbiz.bergamot.cluster.registry.NotifierRegistry;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistar;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistry;
import com.intrbiz.bergamot.config.ClusterCfg;
import com.intrbiz.bergamot.leader.BergamotClusterLeader;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.cluster.ProcessorRegistration;
import com.intrbiz.bergamot.pool.ProcessingPools;
import com.intrbiz.bergamot.result.DefaultResultProcessor;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.bergamot.scheduler.WheelScheduler;
import com.intrbiz.lamplighter.reading.DefaultReadingProcessor;
import com.intrbiz.lamplighter.reading.ReadingProcessor;

/**
 * A Bergamot Processing node
 */
public class BergamotProcessor extends BergamotMember
{
    private static final Logger logger = Logger.getLogger(BergamotProcessor.class);
    
    // topics
    
    private final SiteEventTopic siteEventTopic;
    
    private final SiteNotificationTopic notificationTopic;
    
    private final SiteUpdateTopic updateTopic;
    
    // lookups
    
    private final AgentKeyClusterLookup agentKeyLookup;
    
    // registries
    
    private final WorkerRegistry workerRegistry;
    
    private final AgentRegistry agentRegistry;
    
    private final NotifierRegistry notifierRegistry;
    
    private final ProcessorRegistry processorRegistry;
    
    // registars
    
    private final ProcessorRegistar processorRegistar;
    
    // electors
    
    private final LeaderElector leaderElector;
    
    private final PoolElector[] poolElectors;
    
    // dispatchers
    
    private final CheckDispatcher checkDispatcher;
    
    private final NotificationDispatcher notificationDispatcher;
    
    private final PoolDispatcher poolDispatcher;
    
    // services
    
    private final Scheduler scheduler;
    
    private final ResultProcessor resultProcessor;
    
    private final ReadingProcessor readingProcessor;
    
    private final AgentRegistrationService agentRegistrationService;
    
    // pool executor
    
    private final ProcessingPools processingPools;
    
    // leader
    
    private final BergamotClusterLeader leader;
    
    // Runner for our processing
    
    private final AtomicBoolean run = new AtomicBoolean(false);
    
    private Thread runner;
    
    private CountDownLatch shutdownLatch;

    public BergamotProcessor(ClusterCfg config, Consumer<Void> onPanic, String application, String info, String hostName) throws Exception
    {
        super(config, onPanic, application, info, hostName);
        // topics
        this.siteEventTopic = new SiteEventTopic(this.hazelcast);
        this.notificationTopic = new SiteNotificationTopic(this.hazelcast);
        this.updateTopic = new SiteUpdateTopic(this.hazelcast);
        // lookups
        this.agentKeyLookup = new AgentKeyClusterLookup(this.hazelcast);
        // registries
        this.workerRegistry = new WorkerRegistry(this.zooKeeper.getZooKeeper());
        this.agentRegistry = new AgentRegistry(this.zooKeeper.getZooKeeper());
        this.notifierRegistry = new NotifierRegistry(this.zooKeeper.getZooKeeper());
        this.processorRegistry = new ProcessorRegistry(this.zooKeeper.getZooKeeper());
        // registars
        this.processorRegistar = new ProcessorRegistar(this.zooKeeper.getZooKeeper());
        // elector
        this.leaderElector = new LeaderElector(this.zooKeeper.getZooKeeper(), this.id);
        // coordinator
        this.poolElectors = new PoolElector[CheckMO.PROCESSING_POOL_COUNT];
        for (int i = 0; i < this.poolElectors.length; i++)
        {
            this.poolElectors[i] = new PoolElector(this.zooKeeper.getZooKeeper(), i, this.id);
        }
        // dispatchers
        this.checkDispatcher = new CheckDispatcher(this.workerRegistry, this.agentRegistry, this.hazelcast);
        this.notificationDispatcher = new NotificationDispatcher(this.notifierRegistry, this.hazelcast);
        this.poolDispatcher = new PoolDispatcher(this.hazelcast);
        // services
        this.scheduler = new WheelScheduler(this.checkDispatcher, this.poolDispatcher);
        this.resultProcessor = new DefaultResultProcessor(this.poolDispatcher, this.notificationDispatcher, this.notificationTopic, this.updateTopic);
        this.readingProcessor = new DefaultReadingProcessor();
        this.agentRegistrationService = new AgentRegistrationService(this.poolDispatcher, this.notificationDispatcher);
        // processing pool executor
        this.processingPools = new ProcessingPools(this.poolElectors, this::createPoolConsumer, this.scheduler, this.resultProcessor, this.readingProcessor, this.agentRegistrationService);
        // leader
        this.leader = new BergamotClusterLeader(this.poolElectors, this.processorRegistry, this.leaderElector);
    }
    
    @Override
    protected void panic()
    {
        // Shutdown this processing service
        this.shutdown();
        // Fire the panic
        super.panic();
    }

    public void start()
    {
        if (this.run.compareAndSet(false, true))
        {
            this.shutdownLatch = new CountDownLatch(1);
            this.runner = new Thread(() -> {
                try
                {
                    logger.info("Bergamot Processor " + this.id + " starting");
                    // Register as a processor
                    this.processorRegistar.registerProcessor(new ProcessorRegistration(this.id, System.currentTimeMillis(), this.application, this.info, this.hostName));
                    // Wait for other processors to join
                    this.waitForProcessors();
                    // Start our processing pool executor
                    this.processingPools.start();
                    // Elect a leader
                    this.leaderElector.elect(this::leaderLauncher);
                }
                catch (Exception e)
                {
                    logger.error("Error starting Bergamot Processor", e);
                }
                finally
                {
                    this.shutdownLatch.countDown();
                }
            }, "bergamot-processor");
            this.runner.start();
        }
    }
    
    protected void waitForProcessors() throws Exception
    {
        if (this.config.getExpectedMembers() > 0)
        {
            int waitFor = (int) Math.ceil(((double) this.config.getExpectedMembers()) / 2d);
            logger.info("Waiting for " + waitFor + " processors to join");
            for (int i = 0; i < 300; i++)
            {
                int count = this.processorRegistry.count();
                if (count >= waitFor)
                    break;
                Thread.sleep(1_000);
            }
        }
    }
    
    protected void leaderLauncher(ElectionState state)
    {
        if (state == ElectionState.LEADER)
        {
            logger.info("Starting cluster leader duties.");
            this.leader.start();
        }
        else
        {
            logger.info("Halting cluster leader duties.");
            this.leader.halt();
        }
    }
    
    public void shutdown()
    {
        this.run.set(false);
        try
        {
            this.leader.halt();
        }
        catch (Exception e)
        {
            logger.error("Failed to halt leader", e);
        }
        try
        {
            this.leaderElector.release();
        }
        catch (Exception e)
        {
            logger.error("Failed to release leader election", e);
        }
        try
        {
            this.shutdownLatch.await();
        }
        catch (InterruptedException e)
        {
        }
    }

    public SiteEventTopic getSiteEventTopic()
    {
        return siteEventTopic;
    }

    public SiteNotificationTopic getNotificationTopic()
    {
        return notificationTopic;
    }

    public SiteUpdateTopic getUpdateTopic()
    {
        return updateTopic;
    }

    public AgentKeyClusterLookup getAgentKeyLookup()
    {
        return this.agentKeyLookup;
    }

    public WorkerRegistry getWorkerRegistry()
    {
        return this.workerRegistry;
    }

    public AgentRegistry getAgentRegistry()
    {
        return this.agentRegistry;
    }

    public NotifierRegistry getNotifierRegistry()
    {
        return this.notifierRegistry;
    }

    public ProcessorRegistry getProcessorRegistry()
    {
        return this.processorRegistry;
    }

    public ProcessorRegistar getProcessorRegistar()
    {
        return this.processorRegistar;
    }

    public LeaderElector getLeaderElector()
    {
        return this.leaderElector;
    }

    public PoolElector[] getPoolElectors()
    {
        return this.poolElectors;
    }

    public CheckDispatcher getCheckDispatcher()
    {
        return this.checkDispatcher;
    }

    public NotificationDispatcher getNotificationDispatcher()
    {
        return this.notificationDispatcher;
    }

    public PoolDispatcher getPoolDispatcher()
    {
        return this.poolDispatcher;
    }

    public Scheduler getScheduler()
    {
        return this.scheduler;
    }

    public ResultProcessor getResultProcessor()
    {
        return this.resultProcessor;
    }

    public ReadingProcessor getReadingProcessor()
    {
        return this.readingProcessor;
    }

    public AgentRegistrationService getAgentRegistrationService()
    {
        return this.agentRegistrationService;
    }
    
    public PoolConsumer createPoolConsumer(int pool)
    {
        return new PoolConsumer(this.hazelcast, pool);
    }

    public ProcessingPools getProcessingPools()
    {
        return this.processingPools;
    }

    public BergamotClusterLeader getLeader()
    {
        return this.leader;
    }
}

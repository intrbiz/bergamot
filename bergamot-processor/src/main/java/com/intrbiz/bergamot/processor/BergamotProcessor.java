package com.intrbiz.bergamot.processor;

import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.AgentRegistrationService;
import com.intrbiz.bergamot.cluster.broker.SiteEventTopic;
import com.intrbiz.bergamot.cluster.broker.SiteNotificationTopic;
import com.intrbiz.bergamot.cluster.broker.SiteUpdateTopic;
import com.intrbiz.bergamot.cluster.consumer.ProcessorConsumer;
import com.intrbiz.bergamot.cluster.consumer.SchedulingPoolConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.CheckDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.NotificationDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.SchedulingPoolDispatcher;
import com.intrbiz.bergamot.cluster.election.LeaderElector;
import com.intrbiz.bergamot.cluster.election.SchedulingPoolElector;
import com.intrbiz.bergamot.cluster.election.model.ElectionState;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyClusterLookup;
import com.intrbiz.bergamot.cluster.member.BergamotMember;
import com.intrbiz.bergamot.cluster.registry.AgentRegistry;
import com.intrbiz.bergamot.cluster.registry.NotifierRegistry;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistar;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistry;
import com.intrbiz.bergamot.config.ClusterCfg;
import com.intrbiz.bergamot.executor.ProcessorExecutor;
import com.intrbiz.bergamot.leader.BergamotClusterLeader;
import com.intrbiz.bergamot.model.message.cluster.ProcessorRegistration;
import com.intrbiz.bergamot.result.DefaultResultProcessor;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.bergamot.scheduler.SchedulingPoolsController;
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
    
    private final SchedulingPoolElector[] poolElectors;
    
    // dispatchers
    
    private final CheckDispatcher checkDispatcher;
    
    private final NotificationDispatcher notificationDispatcher;
    
    private final SchedulingPoolDispatcher poolDispatcher;
    
    private final ProcessorDispatcher processorDispatcher;
    
    // services
    
    private final Scheduler scheduler;
    
    private final ResultProcessor resultProcessor;
    
    private final ReadingProcessor readingProcessor;
    
    private final AgentRegistrationService agentRegistrationService;
    
    // consumer
    
    private final ProcessorConsumer processorConsumer;
    
    // schedule controller
    
    private final SchedulingPoolsController processingPools;
    
    // executor
    
    private final ProcessorExecutor processorExecutor;
    
    // leader
    
    private final BergamotClusterLeader leader;
    
    // Runner for our processing

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
        // TODO: Eventually make scheduling pool count configurable
        this.poolElectors = new SchedulingPoolElector[420];
        for (int i = 0; i < this.poolElectors.length; i++)
        {
            this.poolElectors[i] = new SchedulingPoolElector(this.zooKeeper.getZooKeeper(), i, this.id);
        }
        // dispatchers
        this.checkDispatcher = new CheckDispatcher(this.workerRegistry, this.agentRegistry, this.hazelcast);
        this.notificationDispatcher = new NotificationDispatcher(this.notifierRegistry, this.hazelcast);
        this.poolDispatcher = new SchedulingPoolDispatcher(this.hazelcast);
        this.processorDispatcher = new ProcessorDispatcher(this.hazelcast, this.processorRegistry.getRouteTable());
        // services
        this.scheduler = new WheelScheduler(this.id, this.checkDispatcher, this.poolDispatcher, this.processorDispatcher);
        this.resultProcessor = new DefaultResultProcessor(this.poolDispatcher, this.notificationDispatcher, this.notificationTopic, this.updateTopic);
        this.readingProcessor = new DefaultReadingProcessor();
        this.agentRegistrationService = new AgentRegistrationService(this.poolDispatcher, this.notificationDispatcher, this.poolElectors.length);
        // consumer
        this.processorConsumer = new ProcessorConsumer(this.hazelcast, this.id);
        // scheduling controller
        this.processingPools = new SchedulingPoolsController(this.poolElectors, this.scheduler, this::createPoolConsumer);
        // processor executor
        this.processorExecutor = new ProcessorExecutor(this.resultProcessor, this.readingProcessor, this.agentRegistrationService, this.processorConsumer);
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

    /**
     * Launch this processor
     */
    public void start() throws Exception
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
    
    public void startInBackground()
    {
        new Thread(() -> {
            try
            {
                this.start();
            }
            catch (Exception e)
            {
                logger.error("Error starting Bergamot Processor", e);
            }
        }, "bergamot-processor-launcher").start();
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

    public SchedulingPoolElector[] getPoolElectors()
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

    public SchedulingPoolDispatcher getPoolDispatcher()
    {
        return this.poolDispatcher;
    }
    
    public ProcessorDispatcher getProcessorDispatcher()
    {
        return this.processorDispatcher;
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
    
    public SchedulingPoolConsumer createPoolConsumer(int pool)
    {
        return new SchedulingPoolConsumer(this.hazelcast, pool);
    }

    public SchedulingPoolsController getSchedulingController()
    {
        return this.processingPools;
    }

    public BergamotClusterLeader getLeader()
    {
        return this.leader;
    }

    public ProcessorConsumer getProcessorConsumer()
    {
        return this.processorConsumer;
    }

    public ProcessorExecutor getProcessorExecutor()
    {
        return this.processorExecutor;
    }
    
    public int getSchedulingPoolCount()
    {
        return this.poolElectors.length;
    }
}

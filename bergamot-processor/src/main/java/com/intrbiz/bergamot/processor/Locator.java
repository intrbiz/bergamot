package com.intrbiz.bergamot.processor;

import com.intrbiz.bergamot.agent.AgentProcessorService;
import com.intrbiz.bergamot.cluster.broker.SchedulingTopic;
import com.intrbiz.bergamot.cluster.broker.SiteEventTopic;
import com.intrbiz.bergamot.cluster.broker.SiteNotificationTopic;
import com.intrbiz.bergamot.cluster.broker.SiteUpdateTopic;
import com.intrbiz.bergamot.cluster.consumer.ProcessorConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.NotificationDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.ProxyDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.WorkerDispatcher;
import com.intrbiz.bergamot.cluster.election.LeaderElector;
import com.intrbiz.bergamot.cluster.election.SchedulingPoolElector;
import com.intrbiz.bergamot.cluster.registry.AgentRegistry;
import com.intrbiz.bergamot.cluster.registry.NotifierRegistry;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistar;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;
import com.intrbiz.bergamot.cluster.registry.ProxyRegistry;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistry;
import com.intrbiz.bergamot.executor.ProcessorExecutor;
import com.intrbiz.bergamot.leader.BergamotClusterLeader;
import com.intrbiz.bergamot.proxy.ProxyProcessorService;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.bergamot.scheduler.SchedulingPoolsController;
import com.intrbiz.lamplighter.reading.ReadingProcessor;

public interface Locator
{
    SiteEventTopic getSiteEventTopic();

    SiteNotificationTopic getNotificationTopic();

    SiteUpdateTopic getUpdateTopic();

    SchedulingTopic getSchedulingTopic();

    WorkerRegistry getWorkerRegistry();

    AgentRegistry getAgentRegistry();

    NotifierRegistry getNotifierRegistry();

    ProcessorRegistry getProcessorRegistry();

    ProxyRegistry getProxyRegistry();

    ProcessorRegistar getProcessorRegistar();

    LeaderElector getLeaderElector();

    SchedulingPoolElector[] getPoolElectors();

    WorkerDispatcher getWorkerDispatcher();

    NotificationDispatcher getNotificationDispatcher();
    
    ProcessorDispatcher getProcessorDispatcher();

    ProxyDispatcher getProxyDispatcher();

    Scheduler getScheduler();

    ResultProcessor getResultProcessor();

    ReadingProcessor getReadingProcessor();

    AgentProcessorService getAgentProcessorService();
    
    ProxyProcessorService getProxyProcessorService();

    SchedulingPoolsController getSchedulingController();

    BergamotClusterLeader getLeader();

    ProcessorConsumer getProcessorConsumer();

    ProcessorExecutor getProcessorExecutor();
    
    int getSchedulingPoolCount();
}

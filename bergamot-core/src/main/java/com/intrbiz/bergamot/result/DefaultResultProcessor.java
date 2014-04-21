package com.intrbiz.bergamot.result;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.ResultProcessorCfg;
import com.intrbiz.bergamot.engine.AbstractEngine;
import com.intrbiz.bergamot.model.Checkable;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.result.Result;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.GenericKey;
import com.intrbiz.queue.name.Queue;

public class DefaultResultProcessor extends AbstractEngine<ResultProcessorCfg> implements ResultProcessor, DeliveryHandler<Message>
{
    private Logger logger = Logger.getLogger(DefaultResultProcessor.class);

    private List<Consumer<Message, GenericKey>> consumers = new LinkedList<Consumer<Message, GenericKey>>();

    public DefaultResultProcessor()
    {
        super();
    }

    @Override
    protected void configure() throws Exception
    {
    }

    @Override
    public void start()
    {
        // setup the consumer
        logger.info("Creating result consumer");
        Exchange exchange = this.config.getExchange().asExchange();
        Queue queue = this.config.getQueue() == null ? null : this.config.getQueue().asQueue();
        GenericKey[] bindings = this.config.asBindings();
        // create the consumer
        for (int i = 0; i < (Runtime.getRuntime().availableProcessors() * 2); i++)
        {
            this.consumers.add(this.getBergamot().getManifold().setupConsumer(this, exchange, queue, bindings));
        }
    }

    public List<Consumer<Message, GenericKey>> getConsumers()
    {
        return this.consumers;
    }

    protected void processResult(Result result)
    {
        logger.info("Got result " + result.getId() + " for '" + result.getCheckableType() + "' " + result.getCheckableId() + " [" + (result.getCheck() == null ? "" : result.getCheck().getName()) + "] => " + result.isOk() + " " + result.getStatus() + " " + result.getOutput() + " took " + result.getRuntime() + " ms.");
        // stamp in processed time
        result.setProcessed(System.currentTimeMillis());
        // update the state
        Checkable checkable = this.getBergamot().getObjectStore().lookupCheckable(result.getCheckableType(), result.getCheckableId());
        if (checkable != null)
        {
            CheckState state = checkable.getState();
            // transition the state
            boolean isStateChange = this.transitionState(checkable, state, result);
            // stats
            this.computeStats(state, result);
            // is this an alert
            if (isStateChange)
            {
                logger.info("Got state change for " + checkable + " " + state.isOk() + " " + state.getStatus() + " " + state.isHard() + " " + state.getOutput());
                if (state.isOk())
                {
                    this.handleRecovery(checkable, state, result);
                }
                else
                {
                    this.handleAlert(checkable, state, result);
                }
            }
            else
            {
                logger.info("Got steady state for " + checkable + " " + state.isOk() + " " + state.getStatus() + " " + state.isHard() + " " + state.getOutput());
                this.handleSteadyState(checkable, state, result);
            }
        }
        else
        {
            logger.warn("Could not find " + result.getCheckableType() + " " + result.getCheckableId() + " for result " + result.getId());
        }
    }
    
    protected void handleSteadyState(Checkable checkable, CheckState state, Result result)
    {
        // steady state, NB: this could be good or bad!
    }
    
    protected void handleAlert(Checkable checkable, CheckState state, Result result)
    {
        // alert
        this.getBergamot().getObjectStore().addRecentCheck(checkable);
        this.getBergamot().getScheduler().reschedule(checkable);
        logger.warn("Alert for " + checkable);
    }
    
    protected void handleRecovery(Checkable checkable, CheckState state, Result result)
    {
        // recovery
        this.getBergamot().getObjectStore().removeRecentCheck(checkable);
        this.getBergamot().getScheduler().reschedule(checkable);
        logger.warn("Recovery for " + checkable);
    }

    protected boolean transitionState(Checkable checkable, CheckState state, Result result)
    {
        // is state change
        boolean isStateChange = state.isOk() ^ result.isOk();
        logger.debug("IsStateChange: " + state.isOk() + " ^ " + result.isOk() + " = " + isStateChange);
        // attempts
        if (isStateChange)
        {
            state.setAttempt(0);
            state.setHard(false);
            state.setLastStateChange(System.currentTimeMillis());
        }
        else if (!state.isHard())
        {
            int attempt = state.getAttempt() + 1;
            if (attempt > checkable.getMaxCheckAttempts()) attempt = checkable.getMaxCheckAttempts();
            state.setAttempt(attempt);
            state.setHard(attempt >= checkable.getMaxCheckAttempts());
        }
        // update the state
        state.setLastCheckId(result.getId());
        state.setLastCheckTime(result.getExecuted());
        state.setOk(result.isOk());
        state.setStatus(result.getStatus());
        state.setOutput(result.getOutput());
        return isStateChange;
    }

    protected void computeStats(CheckState state, Result result)
    {
        // stats
        state.setLastRuntime(result.getRuntime());
        state.setAverageRuntime(state.getAverageRuntime() == 0 ? state.getLastRuntime() : ((state.getLastRuntime() + state.getAverageRuntime()) / 2D));
        // check latencies
        if (result.getCheck() != null)
        {
            state.setLastCheckProcessingLatency(result.getProcessed() - result.getCheck().getScheduled());
            state.setLastCheckExecutionLatency(result.getExecuted() - result.getCheck().getScheduled());
            // moving average
            state.setAverageCheckExecutionLatency(state.getAverageCheckExecutionLatency() == 0 ? state.getLastCheckExecutionLatency() : ((state.getAverageCheckExecutionLatency() + state.getLastCheckExecutionLatency()) / 2D));
            state.setAverageCheckProcessingLatency(state.getAverageCheckProcessingLatency() == 0 ? state.getLastCheckProcessingLatency() : ((state.getAverageCheckProcessingLatency() + state.getLastCheckProcessingLatency()) / 2D));
            // log
            logger.debug("Last check latency: processing => " + state.getLastCheckProcessingLatency() + "ms, execution => " + state.getLastCheckExecutionLatency() + "ms processes");
        }
    }

    @Override
    public void handleDevliery(Message event) throws IOException
    {
        if (event instanceof Result)
        {
            logger.trace("Got result: " + event);
            this.processResult((Result) event);
        }
        else
        {
            logger.warn("Got non-result message, ignoring: " + event);
        }
    }
}

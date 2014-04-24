package com.intrbiz.bergamot.result;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.component.AbstractComponent;
import com.intrbiz.bergamot.config.ResultProcessorCfg;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.GenericKey;
import com.intrbiz.queue.name.Queue;

public class DefaultResultProcessor extends AbstractComponent<ResultProcessorCfg> implements ResultProcessor, DeliveryHandler<Message>
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
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++)
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
        logger.info("Got result " + result.getId() + " for '" + result.getCheckType() + "' " + result.getCheckId() + " [" + (result.getCheck() == null ? "" : result.getCheck().getName()) + "] => " + result.isOk() + " " + result.getStatus() + " " + result.getOutput() + " took " + result.getRuntime() + " ms.");
        // stamp in processed time
        result.setProcessed(System.currentTimeMillis());
        // update the state
        Check check = this.getBergamot().getObjectStore().lookupCheckable(result.getCheckType(), result.getCheckId());
        if (check != null)
        {
            CheckState state = check.getState();
            // lock the state
            try
            {
                state.getLock().lock();
                // apply the result
                boolean isHardStateChange = this.applyResult(check, state, result);
                logger.info("State change: hard: " + isHardStateChange + ", transitioning: " + state.isTransitioning() + ", attempt: " + state.getAttempt());
                // reschedule
                if (state.isTransitioning())
                {
                    logger.info("Rescheduling " + check + " due to state change");
                    this.getBergamot().getScheduler().reschedule(check);
                }
                // update any dependencies
                this.updateDependencies(check, state, result);
                // stats
                this.computeStats(state, result);
                // send notifications
                if (isHardStateChange)
                {
                    if (state.isOk())
                    {
                        // send recovery
                        this.sendRecovery(check, state, result);
                    }
                    else
                    {
                        // send alert
                        this.sendAlert(check, state, result);
                    }
                }
                // send the general state update notifications
                this.sendStateUpdate(check, state, result);
            }
            finally
            {
                state.getLock().unlock();
            }
        }
        else
        {
            logger.warn("Could not find " + result.getCheckType() + " " + result.getCheckId() + " for result " + result.getId());
        }
    }
    
    protected void sendStateUpdate(Check check, CheckState state, Result result)
    {
        logger.info("State update " + check + " is " + state.isOk() + " " + state.isHard() + " " + state.getStatus() + " " + state.getOutput());
    }
    
    protected void sendRecovery(Check check, CheckState state, Result result)
    {
        logger.warn("Recovery for " + check);
        this.getBergamot().getObjectStore().removeAlert(check);
    }
    
    protected void sendAlert(Check check, CheckState state, Result result)
    {
        logger.warn("Alert for " + check);
        if (! check.isSuppressed())
        {
            this.getBergamot().getObjectStore().addAlert(check);    
        }
    }

    /**
     * Apply the result to the current state of the check
     * @return true if a hard state change happened
     */
    protected boolean applyResult(Check check, CheckState state, Result result)
    {
        // is the state changing
        boolean isTransition  = state.isOk() ^ result.isOk();
        boolean isFlapping    = isTransition & state.isTransitioning();
        // update the state
        state.setLastCheckId(result.getId());
        state.setLastCheckTime(result.getExecuted());
        state.setOk(result.isOk());
        state.pushOkHistory(result.isOk());
        state.setStatus(result.getStatus());
        state.setOutput(result.getOutput());
        state.setFlapping(isFlapping);
        // apply thresholds
        if (isTransition)
        {
            state.setAttempt(1);
            state.setHard(false);
            state.setLastStateChange(System.currentTimeMillis());
            state.setTransitioning(true);
        }
        else if (!state.isHard())
        {
            int attempt = state.getAttempt() + 1;
            if (attempt > check.getCurrentAttemptThreshold()) attempt = check.getCurrentAttemptThreshold();
            state.setAttempt(attempt);
            state.setHard(attempt >= check.getCurrentAttemptThreshold());
            if (state.isHard())
            {
                state.setTransitioning(false);
            }
            return state.isHard();
        }
        return false;
    }
    
    protected void updateDependencies(Check check, CheckState state, Result result)
    {
        for (Check referencedBy : check.getReferences())
        {
            logger.info("Propagating result to check " + referencedBy);
        }
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

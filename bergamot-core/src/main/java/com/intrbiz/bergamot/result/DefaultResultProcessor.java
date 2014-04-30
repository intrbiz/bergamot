package com.intrbiz.bergamot.result;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.component.AbstractComponent;
import com.intrbiz.bergamot.config.ResultProcessorCfg;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.model.stats.ActiveCheckStats;
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
        // some validation
        if ((System.currentTimeMillis() - result.getExecuted()) > TimeUnit.MINUTES.toMillis(15))
        {
            logger.warn("Not processing result, it is over 15 minutes old.");
            return;
        }
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
                    if (check instanceof ActiveCheck)
                    {
                        logger.info("Rescheduling " + check + " due to state change");
                        this.getBergamot().getScheduler().reschedule((ActiveCheck) check);
                    }
                }
                // update any dependencies
                this.updateDependencies(check, state, result);
                // stats
                if (check instanceof ActiveCheck)
                {
                    this.computeStats(((ActiveCheck) check).getStats(), result);
                }
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
        logger.debug("Recovery for " + check);
        this.getBergamot().getObjectStore().removeAlert(check);
        if (check.isNotificationsEnabled() && (!check.isSuppressed()))
        {
            // send a recovery
            // check notification period
            if (check.getNotificationPeriod() == null || check.getNotificationPeriod().isInTimeRange(Calendar.getInstance()))
            {
                SendRecovery recovery = new SendRecovery();
                recovery.setRaised(System.currentTimeMillis());
                recovery.setCheck(check.toMO());
                for (Contact contact : check.getContacts())
                {
                    if (contact.isNotificationsEnabled() && contact.getNotificationPeriod() == null || contact.getNotificationPeriod().isInTimeRange(Calendar.getInstance()))
                    {
                        recovery.getTo().add(contact.toMO());
                    }
                }
                if (!recovery.getTo().isEmpty())
                {
                    logger.warn("Sending recovery for " + check);
                    this.getBergamot().getManifold().publish(recovery);
                }
                else
                {
                    logger.warn("Not sending recovery for " + check + " no contacts configured.");
                }
            }
        }
    }

    protected void sendAlert(Check check, CheckState state, Result result)
    {
        logger.debug("Alert for " + check);
        if (check.isNotificationsEnabled() && (!check.isSuppressed()))
        {
            this.getBergamot().getObjectStore().addAlert(check);
            // send an alert
            // check the notification period
            if (check.getNotificationPeriod() == null || check.getNotificationPeriod().isInTimeRange(Calendar.getInstance()))
            {
                SendAlert alert = new SendAlert();
                alert.setRaised(System.currentTimeMillis());
                alert.setCheck(check.toMO());
                for (Contact contact : check.getContacts())
                {
                    if (contact.isNotificationsEnabled() && contact.getNotificationPeriod() == null || contact.getNotificationPeriod().isInTimeRange(Calendar.getInstance()))
                    {
                        alert.getTo().add(contact.toMO());
                    }
                }
                if (!alert.getTo().isEmpty())
                {
                    logger.warn("Sending alert for " + check);
                    this.getBergamot().getManifold().publish(alert);
                }
                else
                {
                    logger.warn("Not sending alert for " + check + " no contacts configured.");
                }
            }
        }
    }

    /**
     * Apply the result to the current state of the check
     * 
     * @return true if a hard state change happened
     */
    protected boolean applyResult(Check check, CheckState state, Result result)
    {
        // is the state changing
        boolean isTransition = state.isOk() ^ result.isOk();
        boolean isFlapping = isTransition & state.isTransitioning();
        // copy the last hard state
        if (isTransition && state.isHard())
        {
            state.setLastHardOk(state.isOk());
            state.setLastHardStatus(state.getStatus());
            state.setLastHardOutput(state.getOutput());
        }
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

    protected void computeStats(ActiveCheckStats stats, Result result)
    {
        // stats
        stats.setLastRuntime(result.getRuntime());
        stats.setAverageRuntime(stats.getAverageRuntime() == 0 ? stats.getLastRuntime() : ((stats.getLastRuntime() + stats.getAverageRuntime()) / 2D));
        // check latencies
        if (result.getCheck() != null)
        {
            stats.setLastCheckProcessingLatency(result.getProcessed() - result.getCheck().getScheduled());
            stats.setLastCheckExecutionLatency(result.getExecuted() - result.getCheck().getScheduled());
            // moving average
            stats.setAverageCheckExecutionLatency(stats.getAverageCheckExecutionLatency() == 0 ? stats.getLastCheckExecutionLatency() : ((stats.getAverageCheckExecutionLatency() + stats.getLastCheckExecutionLatency()) / 2D));
            stats.setAverageCheckProcessingLatency(stats.getAverageCheckProcessingLatency() == 0 ? stats.getLastCheckProcessingLatency() : ((stats.getAverageCheckProcessingLatency() + stats.getLastCheckProcessingLatency()) / 2D));
            // log
            logger.debug("Last check latency: processing => " + stats.getLastCheckProcessingLatency() + "ms, execution => " + stats.getLastCheckExecutionLatency() + "ms processes");
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

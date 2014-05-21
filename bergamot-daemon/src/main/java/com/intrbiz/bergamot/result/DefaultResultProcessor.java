package com.intrbiz.bergamot.result;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.component.AbstractComponent;
import com.intrbiz.bergamot.config.ResultProcessorCfg;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.NotificationType;
import com.intrbiz.bergamot.model.RealCheck;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.VirtualCheck;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.notification.Notification;
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
        // basic input validation
        // we discard results older than 15 minutes - this is intended
        // to ensure better operation when the system is under extreme load
        // and or a large number of checks are timing out, causing large execution
        // latencies.
        if ((System.currentTimeMillis() - result.getExecuted()) > TimeUnit.MINUTES.toMillis(15))
        {
            logger.warn("Not processing result, it is over 15 minutes old.");
            return;
        }
        logger.info("Got result " + result.getId() + " for '" + result.getCheckType() + "' " + result.getCheckId() + " [" + (result.getCheck() == null ? "" : result.getCheck().getName()) + "] => " + result.isOk() + " " + result.getStatus() + " " + result.getOutput() + " took " + result.getRuntime() + " ms.");
        // stamp in processed time
        result.setProcessed(System.currentTimeMillis());
        // update the state
        Check<?> check = this.getBergamot().getObjectStore().lookupCheckable(result.getCheckType(), result.getCheckId());
        if (check instanceof RealCheck)
        {
            // should be process this result?
            if (!check.isEnabled())
            {
                logger.warn("Discarding result " + result.getId() + " for " + result.getCheckType() + " " + result.getCheckId() + " because it is disabled.");
                return;
            }
            // apply the result
            CheckState state = check.getState();
            // apply the result
            boolean isHardStateChange = this.applyResult((RealCheck<?>) check, result);
            logger.info("State change: hard: " + isHardStateChange + ", transitioning: " + state.isTransitioning() + ", attempt: " + state.getAttempt());
            // reschedule
            if (state.isTransitioning())
            {
                if (check instanceof ActiveCheck)
                {
                    logger.info("Rescheduling " + check + " due to state change");
                    this.getBergamot().getScheduler().reschedule((ActiveCheck<?>) check);
                }
            }
            // stats
            if (check instanceof ActiveCheck)
            {
                this.computeStats(((ActiveCheck<?>) check).getStats(), result);
            }
            // send the general state update notifications
            this.sendStateUpdate(check);
            // send notifications
            if (isHardStateChange)
            {
                if (state.isOk())
                {
                    // send recovery
                    this.sendRecovery(check);
                }
                else
                {
                    // send alert
                    this.sendAlert(check);
                }
            }
            // update any virtual checks
            this.updateVirtualChecks(check, result, isHardStateChange);
        }
        else
        {
            logger.warn("Could not find " + result.getCheckType() + " " + result.getCheckId() + " for result " + result.getId());
        }
    }

    protected void sendStateUpdate(Check<?> check)
    {
        CheckState state = check.getState();
        logger.info("State update " + check + " is " + state.isOk() + " " + state.isHard() + " " + state.getStatus() + " " + state.getOutput());
    }

    protected <T extends Notification> T createNotification(Check<?> check, Calendar now, NotificationType type, Supplier<T> ctor)
    {
        T notification = ctor.get();
        // compute the engines available
        final Set<String> enabledEngines = check.getNotifications().getEnginesEnabledAt(type, check.getState().getStatus(), now);
        // send
        notification.setRaised(System.currentTimeMillis());
        notification.setCheck(check.toMO());
        // to
        notification.setTo(check.getAllContacts().stream().filter((contact) -> {
            return contact.getNotifications().isEnabledAt(type, check.getState().getStatus(), now);
        }).map((contact) -> {
            ContactMO cmo = contact.toMO();
            cmo.setEngines(contact.getNotifications().getEnginesEnabledAt(type, check.getState().getStatus(), now).stream().filter((engine) -> {
                return check.getNotifications().isAllEnginesEnabled() || enabledEngines.contains(engine);
            }).collect(Collectors.toSet()));
            return cmo;
        }).collect(Collectors.toList()));
        return notification;
    }

    protected void sendRecovery(Check<?> check)
    {
        logger.warn("Recovery for " + check);
        this.getBergamot().getObjectStore().removeAlert(check);
        if (!check.isSuppressed())
        {
            Calendar now = Calendar.getInstance();
            // send?
            if (check.getNotifications().isEnabledAt(NotificationType.RECOVERY, check.getState().getStatus(), now))
            {
                SendRecovery recovery = this.createNotification(check, now, NotificationType.RECOVERY, SendRecovery::new);
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

    protected void sendAlert(Check<?> check)
    {
        logger.warn("Alert for " + check);
        if (!check.isSuppressed())
        {
            this.getBergamot().getObjectStore().addAlert(check);
            Calendar now = Calendar.getInstance();
            // send?
            if (check.getNotifications().isEnabledAt(NotificationType.ALERT, check.getState().getStatus(), now))
            {
                SendAlert alert = this.createNotification(check, now, NotificationType.ALERT, SendAlert::new);
                if (!alert.getTo().isEmpty())
                {
                    logger.warn("Sending alert for " + check);
                    System.out.println(new BergamotTranscoder().encodeAsString(alert));
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
    protected boolean applyResult(RealCheck<?> check, Result result)
    {
        CheckState state = check.getState();
        state.getLock().lock();
        try
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
            state.setStatus(Status.valueOf(result.getStatus().toUpperCase()));
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
                return state.isHard() && (state.isOk() ^ state.isLastHardOk());
            }
        }
        finally
        {
            state.getLock().unlock();
        }
        return false;
    }
    
    // virtual check handling

    protected void updateVirtualChecks(Check<?> check, Result result, boolean isHardStateChange)
    {
        if (isHardStateChange)
        {
            for (VirtualCheck<?> referencedBy : check.getReferencedBy())
            {
                logger.info("Propagating result to check " + referencedBy);
                // update the status of the check
                boolean ok = referencedBy.getCondition().computeOk();
                Status status = referencedBy.getCondition().computeStatus();
                // update the check
                boolean isStateChange = this.applyVirtualResult(referencedBy, ok, status, result);
                logger.info("State change for virtual check " + referencedBy + " to " + check.getState().isOk() + " " + check.getState().getStatus() + " caused by " + check);
                // send the general state update notifications
                this.sendStateUpdate(check);
                // send notifications
                if (isStateChange)
                {
                    if (referencedBy.getState().isOk())
                    {
                        // send recovery
                        this.sendRecovery(referencedBy);
                    }
                    else
                    {
                        // send alert
                        this.sendAlert(referencedBy);
                    }
                }                
            }
        }
    }

    protected boolean applyVirtualResult(VirtualCheck<?> check, boolean ok, Status status, Result cause)
    {
        CheckState state = check.getState();
        state.getLock().lock();
        try
        {
            boolean isStateChange = state.isOk() ^ ok;
            // always in a hard state
            // as the hard / soft state logic has already happened
            state.setHard(true);
            state.setAttempt(0);
            state.setTransitioning(false);
            state.setFlapping(false);
            // is it a state change
            if (isStateChange)
            {
                state.setLastStateChange(System.currentTimeMillis());
                // copy the last hard state
                state.setLastHardOk(state.isOk());
                state.setLastHardStatus(state.getStatus());
                state.setLastHardOutput(state.getOutput());
            }
            // update the state
            state.setLastCheckId(cause.getId());
            state.setLastCheckTime(cause.getExecuted());
            state.setOk(ok);
            state.pushOkHistory(ok);
            state.setStatus(status);
            // no output
            state.setOutput(null);
            // is state change
            return isStateChange;
        }
        finally
        {
            state.getLock().unlock();
        }
    }
    
    // stats

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
    
    // glue

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

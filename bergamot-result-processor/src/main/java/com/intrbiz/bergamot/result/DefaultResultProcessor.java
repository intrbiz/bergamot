package com.intrbiz.bergamot.result;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.NotificationType;
import com.intrbiz.bergamot.model.RealCheck;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.VirtualCheck;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.bergamot.model.state.CheckState;

public class DefaultResultProcessor extends AbstractResultProcessor
{
    private Logger logger = Logger.getLogger(DefaultResultProcessor.class);

    public DefaultResultProcessor()
    {
        super();
    }    

    @Override
    public void processDead(ExecuteCheck check)
    {
        // we failed to execute the given check in time, oops!
        logger.warn("Failed to execute check, workers aren't working hard enough: " + check.getId() + "\r\n" + check);
        // fake a timeout result and submit it
        Result result = check.createResult();
        result.setOk(false);
        result.setStatus(Status.TIMEOUT.toString());
        result.setOutput("Worker timeout whilst executing check");
        result.setRuntime(0);
        this.processExecuted(result);
    }

    @Override
    public void processExecuted(Result result)
    {
        logger.info("Got result " + result.getId() + " for '" + result.getCheckType() + "' " + result.getCheckId() + " [" + (result.getCheck() == null ? "" : result.getCheck().getName()) + "] => " + result.isOk() + " " + result.getStatus() + " " + result.getOutput() + " took " + result.getRuntime() + " ms.");
        // stamp in processed time
        result.setProcessed(System.currentTimeMillis());
        // start the transaction
        try (BergamotDB db = BergamotDB.connect())
        {
            db.execute(() -> {
                // update the state
                Check<?, ?> check = db.getCheck(result.getCheckId());
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
                    boolean isHardStateChange = this.applyResult((RealCheck<?, ?>) check, state, result);
                    logger.info("State change: hard: " + isHardStateChange + ", transitioning: " + state.isTransitioning() + ", attempt: " + state.getAttempt());
                    // stats
                    this.computeStats(check, state, result);
                    // update the check state
                    db.setCheckState(state);
                    db.commit();
                    // reschedule?
                    if (state.isTransitioning())
                    {
                        if (check instanceof ActiveCheck)
                        {
                            this.rescheduleCheck((ActiveCheck<?,?>) check);
                        }
                    }
                    // send the general state update notifications
                    this.sendStateUpdate(check);
                    // send notifications
                    if (isHardStateChange)
                    {
                        if (state.isOk())
                        {
                            // send recovery
                            this.sendRecovery(check, db);
                        }
                        else
                        {
                            // send alert
                            this.sendAlert(check, db);
                        }
                    }
                    // update any virtual checks
                    this.updateVirtualChecks(check, state, result, isHardStateChange, db);
                }
                else
                {
                    logger.warn("Could not find " + result.getCheckType() + " " + result.getCheckId() + " for result " + result.getId());
                }
            });
        }
    }

    protected void sendStateUpdate(Check<?, ?> check)
    {
        CheckState state = check.getState();
        logger.info("State update " + check + " is " + state.isOk() + " " + state.isHard() + " " + state.getStatus() + " " + state.getOutput());
        // send the update
        this.publishUpdate(check, new Update(check.toMO()));
    }

    protected <T extends Notification> T createNotification(Check<?, ?> check, Alert alertRecord, Calendar now, NotificationType type, Supplier<T> ctor)
    {
        T notification = ctor.get();
        // alert id
        notification.setAlertId(alertRecord.getId());
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

    protected void sendRecovery(Check<?, ?> check, BergamotDB db)
    {
        logger.warn("Recovery for " + check);
        if (!check.isSuppressed())
        {
            // ger the current alert for this check
            Alert alertRecord = db.getCurrentAlertForCheck(check.getId());
            alertRecord.setRecovered(true);
            alertRecord.setRecoveredAt(new Timestamp(System.currentTimeMillis()));
            alertRecord.setRecoveredBy(check.getState().getLastCheckId());
            db.setAlert(alertRecord);
            // send notifications?
            Calendar now = Calendar.getInstance();
            // send?
            if (check.getNotifications().isEnabledAt(NotificationType.RECOVERY, check.getState().getStatus(), now))
            {
                SendRecovery recovery = this.createNotification(check, alertRecord, now, NotificationType.RECOVERY, SendRecovery::new);
                if (!recovery.getTo().isEmpty())
                {
                    logger.warn("Sending recovery for " + check);
                    this.publishNotification(check, recovery);
                }
                else
                {
                    logger.warn("Not sending recovery for " + check + " no contacts configured.");
                }
            }
        }
    }

    protected void sendAlert(Check<?, ?> check, BergamotDB db)
    {
        logger.warn("Alert for " + check);
        if (!check.isSuppressed())
        {
            // record the alert
            Alert alertRecord = new Alert(check, check.getState());
            db.setAlert(alertRecord);
            // send the notifications
            Calendar now = Calendar.getInstance();
            // send?
            if (check.getNotifications().isEnabledAt(NotificationType.ALERT, check.getState().getStatus(), now))
            {
                SendAlert alert = this.createNotification(check, alertRecord, now, NotificationType.ALERT, SendAlert::new);
                if (!alert.getTo().isEmpty())
                {
                    logger.warn("Sending alert for " + check);
                    System.out.println(new BergamotTranscoder().encodeAsString(alert));
                    this.publishNotification(check, alert);
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
    protected boolean applyResult(RealCheck<?, ?> check, CheckState state, Result result)
    {
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
            state.setLastCheckTime(new Timestamp(result.getExecuted()));
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
                state.setLastStateChange(new Timestamp(System.currentTimeMillis()));
                state.setTransitioning(true);
            }
            else if (! (state.isHard() && state.getAttempt() >= check.getCurrentAttemptThreshold(state)))
            {
                int attempt = state.getAttempt() + 1;
                if (attempt > check.getCurrentAttemptThreshold(state)) attempt = check.getCurrentAttemptThreshold(state);
                state.setAttempt(attempt);
                state.setHard(attempt >= check.getCurrentAttemptThreshold(state));
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

    protected void updateVirtualChecks(Check<?, ?> check, CheckState state, Result result, boolean isHardStateChange, BergamotDB db)
    {
        for (VirtualCheck<?, ?> referencedBy : check.getReferencedBy())
        {
            logger.info("Propagating result to check " + referencedBy);
            // get the state
            CheckState referencedByState = referencedBy.getState();
            // update the status of the check
            boolean ok = referencedBy.getCondition().computeOk();
            Status status = referencedBy.getCondition().computeStatus();
            // update the check
            boolean isStateChange = this.applyVirtualResult(referencedBy, referencedByState, ok, status, result);
            logger.info("State change for virtual check " + referencedBy + " to " + check.getState().isOk() + " " + check.getState().getStatus() + " caused by " + check);
            // update the state
            db.setCheckState(referencedByState);
            db.commit();
            // send the general state update notifications
            this.sendStateUpdate(referencedBy);
            // send notifications
            // only send notifications when a dependent check has reached a hard state change
            if (isStateChange && isHardStateChange)
            {
                if (referencedBy.getState().isOk())
                {
                    // send recovery
                    this.sendRecovery(referencedBy, db);
                }
                else
                {
                    // send alert
                    this.sendAlert(referencedBy, db);
                }
            }
        }
    }

    protected boolean applyVirtualResult(VirtualCheck<?, ?> check, CheckState state, boolean ok, Status status, Result cause)
    {
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
                state.setLastStateChange(new Timestamp(System.currentTimeMillis()));
                // copy the last hard state
                state.setLastHardOk(state.isOk());
                state.setLastHardStatus(state.getStatus());
                state.setLastHardOutput(state.getOutput());
            }
            // update the state
            state.setLastCheckId(cause.getId());
            state.setLastCheckTime(new Timestamp(cause.getExecuted()));
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
    
    protected void computeStats(Check<?,?> check, CheckState state, Result result)
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
    
}
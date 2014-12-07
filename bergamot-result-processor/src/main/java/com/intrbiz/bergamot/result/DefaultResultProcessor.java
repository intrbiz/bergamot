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
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
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
        this.processExecuted(new Result().fromCheck(check).timeout("Worker timeout whilst executing check"));
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
                        logger.warn("Discarding result " + result.getId() + " for " + result.getCheckType() + "::" + result.getCheckId() + " because it is disabled.");
                        return;
                    }
                    // apply the result
                    Transition transition = this.computeResultTransition((RealCheck<?, ?>) check, check.getState(), result);
                    logger.info("State change for " + result.getCheckType() + "::" + result.getCheckId() + " => hard state change: " + transition.hardChange + ", state change: " + transition.stateChange);
                    // stats
                    this.computeStats(check, transition.nextState, result);
                    // update the check state
                    db.setCheckState(transition.nextState);
                    db.commit();
                    // reschedule active checks if they are in transition 
                    // or have changed hard state
                    // or the ok state has changed
                    // or the hard state has changed
                    if (check instanceof ActiveCheck && (transition.stateChange || transition.hardChange))
                    {
                        // inform the scheduler to reschedule this check
                        logger.info("Sending reschedule for " + result.getCheckType() + "::" + result.getCheckId() + " => hard state change: " + transition.hardChange + ", state change: " + transition.stateChange);
                        this.rescheduleCheck((ActiveCheck<?,?>) check);
                    }
                    // send the general state update notifications
                    this.sendStateUpdate(check);
                    // send notifications
                    if (transition.hardChange)
                    {
                        if (check.getState().isOk())
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
                    this.updateVirtualChecks(check, transition, result, db);
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

    protected <T extends CheckNotification> T createNotification(Check<?, ?> check, Alert alertRecord, Calendar now, NotificationType type, Supplier<T> ctor)
    {
        T notification = ctor.get();
        // the site
        notification.setSite(check.getSite().toMO());
        notification.setRaised(System.currentTimeMillis());
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
        if (!check.isSuppressedOrInDowntime())
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
        if (!check.isSuppressedOrInDowntime())
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
    
    protected Transition computeResultTransition(RealCheck<?,?> check, CheckState currentState, Result result)
    {
        // validate that status matches ok
        Status resultStatus = Status.parse(result.getStatus());
        if (result.isOk() != resultStatus.isOk())
        {
            result.setOk(resultStatus.isOk());
        }
        // the next state
        CheckState nextState = currentState.clone();
        // apply the result to the next state
        nextState.setOk(result.isOk());
        nextState.pushOkHistory(result.isOk());
        nextState.setStatus(resultStatus);
        nextState.setOutput(result.getOutput());
        nextState.setLastRuntime(result.getRuntime());
        nextState.setLastCheckTime(new Timestamp(result.getExecuted()));
        // compute the transition
        // do we have a state change
        if (currentState.isOk() ^ nextState.isOk())
        {
            // we've changed state
            nextState.setLastStateChange(new Timestamp(System.currentTimeMillis()));
            // begin or immediately transition
            if (check.computeCurrentAttemptThreshold(nextState) > 1)
            {
                // start the transition
                nextState.setHard(false);
                nextState.setTransitioning(true);
                nextState.setAttempt(1);               
                return new Transition(currentState, nextState, true, false);
            }
            else
            {
                // immediately enter hard state
                nextState.setHard(true);
                nextState.setTransitioning(false);
                nextState.setAttempt(check.computeCurrentAttemptThreshold(nextState));
                return new Transition(currentState, nextState, true, true);
            }
        }
        else if (currentState.isHard())
        {
            // steady state
            return new Transition(currentState, nextState, false, false);
        }
        else
        {
            // during transition
            nextState.setAttempt(currentState.getAttempt() + 1);
            // have we reached a hard state
            if (nextState.getAttempt() >= check.computeCurrentAttemptThreshold(nextState))
            {
                nextState.setHard(true);
                nextState.setTransitioning(false);
                nextState.setAttempt(check.computeCurrentAttemptThreshold(nextState));
                return new Transition(currentState, nextState, false, true);
            }
            else
            {
                return new Transition(currentState, nextState, false, false);
            }
        }
    }

    // virtual check handling

    protected void updateVirtualChecks(Check<?, ?> check, Transition transition, Result result, BergamotDB db)
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
            if (isStateChange && transition.hardChange)
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
    
    protected void computeStats(Check<?,?> check, CheckState state, Result result)
    {
        // stats
        state.setLastRuntime(result.getRuntime());
        state.setAverageRuntime(state.getAverageRuntime() == 0 ? state.getLastRuntime() : ((state.getLastRuntime() + state.getAverageRuntime()) / 2D));
        // check latencies
        if (result.getCheck() != null && result.getCheck() instanceof ExecuteCheck)
        {
            state.setLastCheckProcessingLatency(result.getProcessed() - ((ExecuteCheck) result.getCheck()).getScheduled());
            state.setLastCheckExecutionLatency(result.getExecuted() - ((ExecuteCheck) result.getCheck()).getScheduled());
            // moving average
            state.setAverageCheckExecutionLatency(state.getAverageCheckExecutionLatency() == 0 ? state.getLastCheckExecutionLatency() : ((state.getAverageCheckExecutionLatency() + state.getLastCheckExecutionLatency()) / 2D));
            state.setAverageCheckProcessingLatency(state.getAverageCheckProcessingLatency() == 0 ? state.getLastCheckProcessingLatency() : ((state.getAverageCheckProcessingLatency() + state.getLastCheckProcessingLatency()) / 2D));
            // log
            logger.debug("Last check latency: processing => " + state.getLastCheckProcessingLatency() + "ms, execution => " + state.getLastCheckExecutionLatency() + "ms processes");
        }
    }
 
    public static class Transition
    {
        public final CheckState previousState;
        
        public final CheckState nextState;
        
        public final boolean stateChange;
        
        public final boolean hardChange;

        public Transition(CheckState previousState, CheckState nextState, boolean stateChange, boolean hardChange)
        {
            super();
            this.previousState = previousState;
            this.nextState = nextState;
            this.stateChange = stateChange;
            this.hardChange = hardChange;
        }
    }
}

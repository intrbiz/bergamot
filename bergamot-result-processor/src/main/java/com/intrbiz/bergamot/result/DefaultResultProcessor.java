package com.intrbiz.bergamot.result;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.RealCheck;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.VirtualCheck;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.model.message.update.AlertUpdate;
import com.intrbiz.bergamot.model.message.update.CheckUpdate;
import com.intrbiz.bergamot.model.message.update.GroupUpdate;
import com.intrbiz.bergamot.model.message.update.LocationUpdate;
import com.intrbiz.bergamot.model.state.CheckSavedState;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.model.state.CheckStats;
import com.intrbiz.bergamot.model.state.CheckTransition;
import com.intrbiz.bergamot.result.matcher.Matcher;
import com.intrbiz.bergamot.result.matcher.Matchers;

public class DefaultResultProcessor extends AbstractResultProcessor
{
    private Logger logger = Logger.getLogger(DefaultResultProcessor.class);
    
    private Matchers matchers = new Matchers();

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
        this.processExecuted(new ActiveResultMO().fromCheck(check).timeout("Worker timeout whilst executing check"));
    }
    
    @Override
    public void processDeadAgent(ExecuteCheck check)
    {
        // submit a disconnected result
        this.processExecuted(new ActiveResultMO().fromCheck(check).disconnected("Bergamot Agent disconnected"));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Check<?, ?> matchCheck(BergamotDB db, ResultMO resultMO)
    {
        if (resultMO instanceof ActiveResultMO)
        {
            ActiveResultMO activeResult = (ActiveResultMO) resultMO;
            logger.debug("Matching active result for check: " + activeResult.getCheckType() + "::" + activeResult.getCheckId());
            if (Util.isEmpty(activeResult.getCheckType()))
            {
                if ("host".equalsIgnoreCase(activeResult.getCheckType()))
                {
                    return db.getHost(activeResult.getCheckId());
                }
                else if ("service".equalsIgnoreCase(activeResult.getCheckType()))
                {
                    return db.getService(activeResult.getCheckId());
                }
                else
                {
                    return db.getCheck(activeResult.getCheckId());    
                }
            }
            else
            {
                return db.getCheck(activeResult.getCheckId());
            }
        }
        else if (resultMO instanceof PassiveResultMO)
        {
            PassiveResultMO passiveResult = (PassiveResultMO) resultMO;
            // use the match engine to find the result
            Matcher<?> matcher = this.matchers.buildMatcher(passiveResult.getMatchOn());
            if (matcher == null) return null;
            logger.debug("Built matcher: " + matcher + " for passive result match on: " + passiveResult.getMatchOn());
            return ((Matcher) matcher).match(db, passiveResult.getMatchOn(), passiveResult);
        }
        return null;
    }

    @Override
    public void processExecuted(ResultMO resultMO)
    {
        // stamp in processed time
        resultMO.setProcessed(System.currentTimeMillis());
        // start the transaction
        try (BergamotDB db = BergamotDB.connect())
        {
            db.execute(() -> {
                Check<?, ?> check = this.matchCheck(db, resultMO);
                // should be process this result?
                if (! (check instanceof RealCheck))
                {
                    logger.warn("Failed to match result to a real check: " + resultMO.getId() + ", discarding!");
                    return;
                }
                if (!check.isEnabled())
                {
                    logger.warn("Discarding result " + resultMO.getId() + " for " + check.getType() + "::" + check.getId() + " because it is disabled.");
                    return;
                }
                // update the state
                // apply the result
                Transition transition = this.computeResultTransition((RealCheck<?, ?>) check, check.getState(), resultMO);
                logger.info("State change for " + check.getType() + "::" + check.getId() + " => hard state change: " + transition.hardChange + ", state change: " + transition.stateChange + ", in downtime: " + transition.nextState.isInDowntime());
                // log the transition
                db.logCheckTransition(transition.toCheckTransition(check.getSite().randomObjectId(), check.getId(), new Timestamp(resultMO.getProcessed())));
                // update the check state
                db.setCheckState(transition.nextState);
                // compute the check stats
                if (resultMO instanceof ActiveResultMO)
                {
                    CheckStats stats = this.computeStats(((RealCheck<?,?>) check).getStats(), (ActiveResultMO) resultMO);
                    db.setCheckStats(stats);
                    // saved state
                    if (resultMO instanceof ActiveResultMO)
                    {
                        ActiveResultMO arm = (ActiveResultMO) resultMO;
                        if (arm.getSavedState() != null)
                        {
                            db.setCheckSavedState(new CheckSavedState(check.getId(), arm.getSavedState()));
                        }
                    }
                }
                db.commit();
                // reschedule active checks if we have changed state at all
                if ((check instanceof ActiveCheck) && (transition.stateChange || transition.hardChange))
                {
                    // inform the scheduler to reschedule this check
                    long interval = ((ActiveCheck<?,?>) check).computeCurrentInterval(transition.nextState);
                    logger.info("Sending reschedule for " + check.getType() + "::" + check.getId() + "[" + check.getName() + "]");
                    this.rescheduleCheck((ActiveCheck<?,?>) check, interval);
                }
                // send the general state update notifications
                this.sendCheckStateUpdate(db, check, transition);
                // send group updates
                if (
                        transition.stateChange || 
                        transition.hardChange || 
                        transition.alert || 
                        transition.recovery || 
                        transition.nextState.getStatus() != transition.previousState.getStatus() || 
                        transition.nextState.isInDowntime() != transition.previousState.isInDowntime() || 
                        transition.nextState.isSuppressed() != transition.previousState.isSuppressed()
                )
                {
                    // group update
                    this.sendGroupStateUpdate(db, check, transition);
                    // location update
                    if (check instanceof Host)
                    {
                        this.sendLocationStateUpdate(db, (Host) check, transition);
                    }
                }
                // send notifications
                if (transition.alert)
                {
                    this.sendAlert(check, db);
                }
                else if (transition.recovery)
                {
                    this.sendRecovery(check, db);
                }
                // update any virtual checks
                this.updateVirtualChecks(check, transition, resultMO, db);
            });
        }
    }

    /**
     * Update listeners as to the recent state change for the given check
     */
    protected void sendCheckStateUpdate(BergamotDB db, Check<?, ?> check, Transition transition)
    {
        // send the update
        this.publishCheckUpdate(check, new CheckUpdate(check.toStubMO()));
    }
    
    /**
     * Update listeners as to the changed state of the groups the given check is a member of
     */
    protected void sendGroupStateUpdate(BergamotDB db, Check<?, ?> check, Transition transition)
    {
        // send updates for all groups this check is in
        Set<UUID> updated = new HashSet<UUID>();
        Stack<Group> groups = new Stack<Group>();
        groups.addAll(check.getGroups());
        while (! groups.empty())
        {
            Group group = groups.pop();
            if (! updated.contains(group.getId()))
            {
                updated.add(group.getId());
                // send update for group
                this.publishGroupUpdate(group, new GroupUpdate(group.toStubMO()));
                // recurse up the chain
                groups.addAll(group.getGroups());
            }
        }
    }
    
    /**
     * Update listeners as to the changed state of the locations the given host is a member of
     */
    protected void sendLocationStateUpdate(BergamotDB db, Host check, Transition transition)
    {
        // send updates for all locations this check is in
        Set<UUID> updated = new HashSet<UUID>();
        Stack<Location> locations = new Stack<Location>();
        if (check.getLocation() != null) locations.add(check.getLocation());
        while (! locations.empty())
        {
            Location location = locations.pop();
            if (! updated.contains(location.getId()))
            {
                updated.add(location.getId());
                // send update for location
                this.publishLocationUpdate(location, new LocationUpdate(location.toStubMO()));
                // recurse up the chain
                if (location.getLocation() != null) locations.add(location.getLocation());
            }
        }
    }



    protected void sendRecovery(Check<?, ?> check, BergamotDB db)
    {
        logger.warn("Recovery for " + check);
        // get the current alert for this check
        Alert alertRecord = db.getCurrentAlertForCheck(check.getId());
        if (alertRecord != null)
        {
            alertRecord.setRecovered(true);
            alertRecord.setRecoveredAt(new Timestamp(System.currentTimeMillis()));
            alertRecord.setRecoveredBy(check.getState().getLastCheckId());
            db.setAlert(alertRecord);
            // don't send notifications for suppressed checks
            if (! check.getState().isSuppressedOrInDowntime())
            {
                // send notifications?
                Calendar now = Calendar.getInstance();
                // send?
                SendRecovery recovery = alertRecord.createRecoveryNotification(now);
                if (recovery != null && (! recovery.getTo().isEmpty()))
                {
                    logger.warn("Sending recovery for " + check);
                    this.publishNotification(check, recovery);
                }
                else
                {
                    logger.warn("Not sending recovery for " + check);
                }
            }
            // publish alert update
            this.publishAlertUpdate(alertRecord, new AlertUpdate(alertRecord.toMO()));
        }
    }

    protected void sendAlert(Check<?, ?> check, BergamotDB db)
    {
        logger.warn("Alert for " + check);
        if (! check.getState().isSuppressedOrInDowntime())
        {
            // record the alert
            Alert alertRecord = new Alert(check, check.getState());
            db.setAlert(alertRecord);
            // send the notifications
            Calendar now = Calendar.getInstance();
            // send
            SendAlert alert = alertRecord.createAlertNotification(now);
            if (alert != null && (! alert.getTo().isEmpty()))
            {
                logger.warn("Sending alert for " + check);
                this.publishNotification(check, alert);
            }
            else
            {
                logger.warn("Not sending alert for " + check);
            }
            // publish alert update
            this.publishAlertUpdate(alertRecord, new AlertUpdate(alertRecord.toMO()));
        }
    }
    
    protected Transition computeResultTransition(RealCheck<?,?> check, CheckState currentState, ResultMO resultMO)
    {
        // validate that status matches ok
        Status resultStatus = Status.parse(resultMO.getStatus());
        if (resultMO.isOk() != resultStatus.isOk())
        {
            resultMO.setOk(resultStatus.isOk());
        }
        // the next state
        CheckState nextState = currentState.clone();
        // apply the result to the next state
        nextState.setOk(resultMO.isOk());
        nextState.pushOkHistory(resultMO.isOk());
        nextState.setStatus(resultStatus);
        nextState.setOutput(resultMO.getOutput());
        nextState.setLastCheckTime(new Timestamp(resultMO.getExecuted()));
        // is this check entering downtime?
        nextState.setInDowntime(check.isInDowntime());
        nextState.setSuppressed(check.isSuppressed());
        // compute the transition
        // do we have a state change
        if (currentState.isOk() ^ nextState.isOk())
        {
            // if the previous state was a hard state, update the last known hard state as we've now left that state
            if (currentState.isHard())
            {
                nextState.setLastHardOk(currentState.isOk());
                nextState.setLastHardStatus(currentState.getStatus());
                nextState.setLastHardOutput(currentState.getOutput());
            }
            // we've changed state
            nextState.setLastStateChange(new Timestamp(System.currentTimeMillis()));
            // begin or immediately transition
            if (check.computeCurrentAttemptThreshold(nextState) > 1)
            {
                // start the transition
                nextState.setHard(false);
                nextState.setTransitioning(true);
                nextState.setAttempt(1);               
                return new Transition(currentState, nextState, true, false, false, false);
            }
            else
            {
                // immediately enter hard state
                nextState.setHard(true);
                nextState.setTransitioning(false);
                nextState.setAttempt(check.computeCurrentAttemptThreshold(nextState));
                return new Transition(currentState, nextState, true, true, (nextState.isOk() ^ nextState.isLastHardOk()) && (! nextState.isOk()), (nextState.isOk() ^ nextState.isLastHardOk()) && nextState.isOk());
            }
        }
        else if (currentState.isHard())
        {
            // steady state
            return new Transition(currentState, nextState, false, false, false, false);
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
                return new Transition(currentState, nextState, false, true, (nextState.isOk() ^ nextState.isLastHardOk()) && (! nextState.isOk()), (nextState.isOk() ^ nextState.isLastHardOk()) && nextState.isOk());
            }
            else
            {
                return new Transition(currentState, nextState, false, false, false, false);
            }
        }
    }

    // virtual check handling
    protected void updateVirtualChecks(Check<?, ?> check, Transition transition, ResultMO resultMO, BergamotDB db)
    {
        this.updateVirtualChecks(check, transition, resultMO, db, new HashSet<UUID>());
    }
    
    protected void updateVirtualChecks(Check<?, ?> check, Transition transition, ResultMO resultMO, BergamotDB db, Set<UUID> processedVirtualChecks)
    {
        for (VirtualCheck<?, ?> referencedBy : check.getReferencedBy())
        {
            // cycle handling
            if (processedVirtualChecks.contains(referencedBy.getId()))
            {
                // we've already processed this virtual check, skip it
                continue;
            }
            processedVirtualChecks.add(referencedBy.getId());
            // update the status of the check
            boolean ok = referencedBy.getCondition().computeOk();
            Status status = referencedBy.getCondition().computeStatus();
            boolean allHard = referencedBy.getCondition().isAllDependenciesHard();
            // update the check
            Transition virtualTransition = this.applyVirtualResult(referencedBy, referencedBy.getState(), ok, status, allHard, resultMO);
            logger.info("Virtual state change for " + referencedBy.getType() + "::" + referencedBy.getId() + " => hard state change: " + transition.hardChange + ", state change: " + transition.stateChange + " triggered by " + check.getType() + "::" + check.getId());
            // update the state
            db.logCheckTransition(virtualTransition.toCheckTransition(referencedBy.getSite().randomObjectId(), referencedBy.getId(), new Timestamp(resultMO.getProcessed())));
            db.setCheckState(virtualTransition.nextState);
            db.commit();
            // send the general state update notifications
            this.sendCheckStateUpdate(db, referencedBy, virtualTransition);
            // send group state updates
            if (
                    virtualTransition.stateChange || 
                    virtualTransition.hardChange || 
                    virtualTransition.alert || 
                    virtualTransition.recovery || 
                    virtualTransition.nextState.getStatus() != virtualTransition.previousState.getStatus() ||
                    virtualTransition.nextState.isInDowntime() != virtualTransition.previousState.isInDowntime() || 
                    virtualTransition.nextState.isSuppressed() != virtualTransition.previousState.isSuppressed()
            )
            {
                this.sendGroupStateUpdate(db, referencedBy, virtualTransition);
            }
            // send notifications
            if (virtualTransition.alert)
            {
                this.sendAlert(referencedBy, db);
            }
            if (virtualTransition.recovery)
            {
                this.sendRecovery(referencedBy, db);
            }
            // recurse
            this.updateVirtualChecks(referencedBy, virtualTransition, resultMO, db, processedVirtualChecks);
        }
    }

    protected Transition applyVirtualResult(VirtualCheck<?, ?> check, CheckState currentState, boolean ok, Status status, boolean allHard, ResultMO cause)
    {
     // the next state
        CheckState nextState = currentState.clone();
        // apply the result to the next state
        nextState.setOk(ok);
        nextState.pushOkHistory(ok);
        nextState.setStatus(status);
        nextState.setOutput(null);
        nextState.setLastCheckTime(new Timestamp(cause.getExecuted()));
        // is this check entering downtime?
        nextState.setInDowntime(check.isInDowntime());
        nextState.setSuppressed(check.isSuppressed());
        // compute the transition
        // do we have a state change
        if (currentState.isOk() ^ nextState.isOk())
        {
            // if the previous state was a hard state, update the last known hard state as we've now left that state
            if (currentState.isHard())
            {
                nextState.setLastHardOk(currentState.isOk());
                nextState.setLastHardStatus(currentState.getStatus());
                nextState.setLastHardOutput(currentState.getOutput());
            }
            // we've changed state
            nextState.setLastStateChange(new Timestamp(System.currentTimeMillis()));
            // begin or immediately transition
            if (! allHard)
            {
                // start the transition, until all dependent checks reach a hard state
                nextState.setHard(false);
                nextState.setTransitioning(true);
                nextState.setAttempt(0);               
                return new Transition(currentState, nextState, true, false, false, false);
            }
            else
            {
                // immediately enter hard state as all dependent checks are in a hard state
                nextState.setHard(true);
                nextState.setTransitioning(false);
                nextState.setAttempt(0);
                return new Transition(currentState, nextState, true, true, (nextState.isOk() ^ nextState.isLastHardOk()) && (! nextState.isOk()), (nextState.isOk() ^ nextState.isLastHardOk()) && nextState.isOk());
            }
        }
        else if (currentState.isHard())
        {
            // steady state
            return new Transition(currentState, nextState, false, false, false, false);
        }
        else
        {
            // have we reached a hard state
            if (allHard)
            {
                nextState.setHard(true);
                nextState.setTransitioning(false);
                nextState.setAttempt(0);
                return new Transition(currentState, nextState, false, true, (nextState.isOk() ^ nextState.isLastHardOk()) && (! nextState.isOk()), (nextState.isOk() ^ nextState.isLastHardOk()) && nextState.isOk());
            }
            else
            {
                return new Transition(currentState, nextState, false, false, false, false);
            }
        }
    }
    
    protected CheckStats computeStats(CheckStats stats, ActiveResultMO resultMO)
    {
        CheckStats nextStats = stats.clone();
        // compute the stats
        nextStats.setLastRuntime(resultMO.getRuntime());
        nextStats.setAverageRuntime(stats.getAverageRuntime() == 0 ? stats.getLastRuntime() : ((stats.getLastRuntime() + stats.getAverageRuntime()) / 2D));
        // check latencies
        if (resultMO.getCheck() != null && resultMO.getCheck() instanceof ExecuteCheck)
        {
            nextStats.setLastCheckProcessingLatency(resultMO.getProcessed() - ((ExecuteCheck) resultMO.getCheck()).getScheduled());
            nextStats.setLastCheckExecutionLatency( resultMO.getExecuted()  - ((ExecuteCheck) resultMO.getCheck()).getScheduled());
            // moving average
            nextStats.setAverageCheckExecutionLatency( stats.getAverageCheckExecutionLatency()  == 0 ? stats.getLastCheckExecutionLatency()  : ((stats.getAverageCheckExecutionLatency()  + stats.getLastCheckExecutionLatency())  / 2D));
            nextStats.setAverageCheckProcessingLatency(stats.getAverageCheckProcessingLatency() == 0 ? stats.getLastCheckProcessingLatency() : ((stats.getAverageCheckProcessingLatency() + stats.getLastCheckProcessingLatency()) / 2D));
            // log
            logger.debug("Last check latency: processing => " + nextStats.getLastCheckProcessingLatency() + "ms, execution => " + nextStats.getLastCheckExecutionLatency() + "ms processes");
        }
        return nextStats;
    }
 
    public static class Transition
    {
        public final CheckState previousState;
        
        public final CheckState nextState;
        
        public final boolean stateChange;
        
        public final boolean hardChange;
        
        public final boolean alert;
        
        public final boolean recovery;

        public Transition(CheckState previousState, CheckState nextState, boolean stateChange, boolean hardChange, boolean alert, boolean recovery)
        {
            super();
            this.previousState = previousState;
            this.nextState = nextState;
            this.stateChange = stateChange;
            this.hardChange = hardChange;
            this.alert = alert;
            this.recovery = recovery;
        }
        
        public CheckTransition toCheckTransition(UUID id, UUID checkId, Timestamp appliedAt)
        {
            return new CheckTransition(id, checkId, appliedAt, this.stateChange, this.hardChange, this.alert, this.recovery, this.previousState, this.nextState);
        }
    }
}

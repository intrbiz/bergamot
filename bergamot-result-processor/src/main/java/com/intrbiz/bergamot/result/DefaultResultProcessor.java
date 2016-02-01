package com.intrbiz.bergamot.result;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.accounting.Accounting;
import com.intrbiz.bergamot.accounting.model.AccountingNotificationType;
import com.intrbiz.bergamot.accounting.model.ProcessResultAccountingEvent;
import com.intrbiz.bergamot.accounting.model.ProcessResultAccountingEvent.ResultType;
import com.intrbiz.bergamot.accounting.model.SendNotificationAccountingEvent;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.AlertEncompasses;
import com.intrbiz.bergamot.model.AlertEscalation;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Escalation;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.NotificationType;
import com.intrbiz.bergamot.model.RealCheck;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.VirtualCheck;
import com.intrbiz.bergamot.model.message.ContactMO;
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
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class DefaultResultProcessor extends AbstractResultProcessor
{
    private Logger logger = Logger.getLogger(DefaultResultProcessor.class);
    
    private Matchers matchers = new Matchers();
    
    private Accounting accounting = Accounting.create(DefaultResultProcessor.class);

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
        // is this an adhoc result
        if (resultMO.getAdhocId() != null)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Got adhoc result, result processing will be skipped");
                logger.trace(resultMO);
            }
            // rather than processing this result we should 
            // dispatch it to the adhoc originator
            this.publishAdhocResult(resultMO);
            // skip any processing
            return;
        }
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
                // account this processing
                // account
                this.accounting.account(new ProcessResultAccountingEvent(check.getSiteId(), resultMO.getId(), check.getId(), resultMO instanceof ActiveResultMO ? ResultType.ACTIVE : ResultType.PASSIVE));
                // only process for enabled checks
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
                // make our state available as soon as possible
                db.commit();
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
                // reschedule active checks if we have changed state at all
                if ((check instanceof ActiveCheck) && transition.hasSchedulingChanged())
                {
                    // inform the scheduler to reschedule this check
                    long interval = ((ActiveCheck<?,?>) check).computeCurrentInterval(transition.nextState);
                    logger.info("Sending reschedule for " + check.getType() + "::" + check.getId() + "[" + check.getName() + "]");
                    this.rescheduleCheck((ActiveCheck<?,?>) check, interval);
                }
                // send the general state update notifications
                this.sendCheckStateUpdate(db, check, transition);
                // send group updates
                if (transition.hasChanged())
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
                else if (transition.hasGotWorse())
                {
                    // we should send another alert notification as the situation has gotten worse
                    this.resendAlert(check, db);
                }
                else if (transition.previousState.isHardNotOk() && transition.nextState.isHardNotOk())
                {
                    // we are in a hard not ok state, we should check the escalation policies
                    this.processEscalation(check, db);
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
        this.publishCheckUpdate(check, new CheckUpdate(check.toStubMOUnsafe()));
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
                this.publishGroupUpdate(group, new GroupUpdate(group.toStubMOUnsafe()));
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
                this.publishLocationUpdate(location, new LocationUpdate(location.toStubMOUnsafe()));
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
                    // accounting
                    this.accounting.account(new SendNotificationAccountingEvent(check.getSiteId(), alertRecord.getId(), check.getId(), AccountingNotificationType.RECOVERY, recovery.getTo().size(), 0, null));
                }
                else
                {
                    logger.warn("Not sending recovery for " + check);
                }
            }
            // publish alert update
            this.publishAlertUpdate(alertRecord, new AlertUpdate(alertRecord.toMOUnsafe()));
        }
    }

    protected void sendAlert(Check<?, ?> check, BergamotDB db)
    {
        logger.warn("Alert for " + check);
        CheckState state = check.getState();
        if (! (state.isSuppressedOrInDowntime() || state.isEncompassed()))
        {
            Calendar now = Calendar.getInstance();
            // compute the contacts who should be notified
            List<ContactMO> to = check.getContactsToNotify(NotificationType.ALERT, state.getStatus(), now);
            // record the alert
            Alert alertRecord = new Alert(check, state, to);
            db.setAlert(alertRecord);
            // send the notifications
            SendAlert alert = alertRecord.createAlertNotification(now, to);
            if (alert != null && (! alert.getTo().isEmpty()))
            {
                logger.warn("Sending alert for " + check);
                this.publishNotification(check, alert);
                // accounting
                this.accounting.account(new SendNotificationAccountingEvent(check.getSiteId(), alertRecord.getId(), check.getId(), AccountingNotificationType.ALERT, alert.getTo().size(), 0, null));
            }
            else
            {
                logger.warn("Not sending alert for " + check);
            }
            // publish alert update
            this.publishAlertUpdate(alertRecord, new AlertUpdate(alertRecord.toMOUnsafe()));
        }
        else if (state.isEncompassed())
        {
            Alert encompassingAlert = state.getCurrentAlert();
            // tag this check into the alert for the dependency
            if (encompassingAlert != null && (! check.getId().equals(encompassingAlert.getCheckId())))
            {
                db.setAlertEncompasses(new AlertEncompasses(encompassingAlert.getId(), check.getId(), new Timestamp(System.currentTimeMillis())));
            }
            else
            {
                logger.warn("Failed to find alert which encompasses alert for check " + check.getType() + "::" + check.getId());
            }
        }
    }
    
    protected void resendAlert(Check<?, ?> check, BergamotDB db)
    {
        // get the alert information
        Alert alertRecord = db.getCurrentAlertForCheck(check.getId());
        if (alertRecord != null && (! alertRecord.isAcknowledged()) && (! alertRecord.isRecovered()) && check.getId().equals(alertRecord.getCheckId()))
        {
            logger.warn("Resending notifications for alert " + alertRecord.getId());
            CheckState state = check.getState();
            if (! state.isSuppressedOrInDowntime())
            {
                Calendar now = Calendar.getInstance();
                // compute the contacts who should be notified
                List<ContactMO> to = alertRecord.getContactsToNotify(now);
                // send the notifications
                SendAlert alert = alertRecord.createAlertNotification(now, to);
                if (alert != null && (! alert.getTo().isEmpty()))
                {
                    logger.warn("Sending alert for " + check);
                    this.publishNotification(check, alert);
                    // accounting
                    this.accounting.account(new SendNotificationAccountingEvent(check.getSiteId(), alertRecord.getId(), check.getId(), AccountingNotificationType.ALERT, alert.getTo().size(), 0, null));
                }
                // publish alert update
                this.publishAlertUpdate(alertRecord, new AlertUpdate(alertRecord.toMOUnsafe()));
            }
        }
    }
    
    protected void processEscalation(Check<?,?> check, BergamotDB db)
    {
        // get the alert information
        Alert alert = db.getCurrentAlertForCheck(check.getId());
        if (alert != null && (! alert.isAcknowledged()) && (! alert.isRecovered()))
        {
            long alertDuration = System.currentTimeMillis() - alert.getRaised().getTime();
            logger.debug("Processing escalations for " + check.getType() + " " + check.getId() + " alert duration: " + alertDuration);
            // current check state
            CheckState state = check.getState();
            Calendar now = Calendar.getInstance();
            // evaluate the escalation policies for this check
            List<Escalation> escalations = new LinkedList<Escalation>();
            check.getNotifications().evalEscalations(alertDuration, state.getStatus(), now, escalations);
            // evaluate the escalation policies for the contacts which were notified
            for (Contact notified : alert.getNotified())
            {
                notified.getNotifications().evalEscalations(alertDuration, state.getStatus(), now, escalations);
            }
            if (! escalations.isEmpty())
            {
                // compute the minimum escalation threshold we found
                long after = escalations.stream().map((e) -> e.getAfter()).filter((a) -> a > alert.getEscalationThreshold()).min(Long::compare).orElse(-1L);
                if (after > 0 && after > alert.getEscalationThreshold())
                {
                    // produce the list of contacts to whom the escalated alert should be sent
                    Set<ContactMO> escalateTo = new HashSet<ContactMO>();
                    for (Escalation escalation : escalations)
                    {
                        if (escalation.getAfter() == after)
                        {
                            // should we renotify the original contacts of the alert
                            if (escalation.isRenotify())
                            {
                                /* 
                                 * TODO: Should we apply the notification settings again, or is this 
                                 *       an explicit override.  For example if a contact was originally 
                                 *       notified, but now is not to be notified, should we renotify?
                                 *       
                                 *       Currently we forcefully renotify all original contacts
                                 */
                                alert.getNotified().stream().map(Contact::toMOUnsafe).forEach(escalateTo::add);
                            }
                            // compute any new contacts to notify
                            escalateTo.addAll(escalation.getContactsToNotify(check, state.getStatus(), now));
                        }
                    }
                    // do we have anyone to escalate too
                    if (! escalateTo.isEmpty())
                    {
                        logger.info("Raising escalation for alert " + alert.getId() + " after " + after + " to [" + escalateTo.stream().map(ContactMO::getName).collect(Collectors.joining(", ")) + "]");
                        // record the escalation
                        if (! alert.isEscalated())
                        {
                            alert.setEscalated(true);
                            alert.setEscalatedAt(new Timestamp(System.currentTimeMillis()));
                            alert.setEscalationThreshold(after);
                            db.setAlert(alert);
                        }
                        else
                        {
                            // update the escalation threshold
                            db.setAlertEscalationThreshold(alert.getId(), after);
                            alert.setEscalationThreshold(after);
                        }
                        AlertEscalation alertEscalation = new AlertEscalation();
                        alertEscalation.setEscalationId(UUID.randomUUID());
                        alertEscalation.setAlertId(alert.getId());
                        alertEscalation.setAfter(after);
                        alertEscalation.setEscalatedAt(new Timestamp(System.currentTimeMillis()));
                        alertEscalation.setNotifiedIds(escalateTo.stream().map((c) -> c.getId()).collect(Collectors.toList()));
                        db.setAlertEscalation(alertEscalation);
                        // send the notification
                        SendAlert sendAlert = alert.createEscalatedAlertNotification(now, new LinkedList<ContactMO>(escalateTo));
                        if (sendAlert != null && (! sendAlert.getTo().isEmpty()))
                        {
                            logger.warn("Sending escalated alert for " + check);
                            this.publishNotification(check, sendAlert);
                            // accounting
                            this.accounting.account(new SendNotificationAccountingEvent(check.getSiteId(), sendAlert.getId(), check.getId(), AccountingNotificationType.ALERT, sendAlert.getTo().size(), alertEscalation.getAfter(), alertEscalation.getEscalationId()));
                        }
                        // publish alert update
                        this.publishAlertUpdate(alert, new AlertUpdate(alert.toMOUnsafe()));
                    }
                }
            }
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
        // collect the checks that we are dependent upon
        // we cannot reach a hard state until all dependencies 
        // checks are in a hard state
        boolean hasDependencies = check.hasDependencies();
        boolean dependenciesAreAllHard = true;
        boolean dependenciesAreAllOk = true;
        UUID encompassingAlertId = null;
        if (hasDependencies)
        {
            for (Check<?,?> dependency : check.getDepends())
            {
                CheckState dependencyState = dependency.getState();
                dependenciesAreAllHard &= dependencyState.isHard();
                dependenciesAreAllOk   &= dependencyState.isHardOk();
                // get the alert id which encompasses us
                if (dependencyState.isHardNotOk() && encompassingAlertId == null)
                    encompassingAlertId = dependencyState.getCurrentAlertId();
            }
            logger.debug("Dependencies for check " + check.getId() + " are all hard: " + dependenciesAreAllHard + " and all ok: " + dependenciesAreAllOk);
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
        // has the current alert been acknowledged
        if (currentState.getCurrentAlertId() != null)
        {
            Alert currentAlert = currentState.getCurrentAlert();
            currentState.setAcknowledged(currentAlert == null ? false : currentAlert.isAcknowledged());
        }
        // is the status of this alert encompassed by any other check
        nextState.setEncompassed(dependenciesAreAllHard && (! dependenciesAreAllOk));
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
            // immediately go to a hard state or start the transition
            if (check.computeCurrentAttemptThreshold(nextState) <= 1 && ((! hasDependencies) || dependenciesAreAllHard))
            {
                // we can only enter a hard state if we have no dependencies
                // or all the dependencies are in a hard state
                // immediately enter hard state
                nextState.setHard(true);
                nextState.setTransitioning(false);
                nextState.setAttempt(check.computeCurrentAttemptThreshold(nextState));
                if (nextState.isAlert())
                {
                    nextState.setCurrentAlertId(encompassingAlertId == null ? Site.randomId(check.getSiteId()) : encompassingAlertId);
                    nextState.setAcknowledged(false);
                }
                if (nextState.isRecovery())
                {
                    nextState.setCurrentAlertId(null);
                    nextState.setAcknowledged(false);
                }
                return new Transition()
                    .previousState(currentState)
                    .nextState(nextState)
                    .stateChange(true)
                    .hardChange(true)
                    .alert(nextState.isAlert())
                    .recovery(nextState.isRecovery());
            }
            else
            {
                // start the transition
                nextState.setHard(false);
                nextState.setTransitioning(true);
                nextState.setAttempt(1);               
                return new Transition()
                    .previousState(currentState)
                    .nextState(nextState)
                    .stateChange(true)
                    .hardChange(false)
                    .alert(false)
                    .recovery(false);
            }
        }
        else if (currentState.isHard())
        {
            // steady state
            return new Transition()
                .previousState(currentState)
                .nextState(nextState)
                .stateChange(false)
                .hardChange(false)
                .alert(false)
                .recovery(false);
        }
        else
        {
            // during transition
            nextState.setAttempt(currentState.getAttempt() + 1);
            // have we reached a hard state
            if (nextState.getAttempt() >= check.computeCurrentAttemptThreshold(nextState) && ((! hasDependencies) || dependenciesAreAllHard))
            {
                // we can only enter a hard state if we have no dependencies
                // or all the dependencies are in a hard state
                nextState.setHard(true);
                nextState.setTransitioning(false);
                nextState.setAttempt(check.computeCurrentAttemptThreshold(nextState));
                if (nextState.isAlert())
                {
                    nextState.setCurrentAlertId(encompassingAlertId == null ? Site.randomId(check.getSiteId()) : encompassingAlertId);
                    nextState.setAcknowledged(false);
                }
                if (nextState.isRecovery())
                {
                    nextState.setCurrentAlertId(null);
                    nextState.setAcknowledged(false);
                }
                return new Transition()
                    .previousState(currentState)
                    .nextState(nextState)
                    .stateChange(false)
                    .hardChange(true)
                    .alert(nextState.isAlert())
                    .recovery(nextState.isRecovery());
            }
            else
            {
                return new Transition()
                    .previousState(currentState)
                    .nextState(nextState)
                    .stateChange(false)
                    .hardChange(false)
                    .alert(false)
                    .recovery(false);
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
            VirtualCheckExpressionContext context = db.createVirtualCheckContext(check.getSiteId(), null);
            boolean ok = referencedBy.getCondition().computeOk(context);
            Status status = referencedBy.getCondition().computeStatus(context);
            boolean allHard = referencedBy.getCondition().isAllDependenciesHard(context);
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
        // has the current alert been acknowledged
        if (currentState.getCurrentAlertId() != null)
        {
            Alert currentAlert = currentState.getCurrentAlert();
            currentState.setAcknowledged(currentAlert.isAcknowledged());
        }
        // a virtual check can never be encompassed
        nextState.setEncompassed(false);
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
                return new Transition()
                    .previousState(currentState)
                    .nextState(nextState)
                    .stateChange(true)
                    .hardChange(false)
                    .alert(false)
                    .recovery(false);
            }
            else
            {
                // immediately enter hard state as all dependent checks are in a hard state
                nextState.setHard(true);
                nextState.setTransitioning(false);
                nextState.setAttempt(0);
                if (nextState.isAlert())
                {
                    nextState.setCurrentAlertId(Site.randomId(check.getSiteId()));
                    nextState.setAcknowledged(false);
                }
                if (nextState.isRecovery())
                {
                    nextState.setCurrentAlertId(null);
                    nextState.setAcknowledged(false);
                }
                return new Transition()
                    .previousState(currentState)
                    .nextState(nextState)
                    .stateChange(true)
                    .hardChange(true)
                    .alert(nextState.isAlert())
                    .recovery(nextState.isRecovery());
            }
        }
        else if (currentState.isHard())
        {
            // steady state
            return new Transition()
                .previousState(currentState)
                .nextState(nextState)
                .stateChange(false)
                .hardChange(false)
                .alert(false)
                .recovery(false);
        }
        else
        {
            // have we reached a hard state
            if (allHard)
            {
                nextState.setHard(true);
                nextState.setTransitioning(false);
                nextState.setAttempt(0);
                if (nextState.isAlert())
                {
                    nextState.setCurrentAlertId(Site.randomId(check.getSiteId()));
                    nextState.setAcknowledged(false);
                }
                if (nextState.isRecovery())
                {
                    nextState.setCurrentAlertId(null);
                    nextState.setAcknowledged(false);
                }
                return new Transition()
                    .previousState(currentState)
                    .nextState(nextState)
                    .stateChange(false)
                    .hardChange(true)
                    .alert(nextState.isAlert())
                    .recovery(nextState.isRecovery());
            }
            else
            {
                return new Transition()
                    .previousState(currentState)
                    .nextState(nextState)
                    .stateChange(false)
                    .hardChange(false)
                    .alert(false)
                    .recovery(false);
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
        public CheckState previousState;
        
        public CheckState nextState;
        
        public boolean stateChange;
        
        public boolean hardChange;
        
        public boolean alert;
        
        public boolean recovery;

        public Transition()
        {
            super();
        }
        
        public Transition previousState(CheckState previousState)
        {
            this.previousState = previousState;
            return this;
        }
        
        public Transition nextState(CheckState nextState)
        {
            this.nextState = nextState;
            return this;
        }
        
        public Transition stateChange(boolean stateChange)
        {
            this.stateChange = stateChange;
            return this;
        }
        
        public Transition hardChange(boolean hardChange)
        {
            this.hardChange = hardChange;
            return this;
        }
        
        public Transition alert(boolean alert)
        {
            this.alert = alert;
            return this;
        }
        
        public Transition recovery(boolean recovery)
        {
            this.recovery = recovery;
            return this;
        }
        
        public CheckTransition toCheckTransition(UUID id, UUID checkId, Timestamp appliedAt)
        {
            return new CheckTransition(id, checkId, appliedAt, this.stateChange, this.hardChange, this.alert, this.recovery, this.previousState, this.nextState);
        }
        
        public boolean hasChanged()
        {
            return this.stateChange || 
            this.hardChange || 
            this.alert || 
            this.recovery || 
            this.nextState.getStatus() != this.previousState.getStatus() || 
            this.nextState.isInDowntime() != this.previousState.isInDowntime() || 
            this.nextState.isSuppressed() != this.previousState.isSuppressed() || 
            this.nextState.isAcknowledged() != this.previousState.isAcknowledged() || 
            this.nextState.isEncompassed() != this.previousState.isEncompassed();
        }
        
        public boolean hasSchedulingChanged()
        {
            return this.stateChange || this.hardChange;
        }
        
        public boolean hasGotWorse()
        {
            return this.previousState.isHardNotOk() && this.nextState.isHardNotOk() && this.nextState.getStatus().isWorseThan(this.previousState.getStatus());
        }
    }
}

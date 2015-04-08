package com.intrbiz.bergamot.result;
import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.UUID;

import org.junit.Test;

import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.RealCheck;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.result.DefaultResultProcessor;


public class DefaultResultProcessorTests extends DefaultResultProcessor
{
    private CheckState newState(Status status, boolean hard, int attempt, boolean transitioning, Status lastStatus, boolean lastHard)
    {
        CheckState state = new CheckState();
        state.setCheckId(UUID.randomUUID());
        state.setStatus(status);
        state.setOk(status.isOk());
        state.setOutput(status.toString().toLowerCase());
        state.setHard(hard);
        state.setAttempt(attempt);
        state.setTransitioning(transitioning);
        state.setLastHardOk(lastStatus.isOk());
        state.setLastHardStatus(lastStatus);
        state.setLastHardOutput(lastStatus.toString().toLowerCase());
        state.setLastHardOk(lastHard);
        state.setLastStateChange(new Timestamp(System.currentTimeMillis()));
        return state;
    }
    
    private ResultMO newResult(boolean ok, String status, String output)
    {
        ResultMO resultMO = new ActiveResultMO();
        resultMO.setOk(ok);
        resultMO.setStatus(status);
        resultMO.setOutput(output);
        return resultMO;
    }
    
    private RealCheck<?, ?> newCheck(int alertThreshold, int recoveryThreshold)
    {
        Host h = new Host();
        h.setAlertAttemptThreshold(alertThreshold);
        h.setRecoveryAttemptThreshold(recoveryThreshold);
        return h;
    }
    
    @Test
    public void testSimpleBadTransition()
    {
        RealCheck<?,?> check = newCheck(2, 2);
        CheckState state = newState(Status.OK, true, 2, false, Status.PENDING, true);
        // the transition
        Transition transition;
        // apply a bad result
        transition = this.computeResultTransition(check, state, newResult(false, "CRITICAL", "critical"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is not a hard change", transition.hardChange == false);
        assertTrue("Is not an alert", transition.alert == false);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is not ok", transition.nextState.isOk() == false);
        assertTrue("Next state is critical", transition.nextState.getStatus() == Status.CRITICAL);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is not hard", transition.nextState.isHard() == false);
        assertTrue("Next state is transitioning", transition.nextState.isTransitioning() == true);
        assertTrue("Next state last hard ok copied", transition.previousState.isOk() == transition.nextState.isLastHardOk());
        assertTrue("Next state last hard status copied", transition.previousState.getStatus() == transition.nextState.getLastHardStatus());
        state = transition.nextState;
        // apply a second bad result
        transition = this.computeResultTransition(check, state, newResult(false, "CRITICAL", "critical"));
        assertTrue("Is not a state change", transition.stateChange == false);
        assertTrue("Is a hard change", transition.hardChange == true);
        assertTrue("Is an alert", transition.alert == true);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is not ok", transition.nextState.isOk() == false);
        assertTrue("Next state is critical", transition.nextState.getStatus() == Status.CRITICAL);
        assertTrue("Next state attempt is 2", transition.nextState.getAttempt() == 2);
        assertTrue("Next state is hard", transition.nextState.isHard() == true);
        assertTrue("Next state is no transitioning", transition.nextState.isTransitioning() == false);
    }
    
    @Test
    public void testSimpleGoodTransition()
    {
        RealCheck<?,?> check = newCheck(2, 2);
        CheckState state = newState(Status.CRITICAL, true, 2, false, Status.OK, true);
        // the transition
        Transition transition;
        // apply a good result
        transition = this.computeResultTransition(check, state, newResult(true, "OK", "ok"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is not a hard change", transition.hardChange == false);
        assertTrue("Is not an alert", transition.alert == false);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is ok", transition.nextState.isOk() == true);
        assertTrue("Next state is ok", transition.nextState.getStatus() == Status.OK);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is not hard", transition.nextState.isHard() == false);
        assertTrue("Next state is transitioning", transition.nextState.isTransitioning() == true);
        assertTrue("Next state last hard ok copied", transition.previousState.isOk() == transition.nextState.isLastHardOk());
        assertTrue("Next state last hard status copied", transition.previousState.getStatus() == transition.nextState.getLastHardStatus());
        state = transition.nextState;
        // apply a second good result
        transition = this.computeResultTransition(check, state, newResult(false, "OK", "ok"));
        assertTrue("Is not a state change", transition.stateChange == false);
        assertTrue("Is a hard change", transition.hardChange == true);
        assertTrue("Is an alert", transition.alert == false);
        assertTrue("Is a recovery", transition.recovery == true);
        assertTrue("Next state is ok", transition.nextState.isOk() == true);
        assertTrue("Next state is ok", transition.nextState.getStatus() == Status.OK);
        assertTrue("Next state attempt is 2", transition.nextState.getAttempt() == 2);
        assertTrue("Next state is hard", transition.nextState.isHard() == true);
        assertTrue("Next state is no transitioning", transition.nextState.isTransitioning() == false);
    }
    
    @Test
    public void testFlappingBadTransition()
    {
        RealCheck<?,?> check = newCheck(2, 2);
        CheckState state = newState(Status.OK, true, 2, false, Status.PENDING, true);
        // the transition
        Transition transition;
        // apply a bad result
        transition = this.computeResultTransition(check, state, newResult(false, "CRITICAL", "critical"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is not a hard change", transition.hardChange == false);
        assertTrue("Is not an alert", transition.alert == false);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is not ok", transition.nextState.isOk() == false);
        assertTrue("Next state is critical", transition.nextState.getStatus() == Status.CRITICAL);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is not hard", transition.nextState.isHard() == false);
        assertTrue("Next state is transitioning", transition.nextState.isTransitioning() == true);
        assertTrue("Next state last hard ok copied", transition.previousState.isOk() == transition.nextState.isLastHardOk());
        assertTrue("Next state last hard status copied", transition.previousState.getStatus() == transition.nextState.getLastHardStatus());
        state = transition.nextState;
        // apply a good result
        transition = this.computeResultTransition(check, state, newResult(true, "OK", "ok"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is not a hard change", transition.hardChange == false);
        assertTrue("Is not an alert", transition.alert == false);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is ok", transition.nextState.isOk() == true);
        assertTrue("Next state is ok", transition.nextState.getStatus() == Status.OK);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is not hard", transition.nextState.isHard() == false);
        assertTrue("Next state is transitioning", transition.nextState.isTransitioning() == true);
        state = transition.nextState;
        // apply a bad result
        transition = this.computeResultTransition(check, state, newResult(false, "CRITICAL", "critical"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is not a hard change", transition.hardChange == false);
        assertTrue("Is not an alert", transition.alert == false);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is not ok", transition.nextState.isOk() == false);
        assertTrue("Next state is critical", transition.nextState.getStatus() == Status.CRITICAL);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is not hard", transition.nextState.isHard() == false);
        assertTrue("Next state is transitioning", transition.nextState.isTransitioning() == true);
        state = transition.nextState;
        // apply a second bad result
        transition = this.computeResultTransition(check, state, newResult(false, "CRITICAL", "critical"));
        assertTrue("Is not a state change", transition.stateChange == false);
        assertTrue("Is a hard change", transition.hardChange == true);
        assertTrue("Is an alert", transition.alert == true);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is not ok", transition.nextState.isOk() == false);
        assertTrue("Next state is critical", transition.nextState.getStatus() == Status.CRITICAL);
        assertTrue("Next state attempt is 2", transition.nextState.getAttempt() == 2);
        assertTrue("Next state is hard", transition.nextState.isHard() == true);
        assertTrue("Next state is no transitioning", transition.nextState.isTransitioning() == false);
    }

    @Test
    public void testFlappingGoodTransition()
    {
        RealCheck<?,?> check = newCheck(2, 2);
        CheckState state = newState(Status.CRITICAL, true, 2, false, Status.OK, true);
        // the transition
        Transition transition;
        // apply a good result
        transition = this.computeResultTransition(check, state, newResult(true, "OK", "ok"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is not a hard change", transition.hardChange == false);
        assertTrue("Is not an alert", transition.alert == false);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is ok", transition.nextState.isOk() == true);
        assertTrue("Next state is ok", transition.nextState.getStatus() == Status.OK);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is not hard", transition.nextState.isHard() == false);
        assertTrue("Next state is transitioning", transition.nextState.isTransitioning() == true);
        assertTrue("Next state last hard ok copied", transition.previousState.isOk() == transition.nextState.isLastHardOk());
        assertTrue("Next state last hard status copied", transition.previousState.getStatus() == transition.nextState.getLastHardStatus());
        state = transition.nextState;
        // apply a bad result
        transition = this.computeResultTransition(check, state, newResult(false, "CRITICAL", "critical"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is not a hard change", transition.hardChange == false);
        assertTrue("Is not an alert", transition.alert == false);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is not ok", transition.nextState.isOk() == false);
        assertTrue("Next state is critical", transition.nextState.getStatus() == Status.CRITICAL);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is not hard", transition.nextState.isHard() == false);
        assertTrue("Next state is transitioning", transition.nextState.isTransitioning() == true);
        state = transition.nextState;
        // apply a good result
        transition = this.computeResultTransition(check, state, newResult(true, "OK", "ok"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is not a hard change", transition.hardChange == false);
        assertTrue("Is not an alert", transition.alert == false);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is ok", transition.nextState.isOk() == true);
        assertTrue("Next state is ok", transition.nextState.getStatus() == Status.OK);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is not hard", transition.nextState.isHard() == false);
        assertTrue("Next state is transitioning", transition.nextState.isTransitioning() == true);
        state = transition.nextState;
        // apply a second good result
        transition = this.computeResultTransition(check, state, newResult(false, "OK", "ok"));
        assertTrue("Is not a state change", transition.stateChange == false);
        assertTrue("Is a hard change", transition.hardChange == true);
        assertTrue("Is an alert", transition.alert == false);
        assertTrue("Is a recovery", transition.recovery == true);
        assertTrue("Next state is ok", transition.nextState.isOk() == true);
        assertTrue("Next state is ok", transition.nextState.getStatus() == Status.OK);
        assertTrue("Next state attempt is 2", transition.nextState.getAttempt() == 2);
        assertTrue("Next state is hard", transition.nextState.isHard() == true);
        assertTrue("Next state is no transitioning", transition.nextState.isTransitioning() == false);
    }
    
    @Test
    public void testStagedBadTransition()
    {
        RealCheck<?,?> check = newCheck(3, 3);
        CheckState state = newState(Status.OK, true, 2, false, Status.PENDING, true);
        // the transition
        Transition transition;
        // apply a bad result
        transition = this.computeResultTransition(check, state, newResult(false, "WARNING", "warning"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is not a hard change", transition.hardChange == false);
        assertTrue("Is not an alert", transition.alert == false);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is not ok", transition.nextState.isOk() == false);
        assertTrue("Next state is warning", transition.nextState.getStatus() == Status.WARNING);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is not hard", transition.nextState.isHard() == false);
        assertTrue("Next state is transitioning", transition.nextState.isTransitioning() == true);
        assertTrue("Next state last hard ok copied", transition.previousState.isOk() == transition.nextState.isLastHardOk());
        assertTrue("Next state last hard status copied", transition.previousState.getStatus() == transition.nextState.getLastHardStatus());
        state = transition.nextState;
        // apply a second bad result
        transition = this.computeResultTransition(check, state, newResult(false, "WARNING", "warning"));
        assertTrue("Is not a state change", transition.stateChange == false);
        assertTrue("Is not a hard change", transition.hardChange == false);
        assertTrue("Is not an alert", transition.alert == false);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is not ok", transition.nextState.isOk() == false);
        assertTrue("Next state is warning", transition.nextState.getStatus() == Status.WARNING);
        assertTrue("Next state attempt is 2", transition.nextState.getAttempt() == 2);
        assertTrue("Next state is not hard", transition.nextState.isHard() == false);
        assertTrue("Next state is transitioning", transition.nextState.isTransitioning() == true);
        state = transition.nextState;
        // apply a final bad result
        transition = this.computeResultTransition(check, state, newResult(false, "CRITICAL", "critical"));
        assertTrue("Is not a state change", transition.stateChange == false);
        assertTrue("Is a hard change", transition.hardChange == true);
        assertTrue("Is an alert", transition.alert == true);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is not ok", transition.nextState.isOk() == false);
        assertTrue("Next state is critical", transition.nextState.getStatus() == Status.CRITICAL);
        assertTrue("Next state attempt is 3", transition.nextState.getAttempt() == 3);
        assertTrue("Next state is hard", transition.nextState.isHard() == true);
        assertTrue("Next state is no transitioning", transition.nextState.isTransitioning() == false);
    }
    
    @Test
    public void testSimpleCombinedTransition()
    {
        RealCheck<?,?> check = newCheck(2, 2);
        CheckState state = newState(Status.OK, true, 2, false, Status.PENDING, true);
        // the transition
        Transition transition;
        // apply a bad result
        transition = this.computeResultTransition(check, state, newResult(false, "CRITICAL", "critical"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is not a hard change", transition.hardChange == false);
        assertTrue("Is not an alert", transition.alert == false);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is not ok", transition.nextState.isOk() == false);
        assertTrue("Next state is critical", transition.nextState.getStatus() == Status.CRITICAL);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is not hard", transition.nextState.isHard() == false);
        assertTrue("Next state is transitioning", transition.nextState.isTransitioning() == true);
        assertTrue("Next state last hard ok copied", transition.previousState.isOk() == transition.nextState.isLastHardOk());
        assertTrue("Next state last hard status copied", transition.previousState.getStatus() == transition.nextState.getLastHardStatus());
        state = transition.nextState;
        // apply a second bad result
        transition = this.computeResultTransition(check, state, newResult(false, "CRITICAL", "critical"));
        assertTrue("Is not a state change", transition.stateChange == false);
        assertTrue("Is a hard change", transition.hardChange == true);
        assertTrue("Is an alert", transition.alert == true);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is not ok", transition.nextState.isOk() == false);
        assertTrue("Next state is critical", transition.nextState.getStatus() == Status.CRITICAL);
        assertTrue("Next state attempt is 2", transition.nextState.getAttempt() == 2);
        assertTrue("Next state is hard", transition.nextState.isHard() == true);
        assertTrue("Next state is no transitioning", transition.nextState.isTransitioning() == false);
        state = transition.nextState;
        // apply a good result
        transition = this.computeResultTransition(check, state, newResult(true, "OK", "ok"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is not a hard change", transition.hardChange == false);
        assertTrue("Is not an alert", transition.alert == false);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is ok", transition.nextState.isOk() == true);
        assertTrue("Next state is ok", transition.nextState.getStatus() == Status.OK);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is not hard", transition.nextState.isHard() == false);
        assertTrue("Next state is transitioning", transition.nextState.isTransitioning() == true);
        assertTrue("Next state last hard ok copied", transition.previousState.isOk() == transition.nextState.isLastHardOk());
        assertTrue("Next state last hard status copied", transition.previousState.getStatus() == transition.nextState.getLastHardStatus());
        state = transition.nextState;
        // apply a second good result
        transition = this.computeResultTransition(check, state, newResult(false, "OK", "ok"));
        assertTrue("Is not a state change", transition.stateChange == false);
        assertTrue("Is a hard change", transition.hardChange == true);
        assertTrue("Is an alert", transition.alert == false);
        assertTrue("Is a recovery", transition.recovery == true);
        assertTrue("Next state is ok", transition.nextState.isOk() == true);
        assertTrue("Next state is ok", transition.nextState.getStatus() == Status.OK);
        assertTrue("Next state attempt is 2", transition.nextState.getAttempt() == 2);
        assertTrue("Next state is hard", transition.nextState.isHard() == true);
        assertTrue("Next state is no transitioning", transition.nextState.isTransitioning() == false);
    }
    
    @Test
    public void testImmediateBadTransition()
    {
        RealCheck<?,?> check = newCheck(1, 1);
        CheckState state = newState(Status.OK, true, 2, false, Status.PENDING, true);
        // the transition
        Transition transition;
        // apply a bad result
        transition = this.computeResultTransition(check, state, newResult(false, "CRITICAL", "critical"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is a hard change", transition.hardChange == true);
        assertTrue("Is an alert", transition.alert == true);
        assertTrue("Is not a recovery", transition.recovery == false);
        assertTrue("Next state is not ok", transition.nextState.isOk() == false);
        assertTrue("Next state is critical", transition.nextState.getStatus() == Status.CRITICAL);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is hard", transition.nextState.isHard() == true);
        assertTrue("Next state is no transitioning", transition.nextState.isTransitioning() == false);
        assertTrue("Next state last hard ok copied", transition.previousState.isOk() == transition.nextState.isLastHardOk());
        assertTrue("Next state last hard status copied", transition.previousState.getStatus() == transition.nextState.getLastHardStatus());
    }
    
    @Test
    public void testImmediateGoodTransition()
    {
        RealCheck<?,?> check = newCheck(1, 1);
        CheckState state = newState(Status.CRITICAL, true, 2, false, Status.OK, true);
        // the transition
        Transition transition;
        // apply a good result
        transition = this.computeResultTransition(check, state, newResult(false, "OK", "ok"));
        assertTrue("Is a state change", transition.stateChange == true);
        assertTrue("Is a hard change", transition.hardChange == true);
        assertTrue("Is an alert", transition.alert == false);
        assertTrue("Is a recovery", transition.recovery == true);
        assertTrue("Next state is ok", transition.nextState.isOk() == true);
        assertTrue("Next state is ok", transition.nextState.getStatus() == Status.OK);
        assertTrue("Next state attempt is 1", transition.nextState.getAttempt() == 1);
        assertTrue("Next state is hard", transition.nextState.isHard() == true);
        assertTrue("Next state is no transitioning", transition.nextState.isTransitioning() == false);
        assertTrue("Next state last hard ok copied", transition.previousState.isOk() == transition.nextState.isLastHardOk());
        assertTrue("Next state last hard status copied", transition.previousState.getStatus() == transition.nextState.getLastHardStatus());
    }
}

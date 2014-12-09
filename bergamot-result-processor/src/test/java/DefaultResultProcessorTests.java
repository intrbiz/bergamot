import java.util.UUID;

import org.junit.Test;

import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.RealCheck;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.result.DefaultResultProcessor;


public class DefaultResultProcessorTests extends DefaultResultProcessor
{
    private CheckState newState(Status status, String output, boolean hard, int attempt, boolean transitioning)
    {
        CheckState state = new CheckState();
        state.setCheckId(UUID.randomUUID());
        state.setStatus(status);
        state.setOk(status.isOk());
        state.setOutput(output);
        state.setHard(hard);
        state.setAttempt(attempt);
        state.setTransitioning(transitioning);
        return state;
    }
    
    private Result newResult(boolean ok, String status, String output)
    {
        Result result = new Result();
        result.setOk(ok);
        result.setStatus(status);
        result.setOutput(output);
        return result;
    }
    
    private RealCheck<?, ?> newCheck(int alertThreshold, int recoveryThreshold)
    {
        Host h = new Host();
        h.setAlertAttemptThreshold(alertThreshold);
        h.setRecoveryAttemptThreshold(recoveryThreshold);
        return h;
    }
    
    @Test
    public void testOkToBadTransition()
    {
        System.out.println("---------------------------------");
        Transition transition;
        // the check
        RealCheck<?, ?> check = newCheck(4, 4);
        // initial state
        CheckState state = newState(Status.OK, "All Good", true, 4, false);
        // result
        Result result = newResult(false, "CRITICAL", "Ooops");
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("O2B: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        state = transition.nextState;
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("O2B: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        state = transition.nextState;
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("O2B: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        state = transition.nextState;
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("O2B: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        state = transition.nextState;
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("O2B: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        state = transition.nextState;
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("O2B: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        state = transition.nextState;
        System.out.println("---------------------------------");
    }
    
    @Test
    public void testSingleOkToBadTransition()
    {
        System.out.println("---------------------------------");
        Transition transition;
        // the check
        RealCheck<?, ?> check = newCheck(1, 1);
        // initial state
        CheckState state = newState(Status.PENDING, "Pending", true, 1, false);
        // result
        Result result = newResult(false, "CRITICAL", "Ooops");
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("SO2B: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        System.out.println("---------------------------------");
    }
    
    @Test
    public void testBadToOkTransition()
    {
        System.out.println("---------------------------------");
        Transition transition;
        // the check
        RealCheck<?, ?> check = newCheck(4, 4);
        // initial state
        CheckState state = newState(Status.CRITICAL, "Not Good", true, 4, false);
        // result
        Result result = newResult(true, "OK", "All Good");
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("B2O: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        state = transition.nextState;
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("B2O: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        state = transition.nextState;
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("B2O: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        state = transition.nextState;
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("B2O: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        state = transition.nextState;
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("B2O: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        state = transition.nextState;
        // apply the result
        transition = this.computeResultTransition(check, state, result);
        System.out.println("B2O: Transition: state_change=" + transition.stateChange + ", hard_change=" + transition.hardChange + " from " + transition.previousState + " to " + transition.nextState);
        state = transition.nextState;
        System.out.println("---------------------------------");
    }
}

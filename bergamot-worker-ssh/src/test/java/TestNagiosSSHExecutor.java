import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.UUID;

import org.junit.Test;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

public class TestNagiosSSHExecutor extends BaseSSHTest
{
    /**
     * Helper to create check
     */
    protected ExecuteCheck nagiosSSHCheck(String commandName, String commandLine)
    {
        ExecuteCheck executeCheck = new ExecuteCheck();
        executeCheck.setId(UUID.randomUUID());
        executeCheck.setCheckType("service");
        executeCheck.setCheckId(UUID.randomUUID());
        executeCheck.setEngine("ssh");
        executeCheck.setExecutor("nagios");
        executeCheck.setName(commandName);
        executeCheck.addParameter("host", "bergamot-test-ssh");
        executeCheck.addParameter("port", "2222");
        executeCheck.addParameter("username", "keyuser");
        executeCheck.addParameter("private_key", this.privateKey);
        executeCheck.addParameter("public_key", this.publicKey);
        executeCheck.addParameter("command_line", commandLine);
        executeCheck.setScheduled(System.currentTimeMillis());
        executeCheck.setTimeout(30_000L);
        return executeCheck;
    }
    
    @Test
    public void testDummyOkCheck()
    {
        ExecuteCheck executeCheck = nagiosSSHCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 0 Test");
        this.run(
            executeCheck, 
            (res) -> {
                ActiveResult result = (ActiveResult) res;
                System.out.println("testDummyOkCheck\n" + result);
                assertThat(result, is(not(nullValue())));
                assertThat(result.getId(), is(equalTo(executeCheck.getId())));
                assertThat(result.getCheckType(), is(equalTo(executeCheck.getCheckType())));
                assertThat(result.getCheckId(), is(equalTo(executeCheck.getCheckId())));
                assertThat(result.getCheck(), is(equalTo(executeCheck)));
                assertThat(result.isOk(), is(equalTo(true)));
                assertThat(result.getStatus(), is(equalTo("OK")));
                assertThat(result.getOutput(), is(equalTo("OK: Test")));
                assertThat(result.getRuntime(), is(greaterThan(0D)));
                assertThat(result.getExecuted(), is(not(nullValue())));
                assertThat(result.getProcessed(), is(equalTo(0L)));
            }
        );
    }
    
    @Test
    public void testBadAuth()
    {
        ExecuteCheck executeCheck = nagiosSSHCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 0 Test");
        executeCheck.removeParameter("public_key");
        executeCheck.removeParameter("private_key");
        executeCheck.removeParameter("password");
        this.run(
            executeCheck, 
            (res) -> {
                ActiveResult result = (ActiveResult) res;
                System.out.println("testBadAuth\n" + result);
                assertThat(result, is(not(nullValue())));
                assertThat(result.getId(), is(equalTo(executeCheck.getId())));
                assertThat(result.getCheckType(), is(equalTo(executeCheck.getCheckType())));
                assertThat(result.getCheckId(), is(equalTo(executeCheck.getCheckId())));
                assertThat(result.getCheck(), is(equalTo(executeCheck)));
                assertThat(result.isOk(), is(equalTo(false)));
                assertThat(result.getStatus(), is(equalTo("ERROR")));
                assertThat(result.getOutput(), is(notNullValue()));
                assertThat(result.getExecuted(), is(not(nullValue())));
                assertThat(result.getProcessed(), is(equalTo(0L)));
            }
        );
    }
    
    @Test
    public void testBadCommand()
    {
        ExecuteCheck executeCheck = nagiosSSHCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 0 Test");
        executeCheck.removeParameter("command_line");
        this.run(
            executeCheck, 
            (res) -> {
                ActiveResult result = (ActiveResult) res;
                System.out.println("testBadCommand\n" + result);
                assertThat(result, is(not(nullValue())));
                assertThat(result.getId(), is(equalTo(executeCheck.getId())));
                assertThat(result.getCheckType(), is(equalTo(executeCheck.getCheckType())));
                assertThat(result.getCheckId(), is(equalTo(executeCheck.getCheckId())));
                assertThat(result.getCheck(), is(equalTo(executeCheck)));
                assertThat(result.isOk(), is(equalTo(false)));
                assertThat(result.getStatus(), is(equalTo("ERROR")));
                assertThat(result.getOutput(), is(equalTo("The 'command_line' parameter must be given")));
                assertThat(result.getExecuted(), is(not(nullValue())));
                assertThat(result.getProcessed(), is(equalTo(0L)));
            }
        );
    }
    
    @Test
    public void testBadPasswordAuth()
    {
        ExecuteCheck executeCheck = nagiosSSHCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 0 Test");
        executeCheck.removeParameter("public_key");
        executeCheck.removeParameter("private_key");
        executeCheck.addParameter("password", "password");
        this.run(
            executeCheck, 
            (res) -> {
                ActiveResult result = (ActiveResult) res;
                System.out.println("testBadPasswordAuth\n" + result);
                assertThat(result, is(not(nullValue())));
                assertThat(result.getId(), is(equalTo(executeCheck.getId())));
                assertThat(result.getCheckType(), is(equalTo(executeCheck.getCheckType())));
                assertThat(result.getCheckId(), is(equalTo(executeCheck.getCheckId())));
                assertThat(result.getCheck(), is(equalTo(executeCheck)));
                assertThat(result.isOk(), is(equalTo(false)));
                assertThat(result.getStatus(), is(equalTo("ERROR")));
                assertThat(result.getExecuted(), is(not(nullValue())));
                assertThat(result.getProcessed(), is(equalTo(0L)));
            }
        );
    }
}

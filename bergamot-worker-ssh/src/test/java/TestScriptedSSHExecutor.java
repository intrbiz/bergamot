import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.UUID;

import org.junit.Test;

import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

public class TestScriptedSSHExecutor extends BaseSSHTest
{
    /**
     * Helper to create check
     */
    protected ExecuteCheck nagiosSSHCheck(String script)
    {
        ExecuteCheck executeCheck = new ExecuteCheck();
        executeCheck.setId(UUID.randomUUID());
        executeCheck.setCheckType("service");
        executeCheck.setCheckId(UUID.randomUUID());
        executeCheck.setEngine("ssh");
        executeCheck.setExecutor("script");
        executeCheck.setScript(script);
        executeCheck.addParameter("host", "bergamot-test-ssh");
        executeCheck.addParameter("port", "2222");
        executeCheck.addParameter("username", "keyuser");
        executeCheck.addParameter("private_key", this.privateKey);
        executeCheck.addParameter("public_key", this.publicKey);
        executeCheck.setScheduled(System.currentTimeMillis());
        executeCheck.setTimeout(30_000L);
        return executeCheck;
    }
    
    @Test
    public void testGetHostId()
    {
        ExecuteCheck executeCheck = nagiosSSHCheck(
            "ssh.addIdentity(check.getParameter('private_key'), check.getParameter('public_key'));" +
            "ssh.connect(check.getParameter('username'), check.getParameter('host'), check.getIntParameter('port'), function(session) {" +
            "    bergamot.info('Host Id: ' + session.hostId());" +
            "});");
        this.run(
            executeCheck, 
            (res) -> {
                ActiveResult result = (ActiveResult) res;
                System.out.println("testGetHostId:\n" + result);
                assertThat(result, is(not(nullValue())));
                assertThat(result.isOk(), is(equalTo(true)));
                assertThat(result.getStatus(), is(equalTo("INFO")));
                assertThat(result.getOutput(), is(notNullValue()));
            }
        );
    }
}

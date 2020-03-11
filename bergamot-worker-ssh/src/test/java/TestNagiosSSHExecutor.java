import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import com.intrbiz.bergamot.cluster.broker.AgentEventQueue;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyLookup;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.EngineContext;
import com.intrbiz.bergamot.worker.engine.ssh.SSHEngine;
import com.intrbiz.configuration.Configuration;

public class TestNagiosSSHExecutor
{
    private SSHEngine engine;
    
    private String publicKey;
    
    private String privateKey;

    /**
     * Setup the runner
     */
    @Before
    public void setup() throws Exception
    {
        this.engine = new SSHEngine();
        this.engine.prepare(new EngineContext() {
            @Override
            public Configuration getConfiguration()
            {
                return new Configuration();
            }

            @Override
            public AgentKeyLookup getAgentKeyLookup()
            {
                return null;
            }

            @Override
            public AgentEventQueue getAgentEventQueue()
            {
                return null;
            }

            @Override
            public void registerAgent(UUID agentId)
            {
            }

            @Override
            public void unregisterAgent(UUID agentId)
            {
            }

            @Override
            public void publishResult(ResultMO result)
            {
            }

            @Override
            public void publishReading(ReadingParcelMO readingParcelMO)
            {
            }            
        });
        // keys
        this.privateKey = new String(readFile(new File(System.getProperty("user.home") + "/.ssh/id_rsa")));
        this.publicKey = new String(readFile(new File(System.getProperty("user.home") + "/.ssh/id_rsa.pub")));
    }

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
        executeCheck.addParameter("host", "127.0.0.1");
        executeCheck.addParameter("username", System.getProperty("user.name"));
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
                ActiveResultMO result = (ActiveResultMO) res;
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
                ActiveResultMO result = (ActiveResultMO) res;
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
                ActiveResultMO result = (ActiveResultMO) res;
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
                ActiveResultMO result = (ActiveResultMO) res;
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
    
    private void run(ExecuteCheck check, Consumer<ResultMO> onResult)
    {
        this.run(check, onResult, null);
    }
    
    private void run(ExecuteCheck check, Consumer<ResultMO> onResult, Consumer<ReadingParcelMO> onReading)
    {
        TestContext context = new TestContext(onResult, onReading);
        this.engine.execute(check, context);
    }
    
    private static class TestContext implements CheckExecutionContext
    {
        private Consumer<ResultMO> onResult;
        
        private Consumer<ReadingParcelMO> onReading;
        
        public TestContext(Consumer<ResultMO> onResult, Consumer<ReadingParcelMO> onReading)
        {
            this.onResult = onResult;
            this.onReading = onReading;
        }

        @Override
        public void publishReading(ReadingParcelMO readingParcelMO)
        {
            if (this.onReading != null)
                this.onReading.accept(readingParcelMO);
        }

        @Override
        public void publishResult(ResultMO resultMO)
        {
            if (this.onResult != null)
                this.onResult.accept(resultMO);
        }
    }
    
    private static byte[] readFile(File file) throws IOException
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            try (FileInputStream in = new FileInputStream(file))
            {
                byte[] buf = new byte[1024];
                int r;
                while ((r = in.read(buf)) != -1)
                {
                    baos.write(buf, 0, r);
                }
            }
            return baos.toByteArray();
        }
    }
}

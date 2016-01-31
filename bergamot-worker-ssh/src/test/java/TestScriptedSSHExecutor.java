import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.junit.Before;
import org.junit.Test;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.worker.engine.ssh.SSHEngine;
import com.intrbiz.bergamot.worker.engine.ssh.ScriptedSSHExecutor;

public class TestScriptedSSHExecutor
{
    private SSHEngine engine;
    
    private ScriptExecutorTester runner;
    
    private String publicKey;
    
    private String privateKey;

    /**
     * Setup the runner
     */
    @Before
    public void setup() throws Exception
    {
        this.engine = new TestSSHEngine();
        this.engine.configure(new EngineCfg(SSHEngine.class));
        this.engine.start();
        this.runner = new ScriptExecutorTester();
        this.runner.setEngine(engine);
        // keys
        this.privateKey = new String(readFile(new File(System.getProperty("user.home") + "/.ssh/id_rsa")));
        this.publicKey = new String(readFile(new File(System.getProperty("user.home") + "/.ssh/id_rsa.pub")));
    }

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
        executeCheck.addParameter("host", "127.0.0.1");
        executeCheck.addParameter("username", System.getProperty("user.name"));
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
            "ssh.connect(check.getParameter('username'), check.getParameter('host'), function(session) {" +
            "    bergamot.info('Host Id: ' + session.hostId());" +
            "});");
        this.runner.test(
            executeCheck, 
            (key, res) -> {
                ActiveResultMO result = (ActiveResultMO) res;
                assertThat(result, is(not(nullValue())));
                assertThat(result.getId(), is(equalTo(executeCheck.getId())));
                assertThat(result.getCheckType(), is(equalTo(executeCheck.getCheckType())));
                assertThat(result.getCheckId(), is(equalTo(executeCheck.getCheckId())));
                assertThat(result.getCheck(), is(equalTo(executeCheck)));
                assertThat(result.isOk(), is(equalTo(true)));
                assertThat(result.getStatus(), is(equalTo("INFO")));
                assertThat(result.getOutput(), is(notNullValue()));
                assertThat(result.getRuntime(), is(greaterThan(0D)));
                assertThat(result.getExecuted(), is(not(nullValue())));
                assertThat(result.getProcessed(), is(equalTo(0L)));
            }
        );
    }
    
    private static class ScriptExecutorTester extends ScriptedSSHExecutor
    {
        private BiConsumer<ResultKey, ResultMO> onResult;
        
        private BiConsumer<ReadingKey, ReadingParcelMO> onReading;

        public void setOnResult(BiConsumer<ResultKey, ResultMO> onResult)
        {
            this.onResult = onResult;
        }

        public void setOnReading(BiConsumer<ReadingKey, ReadingParcelMO> onReading)
        {
            this.onReading = onReading;
        }

        @Override
        public void publishReading(ReadingKey key, ReadingParcelMO readingParcelMO)
        {
            if (this.onReading != null) this.onReading.accept(key, readingParcelMO);
        }

        @Override
        public void publishResult(ResultKey key, ResultMO resultMO)
        {
            if (this.onResult != null) this.onResult.accept(key, resultMO);
        }
        
        public void test(ExecuteCheck check, BiConsumer<ResultKey, ResultMO> onResult, BiConsumer<ReadingKey, ReadingParcelMO> onReading)
        {
            this.setOnResult(onResult);
            this.setOnReading(onReading);
            this.execute(check);
        }
        
        public void test(ExecuteCheck check, BiConsumer<ResultKey, ResultMO> onResult)
        {
            this.test(check, onResult, null);
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
    
    private static class TestSSHEngine extends SSHEngine
    {
        public void start() throws Exception
        {
            startEngineServices();   
        }
    }
}

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.Before;

import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;
import com.intrbiz.bergamot.model.message.processor.agent.ProcessorAgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.processor.result.PassiveResult;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.CheckEngineContext;
import com.intrbiz.bergamot.worker.engine.ssh.SSHEngine;

public abstract class BaseSSHTest
{
    protected SSHEngine engine;
    
    protected String publicKey;
    
    protected String privateKey;
    
    /**
     * Setup the runner
     */
    @Before
    public void setup() throws Exception
    {
        this.engine = new SSHEngine();
        this.engine.prepare(new CheckEngineContext() {
            
            @Override
            public void lookupAgentKey(UUID keyId, Consumer<AgentAuthenticationKey> callback)
            {
            }

            @Override
            public void publishAgentAction(ProcessorAgentMessage event)
            {
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
            public void publishResult(ResultMessage result)
            {
            }

            @Override
            public void publishReading(ReadingParcelMessage readingParcelMO)
            {
            }            
        });
        // keys
        this.privateKey = new String(readResource("/test_key"));
        this.publicKey = new String(readResource("/test_key.pub"));
    }
    
    protected void run(ExecuteCheck check, Consumer<ResultMessage> onResult)
    {
        this.run(check, onResult, null);
    }
    
    protected void run(ExecuteCheck check, Consumer<ResultMessage> onResult, Consumer<ReadingParcelMessage> onReading)
    {
        TestContext context = new TestContext(onResult, onReading);
        this.engine.execute(check, context);
    }
    
    public static class TestContext implements CheckExecutionContext
    {
        private Consumer<ResultMessage> onResult;
        
        private Consumer<ReadingParcelMessage> onReading;
        
        public TestContext(Consumer<ResultMessage> onResult, Consumer<ReadingParcelMessage> onReading)
        {
            this.onResult = onResult;
            this.onReading = onReading;
        }

        @Override
        public void publishReading(ReadingParcelMessage readingParcelMO)
        {
            if (this.onReading != null)
                this.onReading.accept(readingParcelMO);
        }

        @Override
        public void publishResult(ResultMessage resultMO)
        {
            if (this.onResult != null)
                this.onResult.accept(resultMO);
        }

        @Override
        public void publishActiveResult(ActiveResult result)
        {
            this.publishResult(result);
        }

        @Override
        public void publishPassiveResult(PassiveResult result)
        {
            this.publishResult(result);
        }
    }
    
    private static byte[] readResource(String path) throws IOException
    {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            try (InputStream in = TestNagiosSSHExecutor.class.getResourceAsStream(path))
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

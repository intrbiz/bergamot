import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.Before;

import com.intrbiz.bergamot.cluster.broker.AgentEventQueue;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyLookup;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.EngineContext;
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
        this.engine.prepare(new EngineContext() {
            
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
        this.privateKey = new String(readResource("/test_key"));
        this.publicKey = new String(readResource("/test_key.pub"));
    }
    
    protected void run(ExecuteCheck check, Consumer<ResultMO> onResult)
    {
        this.run(check, onResult, null);
    }
    
    protected void run(ExecuteCheck check, Consumer<ResultMO> onResult, Consumer<ReadingParcelMO> onReading)
    {
        TestContext context = new TestContext(onResult, onReading);
        this.engine.execute(check, context);
    }
    
    public static class TestContext implements CheckExecutionContext
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

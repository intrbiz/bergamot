package com.intrbiz.bergamot.worker.engine.http;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.processor.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.EngineContext;

@Ignore
public class TestHTTPEngine
{
    private HTTPEngine engine;
    
    @Before
    public void setupEngine() throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        //
        this.engine = new HTTPEngine();
        this.engine.prepare(new EngineContext() {

            @Override
            public void lookupAgentKey(UUID keyId, Consumer<AgentAuthenticationKey> callback)
            {
            }

            @Override
            public void publishAgentAction(AgentMessage event)
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
            public void publishReading(ReadingParcelMO readingParcelMO)
            {
            }
        });
    }
    
    @Test
    public void testRedirectHTTPCheck() throws Exception
    {
        // the check to execute
        ExecuteCheck check = new ExecuteCheck();
        check.setEngine("http");
        check.setName("check_http");
        check.setCheckType("service");
        check.setCheckId(UUID.randomUUID());
        check.setProcessor(new UUID(0, 0));
        check.setScheduled(System.currentTimeMillis());
        // parameters
        check.setParameter("host", "intrbiz.com");
        check.setParameter("ssl", "false");
        // execute
        ResultMessage resultMO = this.run(check);
        // check
        System.out.println("Got result: " + resultMO);
    }
    
    @Test
    public void testSimpleHTTPSCheck() throws Exception
    {
        // the check to execute
        ExecuteCheck check = new ExecuteCheck();
        check.setEngine("http");
        check.setName("check_http");
        check.setCheckType("service");
        check.setCheckId(UUID.randomUUID());
        check.setProcessor(new UUID(0, 0));
        check.setScheduled(System.currentTimeMillis());
        // parameters
        check.setParameter("host", "intrbiz.com");
        check.setParameter("ssl", "true");
        check.setParameter("contains", "Intrbiz Blog");
        check.setParameter("length", "1024");
        // execute
        ResultMessage resultMO = this.run(check);
        System.out.println("Got result: " + resultMO);
    }
    
    @Test
    public void testSimpleTLSCertificateCheck() throws Exception
    {
        ExecuteCheck check = new ExecuteCheck();
        check.setEngine("http");
        check.setExecutor("certificate");
        check.setName("check_certificate");
        check.setCheckType("service");
        check.setCheckId(UUID.randomUUID());
        check.setProcessor(new UUID(0, 0));
        check.setScheduled(System.currentTimeMillis());
        // parameters
        check.setParameter("host", "intrbiz.com");
        // execute
        ResultMessage resultMO = this.run(check);
        System.out.println("Got result: " + resultMO);
    }
    
    /*@Test
    public void testScriptedHTTPCheck() throws Exception
    {
        // the check to execute
        ExecuteCheck check = new ExecuteCheck();
        check.setEngine("http");
        check.setExecutor("script");
        check.setName("check_http_script");
        check.setCheckType("service");
        check.setCheckId(UUID.randomUUID());
        check.setProcessingPool(1);
        check.setScheduled(System.currentTimeMillis());
        // parameters
        check.setScript(
                "http.get('http://10.250.100.144:15672/api/overview').basicAuth('monitor', 'monitor').execute("
                + " function(r) {"
                + "   if (r.status() == 200) { "
                + "     var res = JSON.parse(r.content()); "
                + "     bergamot.info('RabbitMQ Version: ' + res.rabbitmq_version); "
                + "   } else {"
                + "     bergamot.error('RabbitMQ API returned: ' + r.status());"
                + "   }"
                + " }, "
                + " function(e) { "
                + "   bergamot.error(e); "
                + " }"
                + ");"
        );
        // execute
        ResultMO resultMO = this.engine.test(check);
        assertThat(resultMO, is(notNullValue()));
        assertThat(resultMO.getStatus(), is(equalTo("INFO")));
        System.out.println("Got result: " + resultMO);
    }*/
    
    @Test
    public void testJsoupHTTPCheck() throws Exception
    {
        // the check to execute
        ExecuteCheck check = new ExecuteCheck();
        check.setEngine("http");
        check.setExecutor("script");
        check.setName("check_http_script");
        check.setCheckType("service");
        check.setCheckId(UUID.randomUUID());
        check.setProcessor(new UUID(0, 0));
        check.setScheduled(System.currentTimeMillis());
        // parameters
        check.setScript(
                "http.get('https://intrbiz.com/').execute("
                + " function(r) {"
                + "   if (r.status() == 200) { "
                + "     var doc = r.parseHTML();"
                + "     var title = doc.select('title').text();"
                + "     if (title.contains('Intrbiz')) {"
                + "       bergamot.ok('Page all ok, title: ' + title);"
                + "     } else {"
                + "       bergamot.action('Wasn\\'t expecting: ' + title);"
                + "     }"
                + "   } else {"
                + "     bergamot.error('Request returned: ' + r.status());"
                + "   }"
                + " }, "
                + " function(e) { "
                + "   bergamot.error(e); "
                + " }"
                + ");"
        );
        // execute
        ResultMessage resultMO = this.run(check);
        assertThat(resultMO, is(notNullValue()));
        assertThat(resultMO.getStatus(), is(equalTo("OK")));
        System.out.println("Got result: " + resultMO);
    }
    
    private ResultMessage run(ExecuteCheck check) throws InterruptedException
    {
        // multi-threaded so we need to wait
        final Object lock = new Object();
        final ResultMessage[] results = new ResultMessage[1];
        TestContext context = new TestContext((result) -> {
            // ok we got a result
            results[0] = result;
            // all done
            synchronized (lock)
            {
                lock.notifyAll();
            }
        }, null);
        this.engine.execute(check, context);
        // await execution
        if (results[0] == null)
        {
            synchronized (lock)
            {
                lock.wait();
            }
        }
        return results[0];
    }
    
    private static class TestContext implements CheckExecutionContext
    {
        private Consumer<ResultMessage> onResult;
        
        private Consumer<ReadingParcelMO> onReading;
        
        public TestContext(Consumer<ResultMessage> onResult, Consumer<ReadingParcelMO> onReading)
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
        public void publishResult(ResultMessage resultMO)
        {
            if (this.onResult != null)
                this.onResult.accept(resultMO);
        }
    }
}

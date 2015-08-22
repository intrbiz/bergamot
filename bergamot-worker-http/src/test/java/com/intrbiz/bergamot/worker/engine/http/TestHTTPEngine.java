package com.intrbiz.bergamot.worker.engine.http;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.UUID;
import java.util.function.BiConsumer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.queue.key.ResultKey;

public class TestHTTPEngine
{
    private HTTPEngineTester engine;
    
    @Before
    public void setupEngine() throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        //
        this.engine = new HTTPEngineTester();
        this.engine.configure(new EngineCfg(HTTPEngine.class, new ExecutorCfg(HTTPExecutor.class), new ExecutorCfg(CertificateExecutor.class), new ExecutorCfg(ScriptedHTTPExecutor.class)));
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
        check.setProcessingPool(1);
        check.setScheduled(System.currentTimeMillis());
        // parameters
        check.setParameter("host", "intrbiz.com");
        check.setParameter("ssl", "false");
        // execute
        ResultMO resultMO = this.engine.test(check);
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
        check.setProcessingPool(1);
        check.setScheduled(System.currentTimeMillis());
        // parameters
        check.setParameter("host", "intrbiz.com");
        check.setParameter("ssl", "true");
        check.setParameter("contains", "Intrbiz Blog");
        check.setParameter("length", "1024");
        // execute
        ResultMO resultMO = this.engine.test(check);
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
        check.setProcessingPool(1);
        check.setScheduled(System.currentTimeMillis());
        // parameters
        check.setParameter("host", "intrbiz.com");
        // execute
        ResultMO resultMO = this.engine.test(check);
        System.out.println("Got result: " + resultMO);
    }
    
    @Test
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
    }
    
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
        check.setProcessingPool(1);
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
        ResultMO resultMO = this.engine.test(check);
        assertThat(resultMO, is(notNullValue()));
        assertThat(resultMO.getStatus(), is(equalTo("OK")));
        System.out.println("Got result: " + resultMO);
    }
    
    private class HTTPEngineTester extends HTTPEngine
    {
        private BiConsumer<ResultKey, ResultMO> onResult;
        
        private BiConsumer<ReadingKey, ReadingParcelMO> onReading;

        public void setOnResult(BiConsumer<ResultKey, ResultMO> onResult)
        {
            this.onResult = onResult;
        }

        @SuppressWarnings("unused")
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
        
        public ResultMO test(ExecuteCheck check) throws Exception
        {
            // setup
            // multi-threaded so we need to wait
            final Object lock = new Object();
            final ResultMO[] results = new ResultMO[1];
            this.setOnResult((key, result) -> {
                // ok we got a result
                results[0] = result;
                // all done
                synchronized (lock)
                {
                    lock.notifyAll();
                }
            });
            this.execute(check);
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
    }
}

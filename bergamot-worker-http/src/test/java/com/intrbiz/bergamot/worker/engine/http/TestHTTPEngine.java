package com.intrbiz.bergamot.worker.engine.http;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;

public class TestHTTPEngine
{
    private HTTPEngine engine;
    
    @Before
    public void setupEngine() throws Exception
    {
        this.engine = new HTTPEngine();
        this.engine.configure(new EngineCfg(HTTPEngine.class, new ExecutorCfg(HTTPExecutor.class)));
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
        // multi-threaded so we need to wait
        final Object lock = new Object();
        final Result[] results = new Result[1];
        this.engine.execute(check, (result) -> {
            // ok we got a result
            results[0] = result;
            // all done
            synchronized (lock)
            {
                lock.notifyAll();
            }
        });
        // await execution
        synchronized (lock)
        {
            lock.wait();
        }
        // check
        Result result = results[0];
        System.out.println("Got result: " + result);
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
        // multi-threaded so we need to wait
        final Object lock = new Object();
        final Result[] results = new Result[1];
        this.engine.execute(check, (result) -> {
            // ok we got a result
            results[0] = result;
            // all done
            synchronized (lock)
            {
                lock.notifyAll();
            }
        });
        // await execution
        synchronized (lock)
        {
            lock.wait();
        }
        // check
        Result result = results[0];
        System.out.println("Got result: " + result);
    }
}

package com.intrbiz.bergamot.worker.engine.nagios;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.UUID;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.processor.result.PassiveResult;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;

public class TestNagiosEngine
{
    private NagiosEngine engine;

    /**
     * Setup the runner
     */
    @Before
    public void setup()
    {
        this.engine = new NagiosEngine();
    }

    /**
     * Helper to create check
     */
    protected ExecuteCheck nagiosCheck(String commandName, String commandLine)
    {
        ExecuteCheck executeCheck = new ExecuteCheck();
        executeCheck.setId(UUID.randomUUID());
        executeCheck.setCheckType("service");
        executeCheck.setCheckId(UUID.randomUUID());
        executeCheck.setEngine("nagios");
        executeCheck.setName(commandName);
        executeCheck.addParameter("command_line", commandLine);
        executeCheck.setScheduled(System.currentTimeMillis());
        executeCheck.setTimeout(30_000L);
        return executeCheck;
    }
    
    @Test
    public void testAcceptNagiosCheck()
    {
        ExecuteCheck executeCheck = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 0 Test");
        assertThat(this.engine.accept(executeCheck), is(equalTo(true)));
    }
    
    @Test(expected=NullPointerException.class)
    public void testDoesNotAcceptNullCheck()
    {
        assertThat(this.engine.accept(null), is(equalTo(false)));
    }

    @Test
    public void testDummyOkCheck()
    {
        ExecuteCheck executeCheck = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 0 Test");
        this.run(
            executeCheck, 
            (res) -> {
                ActiveResult result = (ActiveResult) res;
                assertThat(result, is(not(nullValue())));
                assertThat(result.isOk(), is(equalTo(true)));
                assertThat(result.getStatus(), is(equalTo("OK")));
                assertThat(result.getOutput(), is(equalTo("OK: Test")));
            }
        );
    }
    
    @Test
    public void testDummyWarningCheck()
    {
        ExecuteCheck executeCheck = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 1 Test");
        this.run(
            executeCheck,
            (res) -> {
                ActiveResult result = (ActiveResult) res;
                assertThat(result, is(not(nullValue())));
                assertThat(result.isOk(), is(equalTo(false)));
                assertThat(result.getStatus(), is(equalTo("WARNING")));
                assertThat(result.getOutput(), is(equalTo("WARNING: Test")));
            }
        );
    }
    
    @Test
    public void testDummyCriticalCheck()
    {
        ExecuteCheck executeCheck = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 2 Test");
        this.run(
            executeCheck,
            (res) -> {
                ActiveResult result = (ActiveResult) res;
                assertThat(result, is(not(nullValue())));
                assertThat(result.isOk(), is(equalTo(false)));
                assertThat(result.getStatus(), is(equalTo("CRITICAL")));
                assertThat(result.getOutput(), is(equalTo("CRITICAL: Test")));
            }
        );
    }
    
    @Test
    public void testDummyUnknownCheck()
    {
        ExecuteCheck executeCheck = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 3 Test");
        this.run(
            executeCheck,
            (res) -> {
                ActiveResult result = (ActiveResult) res;
                assertThat(result, is(not(nullValue())));
                assertThat(result.isOk(), is(equalTo(false)));
                assertThat(result.getStatus(), is(equalTo("UNKNOWN")));
                assertThat(result.getOutput(), is(equalTo("UNKNOWN: Test")));
            }
        );
    }
    
    @Test
    public void testMissing()
    {
        ExecuteCheck executeCheck = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_missing");
        this.run(
            executeCheck,
            (res) -> {
                ActiveResult result = (ActiveResult) res;
                assertThat(result, is(not(nullValue())));
                assertThat(result.isOk(), is(equalTo(false)));
                assertThat(result.getStatus(), is(equalTo("ERROR")));
                assertThat(result.getOutput(), is(not(nullValue())));
            }
        );
    }
    
    /*@Test
    public void testFailingNRPE()
    {
        // deliberately wrong port number
        ExecuteCheck executeCheck = nagiosCheck("check_nrpe", "/usr/lib/nagios/plugins/check_nrpe -H 127.0.0.1 -p 35666 -t 15 -c check_load");
        this.runner.test(
            executeCheck,
            (key, res) -> {
                ActiveResultMO result = (ActiveResultMO) res;
                assertThat(result, is(not(nullValue())));
                assertThat(result.getId(), is(equalTo(executeCheck.getId())));
                assertThat(result.getCheckType(), is(equalTo(executeCheck.getCheckType())));
                assertThat(result.getCheckId(), is(equalTo(executeCheck.getCheckId())));
                assertThat(result.getCheck(), is(equalTo(executeCheck)));
                assertThat(result.isOk(), is(equalTo(false)));
                assertThat(result.getStatus(), equalTo("ERROR"));
                assertThat(result.getOutput(), is(not(nullValue())));
                assertThat(result.getRuntime(), is(greaterThan(0D)));
                assertThat(result.getExecuted(), is(not(nullValue())));
                assertThat(result.getProcessed(), is(equalTo(0L)));
            }
        );
    }*/
    
    /*@Test
    public void testNRPE()
    {
        ExecuteCheck executeCheck = nagiosCheck("check_nrpe", "/usr/lib/nagios/plugins/check_nrpe -H 127.0.0.1 -t 15 -c check_load");
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
                assertThat(result.getStatus(), is(equalTo("OK")));
                assertThat(result.getOutput(), is(not(nullValue())));
                assertThat(result.getRuntime(), is(greaterThan(0D)));
                assertThat(result.getExecuted(), is(not(nullValue())));
                assertThat(result.getProcessed(), is(equalTo(0L)));
            }
        );
    }*/
    
    private void run(ExecuteCheck check, Consumer<ResultMessage> onResult)
    {
        this.run(check, onResult, null);
    }
    
    private void run(ExecuteCheck check, Consumer<ResultMessage> onResult, Consumer<ReadingParcelMessage> onReading)
    {
        NagiosTestContext context = new NagiosTestContext(onResult, onReading);
        this.engine.execute(check, context);
    }
    
    private static class NagiosTestContext implements CheckExecutionContext
    {
        private Consumer<ResultMessage> onResult;
        
        private Consumer<ReadingParcelMessage> onReading;
        
        public NagiosTestContext(Consumer<ResultMessage> onResult, Consumer<ReadingParcelMessage> onReading)
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
}

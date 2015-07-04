package com.intrbiz.bergamot.worker.runner.nagios;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.UUID;
import java.util.function.BiConsumer;

import org.junit.Before;
import org.junit.Test;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.worker.engine.nagios.NagiosExecutor;

public class TestNagiosRunner
{
    private NagiosExecutorTester runner;

    /**
     * Setup the runner
     */
    @Before
    public void setup()
    {
        this.runner = new NagiosExecutorTester();
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
        assertThat(this.runner.accept(executeCheck), is(equalTo(true)));
    }
    
    @Test(expected=NullPointerException.class)
    public void testDoesNotAcceptNullCheck()
    {
        assertThat(this.runner.accept(null), is(equalTo(false)));
    }

    @Test
    public void testDummyOkCheck()
    {
        ExecuteCheck executeCheck = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 0 Test");
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
                assertThat(result.getOutput(), is(equalTo("OK: Test")));
                assertThat(result.getRuntime(), is(greaterThan(0D)));
                assertThat(result.getExecuted(), is(not(nullValue())));
                assertThat(result.getProcessed(), is(equalTo(0L)));
            }
        );
    }
    
    @Test
    public void testDummyWarningCheck()
    {
        ExecuteCheck executeCheck = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 1 Test");
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
                assertThat(result.getStatus(), is(equalTo("WARNING")));
                assertThat(result.getOutput(), is(equalTo("WARNING: Test")));
                assertThat(result.getRuntime(), is(greaterThan(0D)));
                assertThat(result.getExecuted(), is(not(nullValue())));
                assertThat(result.getProcessed(), is(equalTo(0L)));
            }
        );
    }
    
    @Test
    public void testDummyCriticalCheck()
    {
        ExecuteCheck executeCheck = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 2 Test");
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
                assertThat(result.getStatus(), is(equalTo("CRITICAL")));
                assertThat(result.getOutput(), is(equalTo("CRITICAL: Test")));
                assertThat(result.getRuntime(), is(greaterThan(0D)));
                assertThat(result.getExecuted(), is(not(nullValue())));
                assertThat(result.getProcessed(), is(equalTo(0L)));
            }
        );
    }
    
    @Test
    public void testDummyUnknownCheck()
    {
        ExecuteCheck executeCheck = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 3 Test");
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
                assertThat(result.getStatus(), is(equalTo("UNKNOWN")));
                assertThat(result.getOutput(), is(equalTo("UNKNOWN: Test")));
                assertThat(result.getRuntime(), is(greaterThan(0D)));
                assertThat(result.getExecuted(), is(not(nullValue())));
                assertThat(result.getProcessed(), is(equalTo(0L)));
            }
        );
    }
    
    @Test
    public void testMissing()
    {
        ExecuteCheck executeCheck = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_missing");
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
                assertThat(result.getStatus(), is(equalTo("ERROR")));
                assertThat(result.getOutput(), is(not(nullValue())));
                assertThat(result.getRuntime(), is(equalTo(0D)));
                assertThat(result.getExecuted(), is(not(nullValue())));
                assertThat(result.getProcessed(), is(equalTo(0L)));
            }
        );
    }
    
    @Test
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
    }
    
    @Test
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
    }
    
    private static class NagiosExecutorTester extends NagiosExecutor
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
}

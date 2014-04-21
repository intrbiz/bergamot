package com.intrbiz.bergamot.worker.runner.nagios;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.intrbiz.bergamot.model.result.Result;
import com.intrbiz.bergamot.model.result.ResultStatus;
import com.intrbiz.bergamot.model.task.Check;

public class TestNagiosRunner
{
    private NagiosRunner runner;

    /**
     * Setup the runner
     */
    @Before
    public void setup()
    {
        this.runner = new NagiosRunner();
    }

    /**
     * Helper to create check
     */
    protected Check nagiosCheck(String commandName, String commandLine, String... args)
    {
        Check check = new Check();
        check.setId(UUID.randomUUID());
        check.setCheckableType("service");
        check.setCheckableId(UUID.randomUUID());
        check.setEngine("nagios");
        check.setName(commandName);
        // intrinsic parameters
        check.addParameter("HOSTADDRESS", "127.0.0.1");
        check.addParameter("HOSTNAME", "localhost");
        check.addParameter("SERVICEDESCRIPTION", "Dummy Service");
        check.addParameter("command_line", commandLine);
        int argIdx = 1;
        for (String arg : args)
        {
            check.addParameter("ARG" + (argIdx++), arg);
        }
        check.setScheduled(System.currentTimeMillis());
        check.setTimeout(30_000L);
        return check;
    }
    
    @Test
    public void testAcceptNagiosCheck()
    {
        Check check = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy $ARG1$ $ARG2$", "0", "Test");
        assertThat(this.runner.accept(check), is(equalTo(true)));
    }
    
    @Test
    public void testDoesNotAcceptNullCheck()
    {
        assertThat(this.runner.accept(null), is(equalTo(false)));
    }

    @Test
    public void testDummyOkCheck()
    {
        Check check = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 0 Test");
        Result result = this.runner.run(check);
        assertThat(result, is(not(nullValue())));
        assertThat(result.getId(), is(equalTo(check.getId())));
        assertThat(result.getCheckableType(), is(equalTo(check.getCheckableType())));
        assertThat(result.getCheckableId(), is(equalTo(check.getCheckableId())));
        assertThat(result.getCheck(), is(equalTo(check)));
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo(ResultStatus.OK)));
        assertThat(result.getOutput(), is(equalTo("OK: Test")));
        assertThat(result.getRuntime(), is(greaterThan(0D)));
        assertThat(result.getExecuted(), is(not(nullValue())));
        assertThat(result.getProcessed(), is(equalTo(0L)));
    }
    
    @Test
    public void testDummyWarningCheck()
    {
        Check check = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 1 Test");
        Result result = this.runner.run(check);
        assertThat(result, is(not(nullValue())));
        assertThat(result.getId(), is(equalTo(check.getId())));
        assertThat(result.getCheckableType(), is(equalTo(check.getCheckableType())));
        assertThat(result.getCheckableId(), is(equalTo(check.getCheckableId())));
        assertThat(result.getCheck(), is(equalTo(check)));
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo(ResultStatus.WARNING)));
        assertThat(result.getOutput(), is(equalTo("WARNING: Test")));
        assertThat(result.getRuntime(), is(greaterThan(0D)));
        assertThat(result.getExecuted(), is(not(nullValue())));
        assertThat(result.getProcessed(), is(equalTo(0L)));
    }
    
    @Test
    public void testDummyCriticalCheck()
    {
        Check check = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 2 Test");
        Result result = this.runner.run(check);
        assertThat(result, is(not(nullValue())));
        assertThat(result.getId(), is(equalTo(check.getId())));
        assertThat(result.getCheckableType(), is(equalTo(check.getCheckableType())));
        assertThat(result.getCheckableId(), is(equalTo(check.getCheckableId())));
        assertThat(result.getCheck(), is(equalTo(check)));
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo(ResultStatus.CRITICAL)));
        assertThat(result.getOutput(), is(equalTo("CRITICAL: Test")));
        assertThat(result.getRuntime(), is(greaterThan(0D)));
        assertThat(result.getExecuted(), is(not(nullValue())));
        assertThat(result.getProcessed(), is(equalTo(0L)));
    }
    
    @Test
    public void testDummyUnknownCheck()
    {
        Check check = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_dummy 3 Test");
        Result result = this.runner.run(check);
        assertThat(result, is(not(nullValue())));
        assertThat(result.getId(), is(equalTo(check.getId())));
        assertThat(result.getCheckableType(), is(equalTo(check.getCheckableType())));
        assertThat(result.getCheckableId(), is(equalTo(check.getCheckableId())));
        assertThat(result.getCheck(), is(equalTo(check)));
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo(ResultStatus.UNKNOWN)));
        assertThat(result.getOutput(), is(equalTo("UNKNOWN: Test")));
        assertThat(result.getRuntime(), is(greaterThan(0D)));
        assertThat(result.getExecuted(), is(not(nullValue())));
        assertThat(result.getProcessed(), is(equalTo(0L)));
    }
    
    @Test
    public void testMissing()
    {
        Check check = nagiosCheck("check_dummy", "/usr/lib/nagios/plugins/check_missing");
        Result result = this.runner.run(check);
        assertThat(result, is(not(nullValue())));
        assertThat(result.getId(), is(equalTo(check.getId())));
        assertThat(result.getCheckableType(), is(equalTo(check.getCheckableType())));
        assertThat(result.getCheckableId(), is(equalTo(check.getCheckableId())));
        assertThat(result.getCheck(), is(equalTo(check)));
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo(ResultStatus.INTERNAL)));
        assertThat(result.getOutput(), is(not(nullValue())));
        assertThat(result.getRuntime(), is(equalTo(0D)));
        assertThat(result.getExecuted(), is(not(nullValue())));
        assertThat(result.getProcessed(), is(equalTo(0L)));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testFailingNRPE()
    {
        // deliberately wrong port number
        Check check = nagiosCheck("check_nrpe", "/usr/lib/nagios/plugins/check_nrpe -H $HOSTADDRESS$ -p 35666 -t 15 -c check_load");
        Result result = this.runner.run(check);
        assertThat(result, is(not(nullValue())));
        assertThat(result.getId(), is(equalTo(check.getId())));
        assertThat(result.getCheckableType(), is(equalTo(check.getCheckableType())));
        assertThat(result.getCheckableId(), is(equalTo(check.getCheckableId())));
        assertThat(result.getCheck(), is(equalTo(check)));
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), anyOf(equalTo(ResultStatus.CRITICAL), equalTo(ResultStatus.UNKNOWN)));
        assertThat(result.getOutput(), is(not(nullValue())));
        assertThat(result.getRuntime(), is(greaterThan(0D)));
        assertThat(result.getExecuted(), is(not(nullValue())));
        assertThat(result.getProcessed(), is(equalTo(0L)));
    }
    
    @Test
    public void testNRPE()
    {
        Check check = nagiosCheck("check_nrpe", "/usr/lib/nagios/plugins/check_nrpe -H $HOSTADDRESS$ -t 15 -c check_load");
        Result result = this.runner.run(check);
        assertThat(result, is(not(nullValue())));
        assertThat(result.getId(), is(equalTo(check.getId())));
        assertThat(result.getCheckableType(), is(equalTo(check.getCheckableType())));
        assertThat(result.getCheckableId(), is(equalTo(check.getCheckableId())));
        assertThat(result.getCheck(), is(equalTo(check)));
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo(ResultStatus.OK)));
        assertThat(result.getOutput(), is(not(nullValue())));
        assertThat(result.getRuntime(), is(greaterThan(0D)));
        assertThat(result.getExecuted(), is(not(nullValue())));
        assertThat(result.getProcessed(), is(equalTo(0L)));
    }
}

package com.intrbiz.bergamot.model.message.result;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

public class ResultMOTest
{
    @Test
    public void testPending()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.pending("Test");
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo("PENDING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testInfo()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.info("Test");
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo("INFO")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testOk()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.ok("Test");
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo("OK")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testWarning()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.warning("Test");
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testCritical()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.critical("Test");
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("CRITICAL")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testUnknown()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.unknown("Test");
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("UNKNOWN")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testError()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.error("Test");
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("ERROR")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testErrorThrowable()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.error(new Exception("Test"));
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("ERROR")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testTimeout()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.timeout("Test");
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("TIMEOUT")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testDisconnected()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.disconnected("Test");
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("DISCONNECTED")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testAction()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.action("Test");
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("ACTION")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    //
    
    @Test
    public void testApplyThresholdsOkDouble()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(0.1D, 0.3D, 0.2D, 0.65D, 0.5D, 0.75D), 
                0.8D, 
                0.9D,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo("OK")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsOkDouble1()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(0.1D), 
                0.8D, 
                0.9D,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo("OK")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsWarningDouble()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(0.1, 0.3, 0.2, 0.82, 0.65, 0.5, 0.75), 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsWarningDouble1()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(0.1, 0.3, 0.2, 0.82, 0.65, 0.5, 0.75, 0.8), 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsWarningDouble2()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(0.89), 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsWarningDouble3()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(0.9), 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsCriticalDouble()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(0.1, 0.3, 0.2, 0.82, 0.65, 0.5, 0.75, 0.98, 0.92), 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("CRITICAL")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }

    @Test
    public void testApplyThresholdsCriticalDouble1()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(0.91), 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("CRITICAL")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsCriticalDouble2()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(0.9, 0.91, 0.92), 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("CRITICAL")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsOkLong()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(10L, 30L, 20L, 65L, 50L, 75L), 
                80L, 
                90L,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo("OK")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsOkLong1()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(10L), 
                80L, 
                90L,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo("OK")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsWarningLong()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(10L, 30L, 20L, 82L, 65L, 50L, 75L), 
                80L, 
                90L,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsWarningLong1()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(10L, 30L, 20L, 82L, 65L, 50L, 75L, 80L), 
                80L, 
                90L,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsWarningLong2()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(89L), 
                80L, 
                90L,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsWarningLong3()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(90L), 
                80L, 
                90L,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsCriticalLong()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(10L, 30L, 20L, 82L, 65L, 50L, 75L, 98L, 92L), 
                80L, 
                90L,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("CRITICAL")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }

    @Test
    public void testApplyThresholdsCriticalLong1()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(91L), 
                80L, 
                90L,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("CRITICAL")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdsCriticalLong2()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThresholds(
                Arrays.asList(90L, 91L, 92L), 
                80L, 
                90L,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("CRITICAL")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    //
    
    @Test
    public void testApplyThresholdOkDouble()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThreshold(
                0.5, 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo("OK")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdOkDouble1()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThreshold(
                0.8, 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo("OK")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdWarningDouble()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThreshold(
                0.85, 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdWarningDouble1()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThreshold(
                0.90, 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdCriticalDouble()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThreshold(
                0.91, 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("CRITICAL")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdCriticalDouble1()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThreshold(
                0.99, 
                0.8, 
                0.9,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("CRITICAL")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdOkLong()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThreshold(
                50, 
                80, 
                90,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo("OK")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdOkLong1()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThreshold(
                80, 
                80, 
                90,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(true)));
        assertThat(result.getStatus(), is(equalTo("OK")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdWarningLong()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThreshold(
                85, 
                80, 
                90,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdWarningLong1()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThreshold(
                90, 
                80, 
                90,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("WARNING")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdCriticalLong()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThreshold(
                91, 
                80, 
                90,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("CRITICAL")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
    
    @Test
    public void testApplyThresholdCriticalLong1()
    {
        ActiveResultMO result = new ActiveResultMO();
        result.applyGreaterThanThreshold(
                99, 
                80, 
                90,
                "Test"
        );
        assertThat(result.isOk(), is(equalTo(false)));
        assertThat(result.getStatus(), is(equalTo("CRITICAL")));
        assertThat(result.getOutput(), is(equalTo("Test")));
    }
}

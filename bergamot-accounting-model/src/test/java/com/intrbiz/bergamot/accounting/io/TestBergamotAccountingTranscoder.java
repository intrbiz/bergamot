package com.intrbiz.bergamot.accounting.io;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.intrbiz.bergamot.accounting.model.ExecuteCheckAccountingEvent;
import com.intrbiz.bergamot.accounting.model.ProcessResultAccountingEvent;
import com.intrbiz.bergamot.accounting.model.ProcessResultAccountingEvent.ResultType;

public class TestBergamotAccountingTranscoder
{
    private static UUID siteId = UUID.fromString("01cf7f8e-2da3-4b5b-8764-a8cb0e1e8e6b");
    private static UUID execId = UUID.fromString("7c5efc47-8cd4-475c-817c-fba6acf291c6");
    
    private BergamotAccountingTranscoder codec;
    
    @Before()
    public void setup()
    {
        this.codec  = BergamotAccountingTranscoder.getDefault();
    }
    
    @Test
    public void testEncodeExecuteCheckAccountingEventToString()
    {
        ExecuteCheckAccountingEvent original = new ExecuteCheckAccountingEvent(siteId, execId, "nrpe", "nrpe", "check_load");
        String encoded = this.codec.encodeToString(original);
        assertThat(encoded, is(notNullValue()));
        ExecuteCheckAccountingEvent decoded = this.codec.decodeFromString(encoded);
        assertThat(decoded, is(notNullValue()));
        assertThat(decoded, is(equalTo(original)));
    }
    
    @Test
    public void testEncodeProcessResultAccountingEventToString()
    {
        ProcessResultAccountingEvent original = new ProcessResultAccountingEvent(siteId, execId, ResultType.ACTIVE);
        String encoded = this.codec.encodeToString(original);
        assertThat(encoded, is(notNullValue()));
        ProcessResultAccountingEvent decoded = this.codec.decodeFromString(encoded);
        assertThat(decoded, is(notNullValue()));
        assertThat(decoded, is(equalTo(original)));
    }
}

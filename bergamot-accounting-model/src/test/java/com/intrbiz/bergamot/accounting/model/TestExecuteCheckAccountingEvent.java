package com.intrbiz.bergamot.accounting.model;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.Test;

public class TestExecuteCheckAccountingEvent
{
    private static UUID siteId = UUID.fromString("01cf7f8e-2da3-4b5b-8764-a8cb0e1e8e6b");
    private static UUID execId = UUID.fromString("7c5efc47-8cd4-475c-817c-fba6acf291c6");
    private static UUID checkId = UUID.fromString("3640d25d-547d-40ab-8eb6-fa97155e9dbb");
    
    
    @Test
    public void hasTypeId()
    {
        assertThat(new ExecuteCheckAccountingEvent().getTypeId(), is(notNullValue()));
    }
    
    @Test
    public void checkTypeId()
    {
        assertThat(new ExecuteCheckAccountingEvent().getTypeId(), is(equalTo(UUID.fromString("ab52d8b1-b43f-4dd7-9b9b-9bde92da1fde"))));
    }

    @Test
    public void packUnpack()
    {
        ExecuteCheckAccountingEvent a = new ExecuteCheckAccountingEvent(siteId, execId, checkId, "nrpe", "nrpe", "check_load");
        assertThat(a, is(notNullValue()));
        assertThat(a.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(a.getSiteId(), is(notNullValue()));
        assertThat(a.getExecutionId(), is(notNullValue()));
        assertThat(a.getCheckId(), is(notNullValue()));
        assertThat(a.getEngine(), is(notNullValue()));
        assertThat(a.getCommand(), is(notNullValue()));
        // pack
        ByteBuffer buf = ByteBuffer.allocate(8192);
        a.pack(buf);
        // unpack
        buf.flip();
        ExecuteCheckAccountingEvent b = new ExecuteCheckAccountingEvent();
        b.unpack(buf);
        assertThat(b, is(notNullValue()));
        assertThat(b.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(b.getSiteId(), is(notNullValue()));
        assertThat(b.getExecutionId(), is(notNullValue()));
        assertThat(b.getCheckId(), is(notNullValue()));
        assertThat(b.getEngine(), is(notNullValue()));
        assertThat(b.getCommand(), is(notNullValue()));
        // compare
        assertThat(a, is(equalTo(b)));
        assertThat(a.getTypeId(), is(equalTo(b.getTypeId())));
        assertThat(a.getTimestamp(), is(equalTo(b.getTimestamp())));
        assertThat(a.getSiteId(), is(equalTo(b.getSiteId())));
        assertThat(a.getExecutionId(), is(equalTo(b.getExecutionId())));
        assertThat(a.getCheckId(), is(equalTo(b.getCheckId())));
        assertThat(a.getEngine(), is(equalTo(b.getEngine())));
        assertThat(a.getCommand(), is(equalTo(b.getCommand())));
    }
    
    @Test
    public void packUnpackWithNulls()
    {
        ExecuteCheckAccountingEvent a = new ExecuteCheckAccountingEvent(null, null, null, null, null, null);
        assertThat(a, is(notNullValue()));
        assertThat(a.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(a.getSiteId(), is(nullValue()));
        assertThat(a.getExecutionId(), is(nullValue()));
        assertThat(a.getCheckId(), is(nullValue()));
        assertThat(a.getEngine(), is(nullValue()));
        assertThat(a.getCommand(), is(nullValue()));
        // pack
        ByteBuffer buf = ByteBuffer.allocate(8192);
        a.pack(buf);
        // unpack
        buf.flip();
        ExecuteCheckAccountingEvent b = new ExecuteCheckAccountingEvent();
        b.unpack(buf);
        assertThat(b, is(notNullValue()));
        assertThat(b.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(b.getSiteId(), is(nullValue()));
        assertThat(b.getExecutionId(), is(nullValue()));
        assertThat(b.getCheckId(), is(nullValue()));
        assertThat(b.getEngine(), is(nullValue()));
        assertThat(b.getCommand(), is(nullValue()));
        // compare
        assertThat(a, is(equalTo(b)));
        assertThat(a.getTypeId(), is(equalTo(b.getTypeId())));
        assertThat(a.getTimestamp(), is(equalTo(b.getTimestamp())));
        assertThat(a.getSiteId(), is(equalTo(b.getSiteId())));
        assertThat(a.getExecutionId(), is(equalTo(b.getExecutionId())));
        assertThat(a.getCheckId(), is(equalTo(b.getCheckId())));
        assertThat(a.getEngine(), is(equalTo(b.getEngine())));
        assertThat(a.getCommand(), is(equalTo(b.getCommand())));
    }
}

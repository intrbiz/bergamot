package com.intrbiz.bergamot.accounting.model;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.Test;

public class TestSignAgentAccountingEvent
{
    private static UUID siteId = UUID.fromString("01cf7f8e-2da3-4b5b-8764-a8cb0e1e8e6b");
    private static UUID agentId = UUID.fromString("7d3a4022-d76e-4a0a-b719-d18ea36d2aae");
    private static UUID contactId = UUID.fromString("8b0d6ed1-e118-4179-9d87-2c07e0c18776");
    
    @Test
    public void hasTypeId()
    {
        assertThat(new SignAgentAccountingEvent().getTypeId(), is(notNullValue()));
    }
    
    @Test
    public void checkTypeId()
    {
        assertThat(new SignAgentAccountingEvent().getTypeId(), is(equalTo(UUID.fromString("fdbc3042-d4a4-45bb-a54f-0ae8da9c6d44"))));
    }

    @Test
    public void packUnpack()
    {
        SignAgentAccountingEvent a = new SignAgentAccountingEvent(siteId, agentId, "test.local", "AF453BE3533523", contactId);
        assertThat(a, is(notNullValue()));
        assertThat(a.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(a.getSiteId(), is(notNullValue()));
        assertThat(a.getAgentId(), is(notNullValue()));
        assertThat(a.getCommonName(), is(notNullValue()));
        assertThat(a.getSerial(), is(notNullValue()));
        assertThat(a.getContact(), is(notNullValue()));
        // pack
        ByteBuffer buf = ByteBuffer.allocate(8192);
        a.pack(buf);
        // unpack
        buf.flip();
        SignAgentAccountingEvent b = new SignAgentAccountingEvent();
        b.unpack(buf);
        assertThat(b, is(notNullValue()));
        assertThat(b.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(b.getSiteId(), is(notNullValue()));
        assertThat(b.getAgentId(), is(notNullValue()));
        assertThat(b.getCommonName(), is(notNullValue()));
        assertThat(b.getSerial(), is(notNullValue()));
        assertThat(b.getContact(), is(notNullValue()));
        // compare
        assertThat(a, is(equalTo(b)));
        assertThat(a.getTypeId(), is(equalTo(b.getTypeId())));
        assertThat(a.getTimestamp(), is(equalTo(b.getTimestamp())));
        assertThat(a.getSiteId(), is(equalTo(b.getSiteId())));
        assertThat(a.getAgentId(), is(equalTo(b.getAgentId())));
        assertThat(a.getCommonName(), is(equalTo(b.getCommonName())));
        assertThat(a.getSerial(), is(equalTo(b.getSerial())));
        assertThat(b.getContact(), is(equalTo(b.getContact())));
    }
    
    @Test
    public void packUnpackWithNulls()
    {
        SignAgentAccountingEvent a = new SignAgentAccountingEvent(null, null, null, null, null);
        assertThat(a, is(notNullValue()));
        assertThat(a.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(a.getSiteId(), is(nullValue()));
        assertThat(a.getAgentId(), is(nullValue()));
        assertThat(a.getCommonName(), is(nullValue()));
        assertThat(a.getSerial(), is(nullValue()));
        assertThat(a.getContact(), is(nullValue()));
        // pack
        ByteBuffer buf = ByteBuffer.allocate(8192);
        a.pack(buf);
        // unpack
        buf.flip();
        SignAgentAccountingEvent b = new SignAgentAccountingEvent();
        b.unpack(buf);
        assertThat(b, is(notNullValue()));
        assertThat(b.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(b.getSiteId(), is(nullValue()));
        assertThat(b.getAgentId(), is(nullValue()));
        assertThat(b.getCommonName(), is(nullValue()));
        assertThat(b.getSerial(), is(nullValue()));
        assertThat(b.getContact(), is(nullValue()));
        // compare
        assertThat(a, is(equalTo(b)));
        assertThat(a.getTypeId(), is(equalTo(b.getTypeId())));
        assertThat(a.getTimestamp(), is(equalTo(b.getTimestamp())));
        assertThat(a.getSiteId(), is(equalTo(b.getSiteId())));
        assertThat(a.getAgentId(), is(equalTo(b.getAgentId())));
        assertThat(a.getCommonName(), is(equalTo(b.getCommonName())));
        assertThat(a.getSerial(), is(equalTo(b.getSerial())));
        assertThat(b.getContact(), is(equalTo(b.getContact())));
    }
}

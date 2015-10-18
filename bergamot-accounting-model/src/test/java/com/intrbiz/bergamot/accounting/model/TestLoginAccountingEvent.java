package com.intrbiz.bergamot.accounting.model;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.Test;

public class TestLoginAccountingEvent
{
    private static UUID siteId = UUID.fromString("01cf7f8e-2da3-4b5b-8764-a8cb0e1e8e6b");
    private static UUID contactId = UUID.fromString("c704b5f8-03c1-41d1-a1e1-0d8c677225ec");
    
    
    @Test
    public void hasTypeId()
    {
        assertThat(new LoginAccountingEvent().getTypeId(), is(notNullValue()));
    }
    
    @Test
    public void checkTypeId()
    {
        assertThat(new LoginAccountingEvent().getTypeId(), is(equalTo(UUID.fromString("c3f43c54-e8a0-45ce-8213-fa71221ae5fc"))));
    }

    @Test
    public void packUnpack()
    {
        LoginAccountingEvent a = new LoginAccountingEvent(siteId, contactId, "bergamot.local", "admin", "balsad52097987906", true, true, "127.0.0.1");
        assertThat(a, is(notNullValue()));
        assertThat(a.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(a.getSiteId(), is(notNullValue()));
        assertThat(a.getContactId(), is(notNullValue()));
        assertThat(a.getHost(), is(notNullValue()));
        assertThat(a.getUsername(), is(notNullValue()));
        assertThat(a.getSessionId(), is(notNullValue()));
        assertThat(a.isAutoLogin(), is(equalTo(true)));
        assertThat(a.isSuccess(), is(equalTo(true)));
        assertThat(a.getRemoteAddress(), is(notNullValue()));
        // pack
        ByteBuffer buf = ByteBuffer.allocate(8192);
        a.pack(buf);
        // unpack
        buf.flip();
        LoginAccountingEvent b = new LoginAccountingEvent();
        b.unpack(buf);
        assertThat(b, is(notNullValue()));
        assertThat(b.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(b.getSiteId(), is(notNullValue()));
        assertThat(b.getContactId(), is(notNullValue()));
        assertThat(b.getHost(), is(notNullValue()));
        assertThat(b.getUsername(), is(notNullValue()));
        assertThat(b.getSessionId(), is(notNullValue()));
        assertThat(b.isAutoLogin(), is(equalTo(true)));
        assertThat(b.isSuccess(), is(equalTo(true)));
        assertThat(b.getRemoteAddress(), is(notNullValue()));
        // compare
        assertThat(b, is(equalTo(b)));
        assertThat(b.getTimestamp(), is(equalTo(a.getTimestamp())));
        assertThat(b.getSiteId(), is(equalTo(a.getSiteId())));
        assertThat(b.getContactId(), is(equalTo(b.getContactId())));
        assertThat(b.getHost(), is(equalTo(b.getHost())));
        assertThat(b.getUsername(), is(equalTo(b.getUsername())));
        assertThat(b.getSessionId(), is(equalTo(b.getSessionId())));
        assertThat(b.isAutoLogin(), is(equalTo(a.isAutoLogin())));
        assertThat(b.isSuccess(), is(equalTo(a.isSuccess())));
        assertThat(b.getRemoteAddress(), is(equalTo(a.getRemoteAddress())));
    }
    
    @Test
    public void packUnpackWithNulls()
    {
        LoginAccountingEvent a = new LoginAccountingEvent(null, null, null, null, null, false, false, null);
        assertThat(a, is(notNullValue()));
        assertThat(a.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(a.getSiteId(), is(nullValue()));
        assertThat(a.getContactId(), is(nullValue()));
        assertThat(a.getHost(), is(nullValue()));
        assertThat(a.getUsername(), is(nullValue()));
        assertThat(a.getSessionId(), is(nullValue()));
        assertThat(a.isAutoLogin(), is(equalTo(false)));
        assertThat(a.isSuccess(), is(equalTo(false)));
        assertThat(a.getRemoteAddress(), is(nullValue()));
        // pack
        ByteBuffer buf = ByteBuffer.allocate(8192);
        a.pack(buf);
        // unpack
        buf.flip();
        LoginAccountingEvent b = new LoginAccountingEvent();
        b.unpack(buf);
        assertThat(b, is(notNullValue()));
        assertThat(b.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(b.getSiteId(), is(nullValue()));
        assertThat(b.getContactId(), is(nullValue()));
        assertThat(b.getHost(), is(nullValue()));
        assertThat(b.getUsername(), is(nullValue()));
        assertThat(b.getSessionId(), is(nullValue()));
        assertThat(b.isAutoLogin(), is(equalTo(false)));
        assertThat(b.isSuccess(), is(equalTo(false)));
        assertThat(b.getRemoteAddress(), is(nullValue()));
        // compare
        assertThat(b, is(equalTo(b)));
        assertThat(b.getTimestamp(), is(equalTo(a.getTimestamp())));
        assertThat(b.getSiteId(), is(equalTo(a.getSiteId())));
        assertThat(b.getContactId(), is(equalTo(b.getContactId())));
        assertThat(b.getHost(), is(equalTo(b.getHost())));
        assertThat(b.getUsername(), is(equalTo(b.getUsername())));
        assertThat(b.getSessionId(), is(equalTo(b.getSessionId())));
        assertThat(b.isAutoLogin(), is(equalTo(a.isAutoLogin())));
        assertThat(b.isSuccess(), is(equalTo(a.isSuccess())));
        assertThat(b.getRemoteAddress(), is(equalTo(a.getRemoteAddress())));
    }
}

package com.intrbiz.bergamot.accounting.model;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.Test;

import com.intrbiz.bergamot.accounting.model.SendNotificationAccountingEvent.NotificationType;

public class TestSendNotificationAccountingEvent
{
    private static UUID siteId = UUID.fromString("01cf7f8e-2da3-4b5b-8764-a8cb0e1e8e6b");
    private static UUID alertId = UUID.fromString("e6fa47ea-f435-4607-b0d5-d128fe259742");
    private static UUID checkId = UUID.fromString("3640d25d-547d-40ab-8eb6-fa97155e9dbb");
    
    
    @Test
    public void hasTypeId()
    {
        assertThat(new SendNotificationAccountingEvent().getTypeId(), is(notNullValue()));
    }
    
    @Test
    public void checkTypeId()
    {
        assertThat(new SendNotificationAccountingEvent().getTypeId(), is(equalTo(UUID.fromString("fcafd43f-1b35-43a3-96db-8094f48c664a"))));
    }

    @Test
    public void packUnpack()
    {
        SendNotificationAccountingEvent a = new SendNotificationAccountingEvent(siteId, alertId, checkId, NotificationType.ALERT, 13);
        assertThat(a, is(notNullValue()));
        assertThat(a.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(a.getSiteId(), is(notNullValue()));
        assertThat(a.getNotificationId(), is(notNullValue()));
        assertThat(a.getObjectId(), is(notNullValue()));
        assertThat(a.getNotificationType(), is(notNullValue()));
        assertThat(a.getRecipientCount(), is(equalTo(13)));
        // pack
        ByteBuffer buf = ByteBuffer.allocate(8192);
        a.pack(buf);
        // unpack
        buf.flip();
        SendNotificationAccountingEvent b = new SendNotificationAccountingEvent();
        b.unpack(buf);
        assertThat(b, is(notNullValue()));
        assertThat(b.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(b.getSiteId(), is(notNullValue()));
        assertThat(b.getNotificationId(), is(notNullValue()));
        assertThat(b.getObjectId(), is(notNullValue()));
        assertThat(b.getNotificationType(), is(notNullValue()));
        assertThat(b.getRecipientCount(), is(equalTo(13)));
        // compare
        assertThat(a, is(equalTo(b)));
        assertThat(a.getTypeId(), is(equalTo(b.getTypeId())));
        assertThat(a.getTimestamp(), is(equalTo(b.getTimestamp())));
        assertThat(a.getSiteId(), is(equalTo(b.getSiteId())));
        assertThat(a.getNotificationId(), is(equalTo(b.getNotificationId())));
        assertThat(a.getObjectId(), is(equalTo(b.getObjectId())));
        assertThat(a.getNotificationType(), is(equalTo(b.getNotificationType())));
        assertThat(b.getRecipientCount(), is(equalTo(b.getRecipientCount())));
    }
    
    @Test
    public void packUnpackWithNulls()
    {
        SendNotificationAccountingEvent a = new SendNotificationAccountingEvent(null, null, null, null, -1);
        assertThat(a, is(notNullValue()));
        assertThat(a.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(a.getSiteId(), is(nullValue()));
        assertThat(a.getNotificationId(), is(nullValue()));
        assertThat(a.getObjectId(), is(nullValue()));
        assertThat(a.getNotificationType(), is(nullValue()));
        assertThat(a.getRecipientCount(), is(equalTo(-1)));
        // pack
        ByteBuffer buf = ByteBuffer.allocate(8192);
        a.pack(buf);
        // unpack
        buf.flip();
        SendNotificationAccountingEvent b = new SendNotificationAccountingEvent();
        b.unpack(buf);
        assertThat(b, is(notNullValue()));
        assertThat(b.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(b.getSiteId(), is(nullValue()));
        assertThat(b.getNotificationId(), is(nullValue()));
        assertThat(b.getObjectId(), is(nullValue()));
        assertThat(b.getNotificationType(), is(nullValue()));
        assertThat(a.getRecipientCount(), is(equalTo(-1)));
        // compare
        assertThat(a, is(equalTo(b)));
        assertThat(a.getTypeId(), is(equalTo(b.getTypeId())));
        assertThat(a.getTimestamp(), is(equalTo(b.getTimestamp())));
        assertThat(a.getSiteId(), is(equalTo(b.getSiteId())));
        assertThat(a.getNotificationId(), is(equalTo(b.getNotificationId())));
        assertThat(a.getObjectId(), is(equalTo(b.getObjectId())));
        assertThat(a.getNotificationType(), is(equalTo(b.getNotificationType())));
        assertThat(a.getRecipientCount(), is(equalTo(b.getRecipientCount())));
    }
}

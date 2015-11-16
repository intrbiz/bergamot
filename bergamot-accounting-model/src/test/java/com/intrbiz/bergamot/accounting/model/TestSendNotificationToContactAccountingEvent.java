package com.intrbiz.bergamot.accounting.model;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.Test;

public class TestSendNotificationToContactAccountingEvent
{
    private static UUID siteId = UUID.fromString("01cf7f8e-2da3-4b5b-8764-a8cb0e1e8e6b");
    private static UUID alertId = UUID.fromString("e6fa47ea-f435-4607-b0d5-d128fe259742");
    private static UUID checkId = UUID.fromString("3640d25d-547d-40ab-8eb6-fa97155e9dbb");
    private static UUID contactId = UUID.fromString("8b0d6ed1-e118-4179-9d87-2c07e0c18776");
    
    @Test
    public void hasTypeId()
    {
        assertThat(new SendNotificationToContactAccountingEvent().getTypeId(), is(notNullValue()));
    }
    
    @Test
    public void checkTypeId()
    {
        assertThat(new SendNotificationToContactAccountingEvent().getTypeId(), is(equalTo(UUID.fromString("01fb5dd1-e69f-403c-8847-c3a3f572f8d8"))));
    }

    @Test
    public void packUnpack()
    {
        SendNotificationToContactAccountingEvent a = new SendNotificationToContactAccountingEvent(siteId, alertId, checkId, AccountingNotificationType.ALERT, contactId, "email", "email", "test@localhost", "1234");
        assertThat(a, is(notNullValue()));
        assertThat(a.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(a.getSiteId(), is(notNullValue()));
        assertThat(a.getNotificationId(), is(notNullValue()));
        assertThat(a.getObjectId(), is(notNullValue()));
        assertThat(a.getNotificationType(), is(notNullValue()));
        assertThat(a.getContact(), is(notNullValue()));
        assertThat(a.getEngine(), is(notNullValue()));
        assertThat(a.getMessageType(), is(notNullValue()));
        assertThat(a.getMessageAddress(), is(notNullValue()));
        assertThat(a.getMessageId(), is(notNullValue()));
        // pack
        ByteBuffer buf = ByteBuffer.allocate(8192);
        a.pack(buf);
        // unpack
        buf.flip();
        SendNotificationToContactAccountingEvent b = new SendNotificationToContactAccountingEvent();
        b.unpack(buf);
        assertThat(b, is(notNullValue()));
        assertThat(b.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(b.getSiteId(), is(notNullValue()));
        assertThat(b.getNotificationId(), is(notNullValue()));
        assertThat(b.getObjectId(), is(notNullValue()));
        assertThat(b.getNotificationType(), is(notNullValue()));
        assertThat(b.getContact(), is(notNullValue()));
        assertThat(b.getEngine(), is(notNullValue()));
        assertThat(b.getMessageType(), is(notNullValue()));
        assertThat(b.getMessageAddress(), is(notNullValue()));
        assertThat(b.getMessageId(), is(notNullValue()));
        // compare
        assertThat(a, is(equalTo(b)));
        assertThat(a.getTypeId(), is(equalTo(b.getTypeId())));
        assertThat(a.getTimestamp(), is(equalTo(b.getTimestamp())));
        assertThat(a.getSiteId(), is(equalTo(b.getSiteId())));
        assertThat(a.getNotificationId(), is(equalTo(b.getNotificationId())));
        assertThat(a.getObjectId(), is(equalTo(b.getObjectId())));
        assertThat(a.getNotificationType(), is(equalTo(b.getNotificationType())));
        assertThat(b.getContact(), is(equalTo(b.getContact())));
        assertThat(b.getEngine(), is(equalTo(b.getEngine())));
        assertThat(b.getMessageType(), is(equalTo(b.getMessageType())));
        assertThat(b.getMessageAddress(), is(equalTo(b.getMessageAddress())));
        assertThat(b.getMessageId(), is(equalTo(b.getMessageId())));
    }
    
    @Test
    public void packUnpackWithNulls()
    {
        SendNotificationToContactAccountingEvent a = new SendNotificationToContactAccountingEvent(null, null, null, null, null, null, null, null, null);
        assertThat(a, is(notNullValue()));
        assertThat(a.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(a.getSiteId(), is(nullValue()));
        assertThat(a.getNotificationId(), is(nullValue()));
        assertThat(a.getObjectId(), is(nullValue()));
        assertThat(a.getNotificationType(), is(nullValue()));
        assertThat(a.getContact(), is(nullValue()));
        assertThat(a.getEngine(), is(nullValue()));
        assertThat(a.getMessageType(), is(nullValue()));
        assertThat(a.getMessageAddress(), is(nullValue()));
        assertThat(a.getMessageId(), is(nullValue()));
        // pack
        ByteBuffer buf = ByteBuffer.allocate(8192);
        a.pack(buf);
        // unpack
        buf.flip();
        SendNotificationToContactAccountingEvent b = new SendNotificationToContactAccountingEvent();
        b.unpack(buf);
        assertThat(b, is(notNullValue()));
        assertThat(b.getTimestamp(), is(not(equalTo(-1L))));
        assertThat(b.getSiteId(), is(nullValue()));
        assertThat(b.getNotificationId(), is(nullValue()));
        assertThat(b.getObjectId(), is(nullValue()));
        assertThat(b.getNotificationType(), is(nullValue()));
        assertThat(b.getContact(), is(nullValue()));
        assertThat(b.getEngine(), is(nullValue()));
        assertThat(b.getMessageType(), is(nullValue()));
        assertThat(b.getMessageAddress(), is(nullValue()));
        assertThat(b.getMessageId(), is(nullValue()));
        // compare
        assertThat(a, is(equalTo(b)));
        assertThat(a.getTypeId(), is(equalTo(b.getTypeId())));
        assertThat(a.getTimestamp(), is(equalTo(b.getTimestamp())));
        assertThat(a.getSiteId(), is(equalTo(b.getSiteId())));
        assertThat(a.getNotificationId(), is(equalTo(b.getNotificationId())));
        assertThat(a.getObjectId(), is(equalTo(b.getObjectId())));
        assertThat(a.getNotificationType(), is(equalTo(b.getNotificationType())));
        assertThat(b.getContact(), is(equalTo(b.getContact())));
        assertThat(b.getEngine(), is(equalTo(b.getEngine())));
        assertThat(b.getMessageType(), is(equalTo(b.getMessageType())));
        assertThat(b.getMessageAddress(), is(equalTo(b.getMessageAddress())));
        assertThat(b.getMessageId(), is(equalTo(b.getMessageId())));
    }
}

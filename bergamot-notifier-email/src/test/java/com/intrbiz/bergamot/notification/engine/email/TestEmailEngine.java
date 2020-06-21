package com.intrbiz.bergamot.notification.engine.email;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import javax.mail.Message;

import org.junit.Before;
import org.junit.Test;

import com.intrbiz.bergamot.io.BergamotCoreTranscoder;
import com.intrbiz.bergamot.model.message.notification.PasswordResetNotification;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.model.message.notification.SendUpdate;
import com.intrbiz.bergamot.notification.NotificationEngineContext;

public class TestEmailEngine extends EmailEngine
{
    private EmailEngine engine;
    
    @Before
    public void setupEmailEngine() throws Exception
    {
        this.engine = new TestEmailEngine();
        this.engine.prepare(new NotificationEngineContext() {
        });
    }
    
    private <T> T loadMessage(Class<T> type, String name)
    {
        BergamotCoreTranscoder transcoder = new BergamotCoreTranscoder();
        return transcoder.decode(Thread.currentThread().getContextClassLoader().getResourceAsStream(name), type);
    }
    
    @Test
    public void testServiceRecoveryTemplate()
    {
        SendRecovery message = this.loadMessage(SendRecovery.class, "recovery/service.json");
        // build the subhect
        String subject = this.engine.applyTemplate("service.recovery.subject", message);
        assertThat(subject, is(notNullValue()));
        // build the content
        String content = this.engine.applyTemplate("service.recovery.content", message);
        assertThat(content, is(notNullValue()));
    }
    
    @Test
    public void testServiceAlertTemplate()
    {
        SendAlert message = this.loadMessage(SendAlert.class, "alert/service.json");
        // build the subhect
        String subject = this.engine.applyTemplate("service.alert.subject", message);
        assertThat(subject, is(notNullValue()));
        // build the content
        String content = this.engine.applyTemplate("service.alert.content", message);
        System.out.println("Content: " + content);
        assertThat(content, is(notNullValue()));
    }
    
    @Test
    public void testPasswordResetemplate()
    {
        PasswordResetNotification message = this.loadMessage(PasswordResetNotification.class, "generic/password_reset.json");
        // build the subhect
        String subject = this.engine.applyTemplate("password_reset.subject", message);
        assertThat(subject, is(notNullValue()));
        // build the content
        String content = this.engine.applyTemplate("password_reset.content", message);
        assertThat(content, is(notNullValue()));
    }
    
    @Test
    public void testServiceUpdateTemplate()
    {
        SendUpdate message = this.loadMessage(SendUpdate.class, "update/service.json");
        // build the subhect
        String subject = this.engine.applyTemplate("service.update.subject", message);
        assertThat(subject, is(notNullValue()));
        // build the content
        String content = this.engine.applyTemplate("service.update.content", message);
        System.out.println("Content: " + content);
        assertThat(content, is(notNullValue()));
    }
    
    @Test
    public void testServiceUpdateBuildMessage() throws Exception
    {
        SendUpdate message = this.loadMessage(SendUpdate.class, "update/service.json");
        // build the message
        Message m = this.engine.buildMessage(message);
        assertThat(m, is(notNullValue()));
        assertThat(m.getContent(), is(notNullValue()));
        assertThat(m.getAllHeaders(), is(notNullValue()));
        assertThat(m.getHeader("Message-ID")[0], is(equalTo("<a9f82618-9801-4b91-8bed-9c8c01f883ad.update.bergamot>")));
    }
}

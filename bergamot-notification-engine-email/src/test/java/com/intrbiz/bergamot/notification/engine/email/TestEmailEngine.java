package com.intrbiz.bergamot.notification.engine.email;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.intrbiz.bergamot.config.NotificationEngineCfg;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.notification.PasswordResetNotification;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.configuration.CfgParameter;

public class TestEmailEngine extends EmailEngine
{
    private EmailEngine engine;
    
    @Before
    public void setupEmailEngine() throws Exception
    {
        NotificationEngineCfg cfg = new NotificationEngineCfg(
                EmailEngine.class, 
                new CfgParameter("mail.host", "127.0.0.1"), 
                new CfgParameter("mail.user", ""), 
                new CfgParameter("mail.password", ""), 
                new CfgParameter("from", "bergamot@localhost")
        );
        this.engine = new TestEmailEngine();
        this.engine.configure(cfg);
    }
    
    private <T> T loadMessage(Class<T> type, String name)
    {
        BergamotTranscoder transcoder = new BergamotTranscoder();
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
}

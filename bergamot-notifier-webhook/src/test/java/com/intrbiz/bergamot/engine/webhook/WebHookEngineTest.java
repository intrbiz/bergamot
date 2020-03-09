package com.intrbiz.bergamot.engine.webhook;

import java.util.UUID;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.notification.NotificationEngineContext;
import com.intrbiz.bergamot.notification.engine.webhook.WebHookEngine;
import com.intrbiz.configuration.Configuration;

public class WebHookEngineTest
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        WebHookEngine webhook = new WebHookEngine();
        webhook.prepare(new NotificationEngineContext() {
            @Override
            public Configuration getConfiguration()
            {
                return new Configuration();
            }
        });
        // our dummy alert
        HostMO host = new HostMO();
        host.setId(UUID.randomUUID());
        host.setName("test");
        host.addParameter("webhook.url", "http://127.0.0.1/bergamot/webhook/test");
        SendAlert alert = new SendAlert();
        alert.setCheck(host);
        alert.setAlertId(UUID.randomUUID());
        alert.setRaised(System.currentTimeMillis());
        // send
        webhook.sendNotification(alert);
    }
}

package com.intrbiz.bergamot.notification.engine.sms;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.notification.NotificationEngineContext;
import com.intrbiz.configuration.CfgParameter;
import com.intrbiz.configuration.Configuration;

public class SMSEngineTests
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        SMSEngine sms = new SMSEngine();
        Configuration cfg = new Configuration();
        cfg.addParameter(new CfgParameter("sms.from", "", System.getProperty("from")));
        cfg.addParameter(new CfgParameter("sms.transport", "", System.getProperty("transport")));
        cfg.addParameter(new CfgParameter("twilio.account", "", System.getProperty("twilio.account")));
        cfg.addParameter(new CfgParameter("twilio.token", "", System.getProperty("twilio.token")));
        cfg.addParameter(new CfgParameter("aws.accessKeyId", "", System.getProperty("aws.accessKeyId")));
        cfg.addParameter(new CfgParameter("aws.secretKey", "", System.getProperty("aws.secretKey")));
        sms.prepare(new NotificationEngineContext() {
            @Override
            public Configuration getConfiguration()
            {
                return cfg;
            }
        });
        // our to test number
        String to = System.getProperty("to");
        // our dummy alert
        SendAlert alert = forceToNumber(to, loadMessage(SendAlert.class, "alert/service.json"));
        alert.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        sms.sendNotification(alert);
        // our dummy recovery
        SendRecovery recovery = forceToNumber(to, loadMessage(SendRecovery.class, "recovery/service.json"));
        recovery.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        sms.sendNotification(recovery);
        // our dummy alert
        alert = forceToNumber(to, loadMessage(SendAlert.class, "alert/host.json"));
        alert.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        sms.sendNotification(alert);
        // our dummy recovery
        recovery = forceToNumber(to, loadMessage(SendRecovery.class, "recovery/host.json"));
        recovery.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        sms.sendNotification(recovery);
        // our dummy alert
        alert = forceToNumber(to, loadMessage(SendAlert.class, "alert/cluster.json"));
        alert.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        sms.sendNotification(alert);
        // our dummy recovery
        recovery = forceToNumber(to, loadMessage(SendRecovery.class, "recovery/cluster.json"));
        recovery.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        sms.sendNotification(recovery);
        // exit
        Thread.sleep(1000);
        System.exit(0);
    }
    
    private static <T extends CheckNotification> T forceToNumber(String to, T notification)
    {
        for (ContactMO contact : notification.getTo())
        {
            contact.setPager(to);
            contact.setMobile(to);
        }
        return notification;
    }
    
    private static <T> T loadMessage(Class<T> type, String name)
    {
        BergamotTranscoder transcoder = new BergamotTranscoder();
        return transcoder.decode(Thread.currentThread().getContextClassLoader().getResourceAsStream(name), type);
    }
}

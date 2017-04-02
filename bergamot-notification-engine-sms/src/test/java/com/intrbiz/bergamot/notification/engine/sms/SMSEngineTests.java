package com.intrbiz.bergamot.notification.engine.sms;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.NotificationEngineCfg;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.configuration.CfgParameter;

public class SMSEngineTests
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        SMSEngine slack = new SMSEngine();
        NotificationEngineCfg cfg = new NotificationEngineCfg();
        cfg.addParameter(new CfgParameter("from", "", System.getProperty("from")));
        cfg.addParameter(new CfgParameter("transport", "", System.getProperty("transport")));
        cfg.addParameter(new CfgParameter("twilio.account", "", System.getProperty("twilio.account")));
        cfg.addParameter(new CfgParameter("twilio.token", "", System.getProperty("twilio.token")));
        cfg.addParameter(new CfgParameter("aws.accessKeyId", "", System.getProperty("aws.accessKeyId")));
        cfg.addParameter(new CfgParameter("aws.secretKey", "", System.getProperty("aws.secretKey")));
        slack.configure(cfg);
        // our to test number
        String to = System.getProperty("to");
        // our dummy alert
        SendAlert alert = forceToNumber(to, loadMessage(SendAlert.class, "alert/service.json"));
        alert.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        slack.sendNotification(alert);
        // our dummy recovery
        SendRecovery recovery = forceToNumber(to, loadMessage(SendRecovery.class, "recovery/service.json"));
        recovery.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        slack.sendNotification(recovery);
        // our dummy alert
        alert = forceToNumber(to, loadMessage(SendAlert.class, "alert/host.json"));
        alert.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        slack.sendNotification(alert);
        // our dummy recovery
        recovery = forceToNumber(to, loadMessage(SendRecovery.class, "recovery/host.json"));
        recovery.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        slack.sendNotification(recovery);
        // our dummy alert
        alert = forceToNumber(to, loadMessage(SendAlert.class, "alert/cluster.json"));
        alert.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        slack.sendNotification(alert);
        // our dummy recovery
        recovery = forceToNumber(to, loadMessage(SendRecovery.class, "recovery/cluster.json"));
        recovery.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        slack.sendNotification(recovery);
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

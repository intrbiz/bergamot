package com.intrbiz.bergamot.engine.slack;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.io.BergamotCoreTranscoder;
import com.intrbiz.bergamot.model.message.notification.SendAlert;
import com.intrbiz.bergamot.model.message.notification.SendRecovery;
import com.intrbiz.bergamot.notification.NotificationEngineContext;
import com.intrbiz.bergamot.notification.engine.slack.SlackEngine;

public class SlackEngineTest
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        SlackEngine slack = new SlackEngine();
        slack.prepare(new NotificationEngineContext() {
        });
        // our dummy alert
        SendAlert alert;
        SendRecovery recovery;
        alert = loadMessage(SendAlert.class, "alert/service.json");
        alert.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        slack.sendNotification(alert);
        // our dummy recovery
        recovery = loadMessage(SendRecovery.class, "recovery/service.json");
        recovery.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        slack.sendNotification(recovery);
        // our dummy alert
        alert = loadMessage(SendAlert.class, "alert/host.json");
        alert.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        slack.sendNotification(alert);
        // our dummy recovery
        recovery = loadMessage(SendRecovery.class, "recovery/host.json");
        recovery.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        slack.sendNotification(recovery);
        // our dummy alert
        alert = loadMessage(SendAlert.class, "alert/cluster.json");
        alert.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        slack.sendNotification(alert);
        // our dummy recovery
        recovery = loadMessage(SendRecovery.class, "recovery/cluster.json");
        recovery.getTo().get(0).getTeams().get(0).addParameter("slack.url", System.getProperty("slack.url"));
        slack.sendNotification(recovery);
        // exit
        Thread.sleep(1000);
        System.exit(0);
    }
    
    private static <T> T loadMessage(Class<T> type, String name)
    {
        BergamotCoreTranscoder transcoder = new BergamotCoreTranscoder();
        return transcoder.decode(Thread.currentThread().getContextClassLoader().getResourceAsStream(name), type);
    }
}

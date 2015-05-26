package com.intrbiz.bergamot.notification.engine.webhook;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.notification.AbstractNotificationEngine;

public class WebHookEngine extends AbstractNotificationEngine
{
    public static final String NAME = "webhook";

    private Logger logger = Logger.getLogger(WebHookEngine.class);

    public WebHookEngine()
    {
        super(NAME);
    }

    @Override
    protected void configure() throws Exception
    {
        super.configure();
        logger.info("WebHook notifier configured");
    }

    @Override
    public void sendNotification(Notification notification)
    {
    }
}

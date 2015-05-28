package com.intrbiz.bergamot.notification.engine.webhook;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.check.http.HTTPChecker;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.notification.AbstractNotificationEngine;

public class WebHookEngine extends AbstractNotificationEngine
{
    public static final String NAME = "webhook";

    private Logger logger = Logger.getLogger(WebHookEngine.class);
    
    private HTTPChecker checker;

    public WebHookEngine()
    {
        super(NAME);
    }

    @Override
    protected void configure() throws Exception
    {
        super.configure();
        logger.info("WebHook notifier configured");
        // setup our the checker, which we will repurpose as a simple wrapper to make a HTTP request
        this.checker = new HTTPChecker(Runtime.getRuntime().availableProcessors() + 2);
    }
    
    public HTTPChecker getChecker()
    {
        return this.checker;
    }

    @Override
    public void sendNotification(Notification notification)
    {
    }
}

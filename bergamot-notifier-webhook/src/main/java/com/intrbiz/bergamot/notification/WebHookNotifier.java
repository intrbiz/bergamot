package com.intrbiz.bergamot.notification;

import com.intrbiz.bergamot.notification.config.WebHookNotifierCfg;

public class WebHookNotifier extends DefaultNotifier
{
    public WebHookNotifier()
    {
        super(WebHookNotifierCfg.class, "/etc/bergamot/notifier/webhook.xml", "webhook");
    }
    
    public static void main(String[] args) throws Exception
    {
        Notifier notifier = new WebHookNotifier();
        notifier.start();
    }
}

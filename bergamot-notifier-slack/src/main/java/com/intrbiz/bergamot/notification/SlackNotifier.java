package com.intrbiz.bergamot.notification;

import com.intrbiz.bergamot.notification.config.SlackNotifierCfg;

public class SlackNotifier extends DefaultNotifier
{
    public SlackNotifier()
    {
        super(SlackNotifierCfg.class, "/etc/bergamot/notifier/slack.xml", "slack");
    }
    
    public static void main(String[] args) throws Exception
    {
        Notifier notifier = new SlackNotifier();
        notifier.start();
    }
}

package com.intrbiz.bergamot.notification;

import com.intrbiz.bergamot.notification.config.EmailNotifierCfg;

public class EmailNotifier extends DefaultNotifier
{
    public EmailNotifier()
    {
        super(EmailNotifierCfg.class, "/etc/bergamot/notifier/email.xml", "email");
    }
    
    public static void main(String[] args) throws Exception
    {
        Notifier notifier = new EmailNotifier();
        notifier.start();
    }
}

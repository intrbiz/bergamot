package com.intrbiz.bergamot.notification;

import com.intrbiz.bergamot.notification.config.SMSNotifierCfg;

public class SMSNotifier extends DefaultNotifier
{
    public SMSNotifier()
    {
        super(SMSNotifierCfg.class, "/etc/bergamot/notifier/sms.xml", "sms");
    }
    
    public static void main(String[] args) throws Exception
    {
        Notifier notifier = new SMSNotifier();
        notifier.start();
    }
}

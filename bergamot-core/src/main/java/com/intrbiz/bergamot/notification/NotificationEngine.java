package com.intrbiz.bergamot.notification;

import com.intrbiz.bergamot.config.NotificationEngineCfg;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.configuration.Configurable;

public interface NotificationEngine extends Configurable<NotificationEngineCfg>
{
    String getName();
    
    Notifier getNotifier();
    
    void setNotifier(Notifier notifier);
    
    void sendNotification(Notification notification);
}

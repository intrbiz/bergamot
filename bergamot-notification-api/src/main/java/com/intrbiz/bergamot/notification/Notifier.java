package com.intrbiz.bergamot.notification;

import java.util.List;
import java.util.UUID;

import com.intrbiz.bergamot.config.NotifierCfg;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.configuration.Configurable;

public interface Notifier extends Configurable<NotifierCfg>
{
    List<NotificationEngine> getEngines();
    
    void start() throws Exception;
    
    UUID getSite();
    
    void sendNotification(Notification notification);
}

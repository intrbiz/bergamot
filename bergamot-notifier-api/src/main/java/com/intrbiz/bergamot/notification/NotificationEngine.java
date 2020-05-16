package com.intrbiz.bergamot.notification;

import com.intrbiz.bergamot.model.message.notification.Notification;

public interface NotificationEngine
{
    String getVendor();
    
    String getName();
    
    boolean isEnabledByDefault();
    
    void prepare(NotificationEngineContext context) throws Exception;
    
    void start(NotificationEngineContext context) throws Exception;
    
    boolean accept(Notification notification);
    
    void sendNotification(Notification notification);
    
    void shutdown(NotificationEngineContext engineContext);
}

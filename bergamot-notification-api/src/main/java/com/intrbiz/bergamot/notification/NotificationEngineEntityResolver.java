package com.intrbiz.bergamot.notification;

import com.intrbiz.bergamot.model.message.ClusterMO;
import com.intrbiz.bergamot.model.message.HostMO;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.bergamot.model.message.TrapMO;
import com.intrbiz.bergamot.model.message.notification.CheckNotification;
import com.intrbiz.bergamot.model.message.notification.ContactNotification;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.express.ExpressEntityResolver;
import com.intrbiz.express.action.ActionHandler;

public class NotificationEngineEntityResolver extends ExpressEntityResolver
{
    protected final Notification notification;
    
    public NotificationEngineEntityResolver(Notification notification)
    {
        this.notification = notification;
    }
    
    @Override
    public Object getEntity(String name, Object source)
    {
        if (notification != null)
        {
            if ("this".equals(name) || "notification".equals(name))
            {
                return notification;
            }
            else if ("site".equals(name))
            {
                return ((Notification) notification).getSite();
            }
            else if ("contact".equals(name))
            {
                if (notification instanceof ContactNotification)
                    return ((ContactNotification) notification).getContact();
                return null;
            }
            else if ("check".equals(name))
            {
                return ((CheckNotification) notification).getCheck();
            }
            else if ("service".equals(name) && (((CheckNotification) notification).getCheck() instanceof ServiceMO))
            {
                return ((CheckNotification) notification).getCheck();
            }
            else if ("trap".equals(name) && (((CheckNotification) notification).getCheck() instanceof TrapMO))
            {
                return ((CheckNotification) notification).getCheck();
            }
            else if ("resource".equals(name) && (((CheckNotification) notification).getCheck() instanceof ResourceMO))
            {
                return ((CheckNotification) notification).getCheck();
            }
            else if ("host".equals(name) && (((CheckNotification) notification).getCheck() instanceof HostMO))
            {
                return ((CheckNotification) notification).getCheck();
            }
            else if ("host".equals(name) && (((CheckNotification) notification).getCheck() instanceof ServiceMO))
            {
                return ((ServiceMO) ((CheckNotification) notification).getCheck()).getHost();
            }
            else if ("host".equals(name) && (((CheckNotification) notification).getCheck() instanceof TrapMO))
            {
                return ((TrapMO) ((CheckNotification) notification).getCheck()).getHost();
            }
            else if ("cluster".equals(name) && (((CheckNotification) notification).getCheck() instanceof ClusterMO))
            {
                return ((CheckNotification) notification).getCheck();
            }
            else if ("cluster".equals(name) && (((CheckNotification) notification).getCheck() instanceof ResourceMO)) { return ((ResourceMO) ((CheckNotification) notification).getCheck()).getCluster(); }
        }
        return null;
    }

    @Override
    public ActionHandler getAction(String name, Object source)
    {
        return null;
    }
}
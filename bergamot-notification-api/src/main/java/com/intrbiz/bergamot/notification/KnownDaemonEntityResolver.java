package com.intrbiz.bergamot.notification;

import com.intrbiz.bergamot.health.model.KnownDaemon;
import com.intrbiz.express.ExpressEntityResolver;
import com.intrbiz.express.action.ActionHandler;

public class KnownDaemonEntityResolver extends ExpressEntityResolver
{
    private final KnownDaemon daemon;

    public KnownDaemonEntityResolver(KnownDaemon daemon)
    {
        this.daemon = daemon;
    }

    @Override
    public Object getEntity(String name, Object source)
    {
        if (daemon != null)
        {
            if ("this".equals(name) || "daemon".equals(name))
            {
                return daemon;
            }
        }
        return null;
    }

    @Override
    public ActionHandler getAction(String name, Object source)
    {
        return null;
    }
}
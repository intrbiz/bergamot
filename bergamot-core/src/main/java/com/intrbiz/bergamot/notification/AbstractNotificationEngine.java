package com.intrbiz.bergamot.notification;

import com.intrbiz.bergamot.config.NotificationEngineCfg;

public abstract class AbstractNotificationEngine implements NotificationEngine
{
    private final String name;
    
    protected NotificationEngineCfg config;
    
    protected Notifier notifier;
    
    public AbstractNotificationEngine(String name)
    {
        super();
        this.name = name;
    }

    @Override
    public void configure(NotificationEngineCfg cfg) throws Exception
    {
        this.config = cfg;
        this.configure();
    }
    
    protected void configure() throws Exception
    {
    }

    @Override
    public NotificationEngineCfg getConfiguration()
    {
        return this.config;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public Notifier getNotifier()
    {
        return this.notifier;
    }

    @Override
    public void setNotifier(Notifier notifier)
    {
        this.notifier = notifier;
    }
}

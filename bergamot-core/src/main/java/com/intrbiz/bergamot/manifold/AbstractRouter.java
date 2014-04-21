package com.intrbiz.bergamot.manifold;

import com.intrbiz.bergamot.config.RouterCfg;

public abstract class AbstractRouter implements MessageRouter
{
    protected RouterCfg config;
    
    public AbstractRouter()
    {
        super();
    }

    @Override
    public final void configure(RouterCfg cfg) throws Exception
    {
        this.config = cfg;
        this.configure();
    }
    
    protected void configure() throws Exception
    {
    }

    @Override
    public final RouterCfg getConfiguration()
    {
        return this.config;
    }
}

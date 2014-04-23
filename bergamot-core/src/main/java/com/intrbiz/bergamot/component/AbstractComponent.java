package com.intrbiz.bergamot.component;

import com.intrbiz.bergamot.Bergamot;
import com.intrbiz.configuration.Configuration;

public abstract class AbstractComponent<T extends Configuration> implements BergamotComponent<T>
{
    protected Bergamot bergamot;
    
    protected T config;
    
    public AbstractComponent()
    {
        super();
    }

    @Override
    public final void setBergamot(Bergamot daemon)
    {
        this.bergamot = daemon;
    }

    @Override
    public final Bergamot getBergamot()
    {
        return this.bergamot;
    }

    @Override
    public final T getConfiguration()
    {
        return this.config;
    }

    @Override
    public final void configure(T cfg) throws Exception
    {
        this.config = cfg;
        this.configure();
    }
    
    protected abstract void configure() throws Exception;
    
    @Override
    public void start() throws Exception
    {
    }
}

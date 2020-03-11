package com.intrbiz.bergamot.updater.context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.intrbiz.balsa.BalsaApplication;
import com.intrbiz.balsa.engine.session.BalsaSession;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.api.APIObject;
import com.intrbiz.bergamot.ui.BergamotApp;

public abstract class ClientContext
{    
    private ConcurrentMap<String, Object> vars = new ConcurrentHashMap<String, Object>();

    private Site site;

    private BalsaSession session;

    private Contact principal;

    public ClientContext()
    {
        super();
    }
    
    public BergamotApp app()
    {
        return (BergamotApp) BalsaApplication.getInstance();
    }

    public Site getSite()
    {
        return site;
    }

    public void setSite(Site site)
    {
        this.site = site;
    }

    public Contact getPrincipal()
    {
        return principal;
    }

    public void setPrincipal(Contact principal)
    {
        this.principal = principal;
    }

    public BalsaSession getSession()
    {
        return session;
    }

    public void setSession(BalsaSession session)
    {
        this.session = session;
    }

    @SuppressWarnings("unchecked")
    public <T> T var(String name)
    {
        return (T) this.vars.get(name);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T removeVar(String name)
    {
        return (T) this.vars.remove(name);
    }

    public <T> T var(String name, T value)
    {
        if (value == null)
            this.vars.remove(name);
        else
            this.vars.put(name, value);
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T computeVarIfAbsent(String key, Function<? super String, T> mappingFunction)
    {
        return (T) this.vars.computeIfAbsent(key, (k) -> (T) mappingFunction.apply(k));
    }

    @SuppressWarnings("unchecked")
    public <T> T computeVarIfPresent(String key, BiFunction<? super String, T, T> remappingFunction)
    {
        return (T) this.vars.computeIfPresent(key, (k, v) -> (T) remappingFunction.apply(k, (T) v));
    }

    @SuppressWarnings("unchecked")
    public  <T> T computeVar(String key, BiFunction<? super String, T, T> remappingFunction)
    {
        return (T) this.vars.compute(key, (k, v) -> (T) remappingFunction.apply(k, (T) v));
    }
    
    @SuppressWarnings("unchecked")
    public  <T> T mergeVar(String key, T value, BiFunction<T, T, T> remappingFunction)
    {
        return (T) this.vars.merge(key, value, (k, v) -> (T) remappingFunction.apply((T) v, value));
    }

    public void close()
    {
        this.vars.clear();
        this.principal = null;
        this.session = null;
        this.site = null;
        this.vars = null;
    }

    public abstract void send(APIObject value);
}

package com.intrbiz.bergamot.updater.context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.intrbiz.balsa.engine.session.BalsaSession;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.api.APIObject;

public abstract class ClientContext
{
    private ConcurrentMap<String, Object> vars = new ConcurrentHashMap<String, Object>();

    private CopyOnWriteArrayList<OnClose> closeHandlers = new CopyOnWriteArrayList<OnClose>();

    private Site site;

    private BalsaSession session;

    private Contact principal;

    public ClientContext()
    {
        super();
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

    public void onClose(OnClose close)
    {
        this.closeHandlers.add(close);
    }

    public void close()
    {
        for (OnClose handler : this.closeHandlers)
        {
            handler.onClose(this);
        }
    }

    public abstract void send(APIObject value);
}

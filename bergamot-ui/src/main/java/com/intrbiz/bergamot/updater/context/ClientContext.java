package com.intrbiz.bergamot.updater.context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.intrbiz.bergamot.model.message.api.APIObject;

public abstract class ClientContext
{
    private ConcurrentMap<String, Object> vars = new ConcurrentHashMap<String, Object>();
    
    private CopyOnWriteArrayList<OnClose> closeHandlers = new CopyOnWriteArrayList<OnClose>();
    
    public ClientContext()
    {
        super();
    }
    
    @SuppressWarnings("unchecked")
    public <T> T var(String name)
    {
        return (T) this.vars.get(name);
    }
    
    public <T> T var(String name, T value)
    {
        if (value == null) this.vars.remove(name);
        else this.vars.put(name, value);
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

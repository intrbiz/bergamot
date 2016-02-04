package com.intrbiz.bergamot.updater.handler;

import com.intrbiz.bergamot.model.message.api.APIRequest;
import com.intrbiz.bergamot.updater.context.ClientContext;

public abstract class RequestHandler<T extends APIRequest>
{
    private final Class<?>[] types;
    
    public RequestHandler(Class<?>[] types)
    {
        this.types = types;
    }
    
    public Class<?>[] getRequestTypes()
    {
        return this.types;
    }
    
    public abstract void onRequest(ClientContext context, T request);
}

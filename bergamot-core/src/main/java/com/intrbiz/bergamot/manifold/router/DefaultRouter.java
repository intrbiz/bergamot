package com.intrbiz.bergamot.manifold.router;

import com.intrbiz.bergamot.manifold.AbstractRouter;
import com.intrbiz.bergamot.manifold.model.MessageContext;

/**
 * Route the message based on the default routing it supplies
 */
public class DefaultRouter extends AbstractRouter
{
    public DefaultRouter()
    {
        super();
    }

    @Override
    public boolean route(MessageContext ctx)
    {
        // default route for 
        // use the default route of the message
        ctx.getRouting().setExchange(ctx.getMessage().getDefaultExchange());
        ctx.getRouting().setRoutingKey(ctx.getMessage().getDefaultRoutingKey());
        return true;
    }
}

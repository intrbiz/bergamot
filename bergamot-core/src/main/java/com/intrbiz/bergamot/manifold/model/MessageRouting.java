package com.intrbiz.bergamot.manifold.model;

import com.intrbiz.queue.name.GenericKey;

public class MessageRouting
{
    private String exchange;

    private GenericKey routingKey;

    public MessageRouting()
    {
        super();
    }

    public void setExchange(String exchange)
    {
        this.exchange = exchange;
    }

    public void setRoutingKey(GenericKey routingKey)
    {
        this.routingKey = routingKey;
    }

    public String getExchange()
    {
        return exchange;
    }

    public GenericKey getRoutingKey()
    {
        return routingKey;
    }
}

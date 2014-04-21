package com.intrbiz.bergamot.manifold.model;

import com.intrbiz.bergamot.model.message.Message;

public class MessageContext
{
    private final Message message;
    
    private MessageRouting routing = new MessageRouting();
    
    public MessageContext(Message message)
    {
        this.message = message;
    }

    public MessageRouting getRouting()
    {
        return routing;
    }

    public void setRouting(MessageRouting routing)
    {
        this.routing = routing;
    }

    public Message getMessage()
    {
        return message;
    }
}

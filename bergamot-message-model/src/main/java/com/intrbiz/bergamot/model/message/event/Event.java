package com.intrbiz.bergamot.model.message.event;

import com.intrbiz.bergamot.model.message.Message;

/**
 * Events which happen within the Bergamot cluster
 *
 */
public abstract class Event extends Message
{
    private static final long serialVersionUID = 1L;

    public Event()
    {
        super();
    }
}

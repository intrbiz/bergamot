package com.intrbiz.bergamot.model.message.processor;

/**
 * A pool message which can be consistently routed when the pool is not known
 */
public interface ProcessorHashable
{
    long routeHash();
}

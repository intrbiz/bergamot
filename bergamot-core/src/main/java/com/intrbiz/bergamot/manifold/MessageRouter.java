package com.intrbiz.bergamot.manifold;

import com.intrbiz.bergamot.config.RouterCfg;
import com.intrbiz.bergamot.manifold.model.MessageContext;
import com.intrbiz.configuration.Configurable;

/**
 * Apply some routing rules
 */
public interface MessageRouter extends Configurable<RouterCfg>
{
    /**
     * Apply routing logic to the given task
     * @param ctx the context of the task to route
     * @return true if routing was successfully applied, otherwise false
     */
    boolean route(MessageContext ctx);
}

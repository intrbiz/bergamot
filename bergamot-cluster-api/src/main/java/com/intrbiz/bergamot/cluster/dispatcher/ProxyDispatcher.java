package com.intrbiz.bergamot.cluster.dispatcher;

import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.model.message.proxy.ProxyMessage;

/**
 * Dispatch checks to proxies
 */
public interface ProxyDispatcher
{   
    PublishStatus dispatch(ProxyMessage message);
}

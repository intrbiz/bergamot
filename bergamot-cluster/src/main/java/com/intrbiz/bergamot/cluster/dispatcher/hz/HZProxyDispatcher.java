package com.intrbiz.bergamot.cluster.dispatcher.hz;

import java.util.Objects;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.dispatcher.ProxyDispatcher;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.cluster.registry.ProxyRegistry;
import com.intrbiz.bergamot.cluster.registry.ProxyRouteTable;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.proxy.ProxyMessage;

/**
 * Dispatch messages to proxy nodes
 */
public class HZProxyDispatcher extends HZBaseDispatcher<ProxyMessage> implements ProxyDispatcher
{   
    private final ProxyRegistry proxies;
    
    private final ProxyRouteTable proxyRouteTable;
    
    public HZProxyDispatcher(ProxyRegistry proxies, HazelcastInstance hazelcast)
    {
        super(hazelcast, HZNames::buildProxyRingbufferName);
        this.proxies = Objects.requireNonNull(proxies);
        this.proxyRouteTable = Objects.requireNonNull(this.proxies.getRouteTable());
    }

    @Override
    public PublishStatus dispatch(ProxyMessage message)
    {
        // Pick a proxy at random if we have no proxy id
        // TODO: it would be nice to validate the proxy exists
        if (message.getProxyId() == null)
        {
            message.setProxyId(this.proxyRouteTable.routeProxy());
        }
        // Did we manage to route the message
        if (message.getProxyId() == null)
        {
            return PublishStatus.Unroutable;
        }
        // Offer onto the proxy queue
        return this.offer(message.getProxyId(), message);
    }
}

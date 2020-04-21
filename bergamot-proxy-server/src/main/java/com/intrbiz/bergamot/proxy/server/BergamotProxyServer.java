package com.intrbiz.bergamot.proxy.server;

import com.intrbiz.bergamot.proxy.KeyResolver;

public class BergamotProxyServer extends BaseBergamotServer
{
    public BergamotProxyServer(int port, KeyResolver keyResolver, MessageProcessor.Factory processorFactory)
    {
        super(port, keyResolver, processorFactory);
    }
    
    protected String getServerName()
    {
        return "Bergamot Proxy";
    }
    
    protected String getWebSocketPath()
    {
        return "/proxy";
    }
}

package com.intrbiz.bergamot.proxy;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.dispatcher.ProxyDispatcher;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ProxyKey;
import com.intrbiz.bergamot.model.message.processor.proxy.LookupProxyKey;
import com.intrbiz.bergamot.model.message.processor.proxy.ProcessorProxyMessage;
import com.intrbiz.bergamot.model.message.proxy.FoundProxyKey;

public class ProxyProcessorService
{
    private static final Logger logger = Logger.getLogger(ProxyProcessorService.class);
    
    private final ProxyDispatcher proxyDispatcher;
    
    public ProxyProcessorService(ProxyDispatcher proxyDispatcher)
    {
        super();
        this.proxyDispatcher = proxyDispatcher;
    }
    
    public void process(ProcessorProxyMessage message)
    {
        if (message instanceof LookupProxyKey)
        {
            this.lookupAgentKey((LookupProxyKey) message);
        }
    }
    
    protected void lookupAgentKey(LookupProxyKey lookup)
    {
        FoundProxyKey reply = new FoundProxyKey(lookup);
        try
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                ProxyKey key = db.getProxyKey(lookup.getKeyId());
                if (key != null && (! key.isRevoked()))
                {
                    reply.setKey(key.toAuthenticationKey().toString());
                    reply.setSiteId(key.getSiteId());
                }
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to lookup proxy key", e);
        }
        this.proxyDispatcher.dispatch(reply);
    }
}

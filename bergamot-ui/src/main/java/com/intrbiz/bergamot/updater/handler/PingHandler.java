package com.intrbiz.bergamot.updater.handler;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.api.APIRequest;
import com.intrbiz.bergamot.model.message.api.util.APIPing;
import com.intrbiz.bergamot.model.message.api.util.APIPong;
import com.intrbiz.bergamot.updater.context.ClientContext;

public class PingHandler extends RequestHandler
{
    private Logger logger = Logger.getLogger(PingHandler.class);
    
    public PingHandler()
    {
        super(new Class<?>[] { APIPing.class });
    }

    @Override
    public void onRequest(ClientContext context, APIRequest request)
    {
        APIPing ping = (APIPing) request;
        logger.debug("Got ping from browser.");
        context.send(new APIPong(ping));
    }
}

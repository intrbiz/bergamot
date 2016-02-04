package com.intrbiz.bergamot.updater.handler;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.api.APIRequest;
import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.updater.context.ClientContext;

public class DefaultHandler extends RequestHandler<APIRequest>
{
    private Logger logger = Logger.getLogger(DefaultHandler.class);
    
    public DefaultHandler()
    {
        super(new Class<?>[0]);
    }

    @Override
    public void onRequest(ClientContext context, APIRequest request)
    {
        logger.warn("Unhandled request: " + request);
        context.send(new APIError(request, "Not found"));
    }
}

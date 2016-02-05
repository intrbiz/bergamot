package com.intrbiz.bergamot.updater.handler;

import java.util.UUID;

import com.intrbiz.bergamot.model.message.api.result.RegisterForAdhocResults;
import com.intrbiz.bergamot.model.message.api.result.RegisteredForAdhocResults;
import com.intrbiz.bergamot.updater.context.ClientContext;

public class RegisterForAdhocResultsHandler extends RequestHandler<RegisterForAdhocResults>
{
    public RegisterForAdhocResultsHandler()
    {
        super(new Class<?>[] { RegisterForAdhocResults.class });
    }

    @Override
    public void onRequest(ClientContext context, RegisterForAdhocResults request)
    {
        // our adhoc id
        UUID adhocId = context.var("adhocId");
        if (adhocId == null) adhocId = context.var("adhocId", context.getSite().randomObjectId());
        // TODO: setup the adhoc results queue
        // respond
        context.send(new RegisteredForAdhocResults(request, adhocId));
    }
}

package com.intrbiz.bergamot.updater.handler;

import java.util.UUID;

import com.intrbiz.bergamot.model.message.api.check.ExecuteAdhocCheck;
import com.intrbiz.bergamot.model.message.api.check.ExecutedAdhocCheck;
import com.intrbiz.bergamot.updater.context.ClientContext;

public class ExecuteAdhocChecksHandler extends RequestHandler<ExecuteAdhocCheck>
{
    public ExecuteAdhocChecksHandler()
    {
        super(new Class<?>[] { ExecuteAdhocCheck.class });
    }

    @Override
    public void onRequest(ClientContext context, ExecuteAdhocCheck request)
    {
        // random checkId
        UUID checkId = UUID.randomUUID();
        // TODO: send the check
        // respond
        context.send(new ExecutedAdhocCheck(request, checkId));
    }
}

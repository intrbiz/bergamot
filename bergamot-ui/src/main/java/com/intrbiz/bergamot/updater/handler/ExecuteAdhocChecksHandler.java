package com.intrbiz.bergamot.updater.handler;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.api.check.ExecuteAdhocCheck;
import com.intrbiz.bergamot.model.message.api.check.ExecutedAdhocCheck;
import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.queue.key.WorkerKey;
import com.intrbiz.bergamot.updater.context.ClientContext;
import com.intrbiz.queue.RoutedProducer;

public class ExecuteAdhocChecksHandler extends RequestHandler<ExecuteAdhocCheck>
{
    public ExecuteAdhocChecksHandler()
    {
        super(new Class<?>[] { ExecuteAdhocCheck.class });
    }

    @Override
    public void onRequest(ClientContext context, ExecuteAdhocCheck request)
    {
        UUID adhocId = context.var("adhocId");
        if (adhocId == null)
        {
            context.send(new APIError(request, "You must register for adhoc results first"));
            return;
        }
        // validate the check
        if (request.getCheck() == null)
        {
            context.send(new APIError(request, "You must give a check to execute"));
            return;
        }
        if (Util.isEmpty(request.getCheck().getEngine()) && Util.isEmpty(request.getEngine()))
        {
            context.send(new APIError(request, "You must specify the check engine"));
            return;
        }
        // clamp the ttl
        long ttl = request.getTtl() <= 500L || request.getTtl() > 300_000L ? 30_000L : request.getTtl();
        // random checkId
        UUID checkId = UUID.randomUUID();
        // get the producer - setup by register for adhoc results
        RoutedProducer<ExecuteCheck, WorkerKey> producer = context.var("adhocCheckProducer");
        // compute the worker key
        WorkerKey key = new WorkerKey(context.getSite().getId(), request.getWorkerPool(), Util.coalesceEmpty(request.getEngine(), request.getCheck().getEngine()), request.getAgentId());
        // setup the check
        ExecuteCheck check = request.getCheck();
        check.setAdhocId(adhocId);
        check.setId(checkId);
        // publish
        producer.publish(key, check, ttl);
        // respond
        context.send(new ExecutedAdhocCheck(request, checkId));
    }
}

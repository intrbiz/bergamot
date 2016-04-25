package com.intrbiz.bergamot.updater.handler;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.model.message.api.result.AdhocResultEvent;
import com.intrbiz.bergamot.model.message.api.result.RegisterForAdhocResults;
import com.intrbiz.bergamot.model.message.api.result.RegisteredForAdhocResults;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.queue.key.WorkerKey;
import com.intrbiz.bergamot.updater.context.ClientContext;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.RoutedProducer;

public class RegisterForAdhocResultsHandler extends RequestHandler<RegisterForAdhocResults>
{
    private Logger logger = Logger.getLogger(RegisterForAdhocResultsHandler.class);
    
    public RegisterForAdhocResultsHandler()
    {
        super(new Class<?>[] { RegisterForAdhocResults.class });
    }

    @Override
    public void onRequest(ClientContext context, RegisterForAdhocResults request)
    {
        // our adhoc id
        UUID adhocId = context.var("adhocId");
        if (adhocId == null)
        {
            adhocId = context.var("adhocId", context.getSite().randomObjectId());
            // setup the adhoc results queue
            try
            {
                // setup cleanup task
                context.onClose((ctx) -> {
                    if (logger.isTraceEnabled()) logger.trace("Got disconnect, closing adhoc result queue");
                    Consumer<ResultMO, ResultKey> c = ctx.removeVar("adhocResultsConsumer");
                    if (c != null) c.close();
                    RoutedProducer<ExecuteCheck, WorkerKey> p = ctx.removeVar("adhocCheckProducer");
                    if (p != null) p.close();
                    WorkerQueue q = ctx.removeVar("workerQueue");
                    if (q != null) q.close();
                });
                // open the queue
                WorkerQueue queue = context.var("workerQueue");
                if (queue == null) queue = context.var("workerQueue", WorkerQueue.open());
                // open the result consumer
                context.var("adhocResultsConsumer", queue.consumeAdhocResults(adhocId, (headers, result) -> {
                    if (logger.isTraceEnabled()) logger.trace("Publishing adhoc result to client: " + result);
                    context.send(new AdhocResultEvent(result));
                }));
                // setup the producer while we are here
                context.var("adhocCheckProducer", queue.publishChecks());
            }
            catch (QueueException e)
            {
                logger.warn("Failed to setup adhoc results queue", e);
                context.send(new APIError(request, "Failed to setup queue"));
            }
        }
        // respond
        context.send(new RegisteredForAdhocResults(request, adhocId));
    }
}

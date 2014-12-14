package com.intrbiz.bergamot.updater.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.api.APIRequest;
import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.model.message.api.update.RegisterForUpdates;
import com.intrbiz.bergamot.model.message.api.update.RegisteredForUpdates;
import com.intrbiz.bergamot.model.message.api.update.UpdateEvent;
import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.bergamot.queue.UpdateQueue;
import com.intrbiz.bergamot.updater.context.ClientContext;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.QueueException;

public class RegisterForUpdatesHandler extends RequestHandler
{
    private Logger logger = Logger.getLogger(RegisterForUpdatesHandler.class);
    
    public RegisterForUpdatesHandler()
    {
        super(new Class<?>[] { RegisterForUpdates.class });
    }

    @Override
    public void onRequest(ClientContext context, APIRequest request)
    {
        RegisterForUpdates rfsn = (RegisterForUpdates) request;
        // setup the queue
        if (context.var("updateConsumer") == null)
        {
            logger.info("Reigster for updates, for checks: " + rfsn.getCheckIds());
            Set<String> bindings = new HashSet<String>();
            for (UUID checkId : rfsn.getCheckIds())
            {
                // validate the check id
                if (checkId != null && context.getSite().getId().equals(Site.getSiteId(checkId))) 
                    bindings.add(Site.getSiteId(checkId).toString() + "." + checkId.toString());
            }
            if (!bindings.isEmpty())
            {
                try
                {
                    UpdateQueue queue = context.var("updateQueue", UpdateQueue.open());
                    context.var("updateConsumer", queue.consumeUpdates((u) -> { context.send(new UpdateEvent(u)); }, bindings));
                    // on close handler
                    context.onClose((ctx) -> {
                        Consumer<Update> c = ctx.var("updateConsumer");
                        if (c != null) c.close();
                        UpdateQueue q = ctx.var("updateQueue");
                        if (q != null) q.close();
                    });
                    // done
                    context.send(new RegisteredForUpdates(rfsn));
                }
                catch (QueueException e)
                {
                    context.var("updateQueue", null);
                    context.var("updateConsumer", null);
                    logger.error("Failed to setup queue", e);
                    context.send(new APIError("Failed to setup queue"));
                }
            }
            else
            {
                context.send(new APIError("No valid bindings"));
            }
        }
        else
        {
            Set<String> bindings = new HashSet<String>();
            for (UUID checkId : rfsn.getCheckIds())
            {
                // validate the check id
                if (checkId != null && context.getSite().getId().equals(Site.getSiteId(checkId))) 
                    bindings.add(Site.getSiteId(checkId).toString() + "." + checkId.toString());
            }
            if (!bindings.isEmpty())
            {
                Consumer<Update> updateConsumer = context.var("updateConsumer");
                for (String binding : bindings)
                {
                    logger.info("Updating bindings, adding: " + binding);
                    updateConsumer.addBinding(binding);
                }
                context.send(new RegisteredForUpdates(rfsn));
            }
            else
            {
                context.send(new APIError("No valid bindings"));
            }
        }
    }
}

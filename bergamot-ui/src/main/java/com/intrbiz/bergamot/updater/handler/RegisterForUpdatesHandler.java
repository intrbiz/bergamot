package com.intrbiz.bergamot.updater.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.api.error.APIError;
import com.intrbiz.bergamot.model.message.api.update.RegisterForUpdates;
import com.intrbiz.bergamot.model.message.api.update.RegisteredForUpdates;
import com.intrbiz.bergamot.model.message.api.update.UpdateEvent;
import com.intrbiz.bergamot.model.message.update.AlertUpdate;
import com.intrbiz.bergamot.model.message.update.CheckUpdate;
import com.intrbiz.bergamot.model.message.update.GroupUpdate;
import com.intrbiz.bergamot.model.message.update.LocationUpdate;
import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.bergamot.queue.UpdateQueue;
import com.intrbiz.bergamot.queue.key.UpdateKey;
import com.intrbiz.bergamot.queue.key.UpdateKey.UpdateType;
import com.intrbiz.bergamot.updater.context.ClientContext;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.QueueException;

public class RegisterForUpdatesHandler extends RequestHandler<RegisterForUpdates>
{
    private Logger logger = Logger.getLogger(RegisterForUpdatesHandler.class);
    
    public RegisterForUpdatesHandler()
    {
        super(new Class<?>[] { RegisterForUpdates.class });
    }

    @Override
    public void onRequest(ClientContext context, RegisterForUpdates request)
    {
        // setup the queue
        if (context.var("updateConsumer") == null)
        {
            try
            {
                // the bindings
                Set<UpdateKey> bindings = this.computeBindings(context, request);
                logger.debug("Reigster for updates: " + bindings);
                // setup the queue
                UpdateQueue queue = context.var("updateQueue", UpdateQueue.open());
                context.var("updateConsumer", queue.consumeUpdates((h, u) -> { this.sendUpdate(context, u); }, bindings));
                // on close handler
                context.onClose((ctx) -> {
                    Consumer<Update, UpdateKey> c = ctx.var("updateConsumer");
                    if (c != null) c.close();
                    UpdateQueue q = ctx.var("updateQueue");
                    if (q != null) q.close();
                });
                // done
                context.send(new RegisteredForUpdates(request));
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
            try
            {
                Set<UpdateKey> bindings = this.computeBindings(context, request);
                logger.debug("Reigster for updates: " + bindings);
                // update the bindings
                Consumer<Update, UpdateKey> updateConsumer = context.var("updateConsumer");
                for (UpdateKey binding : bindings)
                {
                    logger.debug("Updating bindings, adding: " + binding);
                    updateConsumer.addBinding(binding);
                }
                context.send(new RegisteredForUpdates(request));
            }
            catch (QueueException e)
            {
                logger.error("Failed to update queue bindings", e);
                context.send(new APIError("Failed to setup queue"));
            }
        }
    }
    
    private void sendUpdate(ClientContext context, Update update)
    {
        if (update instanceof CheckUpdate)
        {
            // only send the update if the user has permission over the check
            if (context.getPrincipal().hasPermission("read", ((CheckUpdate) update).getCheck().getId()))
            {
                context.send(new UpdateEvent(update));
            }
        }
        else if (update instanceof AlertUpdate)
        {
            if (context.getPrincipal().hasPermission("read", ((AlertUpdate) update).getAlert().getCheck().getId()))
            {
                context.send(new UpdateEvent(update));
            }
        }
        else if (update instanceof GroupUpdate)
        {
            // we need to recompute the state of the group with respect to the given user
            GroupUpdate groupUpdate = (GroupUpdate) update;
            try (BergamotDB db = BergamotDB.connect())
            {
                groupUpdate.getGroup().setState(db.computeGroupStateForContact(groupUpdate.getGroup().getId(), context.getPrincipal().getId()).toMO(context.getPrincipal()));
            }
            context.send(new UpdateEvent(update));
        }
        else if (update instanceof LocationUpdate)
        {
            // we need to recompute the state of the group with respect to the given user
            LocationUpdate locationUpdate = (LocationUpdate) update;
            try (BergamotDB db = BergamotDB.connect())
            {
                locationUpdate.getLocation().setState(db.computeLocationStateForContact(locationUpdate.getLocation().getId(), context.getPrincipal().getId()).toMO(context.getPrincipal()));
            }
            context.send(new UpdateEvent(update));
        }
    }
    
    private Set<UpdateKey> computeBindings(ClientContext context, RegisterForUpdates rfsn)
    {
        // the type of updates we are listening for
        UpdateType type = UpdateType.valueOf(Util.coalesceEmpty(rfsn.getUpdateType(), "check").toUpperCase());
        // wildcard ?
        if ((rfsn.getIds() == null || rfsn.getIds().isEmpty()))
        {
            return new HashSet<UpdateKey>(Arrays.asList(new UpdateKey(type, context.getSite().getId())));
        }
        // compute the bindings
        return rfsn.getIds().stream().map((id) -> new UpdateKey(type, context.getSite().getId(), id)).collect(Collectors.toSet());
    }
}

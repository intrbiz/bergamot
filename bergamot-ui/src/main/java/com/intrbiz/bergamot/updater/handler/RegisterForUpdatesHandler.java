package com.intrbiz.bergamot.updater.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.api.update.RegisterForUpdates;
import com.intrbiz.bergamot.model.message.api.update.UpdateEvent;
import com.intrbiz.bergamot.model.message.update.AlertUpdate;
import com.intrbiz.bergamot.model.message.update.CheckUpdate;
import com.intrbiz.bergamot.model.message.update.GroupUpdate;
import com.intrbiz.bergamot.model.message.update.LocationUpdate;
import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.bergamot.updater.context.ClientContext;

public class RegisterForUpdatesHandler extends RequestHandler<RegisterForUpdates>
{
    private static final String VAR_UPDATE_LISTENER_ID = "update.listener.id";
    
    private static final String VAR_UPDATE_BINDNGS = "update.bindings";
    
    private Logger logger = Logger.getLogger(RegisterForUpdatesHandler.class);
    
    public RegisterForUpdatesHandler()
    {
        super(new Class<?>[] { RegisterForUpdates.class });
    }

    @Override
    public void onRequest(ClientContext context, RegisterForUpdates request)
    {
        // setup the listener
        context.computeVarIfAbsent(VAR_UPDATE_LISTENER_ID, (key) -> {
            logger.debug("Listening for update on " + context.getSite().getId());
            return context.app().getUpdateBroker().listen(context.getSite().getId(), (update) -> {
                sendUpdate(context, update);
            });
        });
        // update the bindings
        context.mergeVar(VAR_UPDATE_BINDNGS, extractBindings(request), RegisterForUpdatesHandler::mergeBindings);
    }
    
    public void onClose(ClientContext context)
    {
        UUID listenerId = context.removeVar(VAR_UPDATE_LISTENER_ID);
        if (listenerId != null)
        {
            context.app().getUpdateBroker().unlisten(context.getSite().getId(), listenerId);
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
    
    private static Set<UUID> extractBindings(RegisterForUpdates rfsn)
    {
        return new HashSet<>(rfsn.getIds());
    }
    
    private static Set<UUID> mergeBindings(Set<UUID> a, Set<UUID> b)
    {
        Set<UUID> bindings = new HashSet<>();
        if (a != null)  bindings.addAll(a);
        if (b != null)  bindings.addAll(b);
        return bindings;
    }
}

package com.intrbiz.bergamot.cluster.controller;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;
import com.intrbiz.bergamot.model.message.event.control.ControlEvent;
import com.intrbiz.bergamot.model.message.event.control.RegisterWatcher;
import com.intrbiz.bergamot.model.message.event.watcher.RegisterCheck;
import com.intrbiz.bergamot.queue.ControlQueue;
import com.intrbiz.bergamot.queue.WatcherQueue;
import com.intrbiz.bergamot.queue.key.WatcherKey;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.NullKey;

public class BergamotController implements DeliveryHandler<ControlEvent>
{
    protected ControlQueue controlQueue;
    
    protected WatcherQueue watcherQueue;
    
    protected Consumer<ControlEvent, NullKey> controlConsumer;
    
    protected RoutedProducer<CheckEvent, WatcherKey> watcherProducer;
    
    private Logger logger = Logger.getLogger(BergamotController.class);
    
    public BergamotController()
    {
        super();
    }

    public void start()
    {
        this.controlQueue = ControlQueue.open();
        this.watcherQueue = WatcherQueue.open();
        this.watcherProducer = this.watcherQueue.publishWatcherEvents();
        this.controlConsumer = this.controlQueue.consumeControlEvents(this);
    }

    @Override
    public void handleDevliery(Map<String, Object> headers, ControlEvent event) throws IOException
    {
        if (event instanceof RegisterWatcher)
        {
            this.registerWatcher((RegisterWatcher) event);
        }
    }
    
    private void registerWatcher(RegisterWatcher watcher) throws IOException
    {
        // validate that the watcher has given us at least one filter
        if (watcher.getId() != null && (watcher.getSite() != null || watcher.getLocation() != null || (! Util.isEmpty(watcher.getEngine()))))
        {
            // send register check events to the watcher
            try (BergamotDB db = BergamotDB.connect())
            {
                for (Trap trap : db.listTrapsForWatcher(watcher.getSite(), watcher.getLocation(), watcher.getEngine()))
                {
                    RegisterCheck registerCheck = trap.registerCheck();
                    if (registerCheck != null)
                    {
                        logger.info("Registering trap: " + trap + " with watcher " + watcher.getId() + " [" + watcher.getSite() + "/" + watcher.getLocation() + "/" + watcher.getEngine() + "]");
                        this.watcherProducer.publish(new WatcherKey(watcher.getWatcher(), watcher.getEngine()), registerCheck);
                    }
                }
            }
        }
        else
        {
            logger.warn("The watcher: " + watcher.getId() + " cannot be registered as it does not provide any information about the traps it can handle");
        }
    }
}

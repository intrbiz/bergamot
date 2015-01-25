package com.intrbiz.bergamot.cluster.controller;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.CheckCommand;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;
import com.intrbiz.bergamot.model.message.event.control.ControlEvent;
import com.intrbiz.bergamot.model.message.event.control.RegisterWatcher;
import com.intrbiz.bergamot.queue.ControlQueue;
import com.intrbiz.bergamot.queue.WatcherQueue;
import com.intrbiz.bergamot.queue.key.WatcherKey;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.RoutedProducer;

public class BergamotController implements DeliveryHandler<ControlEvent>
{
    protected ControlQueue controlQueue;
    
    protected WatcherQueue watcherQueue;
    
    protected Consumer<ControlEvent> controlConsumer;
    
    protected RoutedProducer<CheckEvent> watcherProducer;
    
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
    public void handleDevliery(ControlEvent event) throws IOException
    {
        if (event instanceof RegisterWatcher)
        {
            this.registerWatcher((RegisterWatcher) event);
        }
    }
    
    private void registerWatcher(RegisterWatcher watcher) throws IOException
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            for (Trap trap : db.listTraps(watcher.getSite()))
            {
                logger.info("Registering trap: " + trap);
                CheckCommand checkCommand = trap.getCheckCommand();
                if (checkCommand != null)
                {
                    Command command = checkCommand.getCommand();
                    if (command != null && watcher.getEngine().equalsIgnoreCase(command.getEngine()))
                    {
                        this.watcherProducer.publish(new WatcherKey(watcher.getWatcher(), watcher.getEngine()), trap.registerCheck());
                    }
                }
            }
        }
    }
}

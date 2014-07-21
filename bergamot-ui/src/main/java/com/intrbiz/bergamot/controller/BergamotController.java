package com.intrbiz.bergamot.controller;

import java.io.IOException;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.CheckCommand;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
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

public class BergamotController implements DeliveryHandler<ControlEvent>
{
    protected ControlQueue controlQueue;
    
    protected WatcherQueue watcherQueue;
    
    protected Consumer<ControlEvent> controlConsumer;
    
    protected RoutedProducer<CheckEvent> watcherProducer;
    
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
            for (Location location : db.listLocations(watcher.getSite()))
            {
                for (Host host : location.getHosts())
                {
                    for (Trap trap : host.getTraps())
                    {
                        System.out.println("Registering trap: " + trap);
                        CheckCommand checkCommand = trap.getCheckCommand();
                        if (checkCommand != null)
                        {
                            Command command = checkCommand.getCommand();
                            if (command != null && watcher.getEngine().equalsIgnoreCase(command.getEngine()))
                            {
                                RegisterCheck register = trap.registerCheck();
                                System.out.println(new WatcherKey(watcher.getId(), watcher.getEngine()) + " => " + register);
                                this.watcherProducer.publish(new WatcherKey(watcher.getWatcher(), watcher.getEngine()), register);
                            }
                        }
                    }
                }
            }
        }
    }
}

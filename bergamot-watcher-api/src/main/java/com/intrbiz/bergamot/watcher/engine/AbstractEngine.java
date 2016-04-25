package com.intrbiz.bergamot.watcher.engine;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;
import com.intrbiz.bergamot.model.message.event.control.ControlEvent;
import com.intrbiz.bergamot.model.message.event.control.RegisterWatcher;
import com.intrbiz.bergamot.model.message.event.watcher.RegisterCheck;
import com.intrbiz.bergamot.model.message.event.watcher.UnregisterCheck;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.ControlQueue;
import com.intrbiz.bergamot.queue.WatcherQueue;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.ActiveResultKey;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.queue.key.WatcherKey;
import com.intrbiz.bergamot.watcher.Watcher;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.Producer;
import com.intrbiz.queue.RoutedProducer;

public class AbstractEngine implements Engine, DeliveryHandler<CheckEvent>
{
    private Logger logger = Logger.getLogger(AbstractEngine.class);

    protected Watcher watcher;

    protected final String name;

    protected EngineCfg config;

    protected List<Executor<?>> executor = new LinkedList<Executor<?>>();

    protected WorkerQueue workerQueue;
    
    protected WatcherQueue watcherQueue;
    
    protected ControlQueue controlQueue;

    protected Consumer<CheckEvent, WatcherKey> watcherEventConsumer;
    
    protected Producer<ControlEvent> controlEventProducer;
    
    private RoutedProducer<ResultMO, ResultKey> resultProducer;

    public AbstractEngine(final String name)
    {
        super();
        this.name = name;
    }

    @Override
    public void configure(EngineCfg cfg) throws Exception
    {
        this.config = cfg;
        this.configure();
    }

    @Override
    public EngineCfg getConfiguration()
    {
        return this.config;
    }

    protected void configure() throws Exception
    {
        for (ExecutorCfg executorCfg : this.config.getExecutors())
        {
            Executor<?> listener = (Executor<?>) executorCfg.create();
            this.addListener(listener);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void addListener(Executor<?> listener)
    {
        ((Executor) listener).setEngine(this);
        this.executor.add(listener);
    }

    @Override
    public Collection<Executor<?>> getExecutors()
    {
        return this.executor;
    }

    @Override
    public Watcher getWatcher()
    {
        return this.watcher;
    }

    @Override
    public void setWatcher(Watcher watcher)
    {
        this.watcher = watcher;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public void start() throws Exception
    {
        // open the queue
        this.controlQueue = ControlQueue.open();
        this.watcherQueue = WatcherQueue.open();
        this.workerQueue  = WorkerQueue.open();
        // control
        this.controlEventProducer = this.controlQueue.publishControlEvents();
        // watcher
        this.watcherEventConsumer = this.watcherQueue.consumeWatcherEvents(this, this.getWatcher().getId(), this.getName());
        // results
        this.resultProducer = this.workerQueue.publishResults();
        // start the executors
        for (Executor<?> ex : this.getExecutors())
        {
            ex.start();
        }
        // register with the controller
        this.controlEventProducer.publish(new RegisterWatcher(this.getWatcher().getId(), this.getName(), this.getWatcher().getSite(), this.getWatcher().getLocation()));
    }

    @Override
    public void handleDevliery(Map<String, Object> headers, CheckEvent event) throws IOException
    {
        if (logger.isTraceEnabled())
            logger.trace("Got event: " + event);
        if (event instanceof RegisterCheck)
        {
            this.registerCheck((RegisterCheck) event);
        }
        else if (event instanceof UnregisterCheck)
        {
            this.unregisterCheck((UnregisterCheck) event);
        }
    }
    
    private void registerCheck(RegisterCheck check)
    {
        for (Executor<?> executor : this.getExecutors())
        {
            if (executor.accept(check))
            {
                executor.register(check, (result) -> {
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Publishing result: " + result.getId() + " " + result.isOk() + " " + result.getStatus() + " " + result.getOutput());
                    }
                    // Note: using an ActiveResultKey here as we know the pool id
                    this.resultProducer.publish(new ActiveResultKey(check.getSiteId(), check.getProcessingPool()), result);
                });
                return;
            }
        }
        logger.warn("Failed to register check, no executor found for: " + check);
    }
    
    private void unregisterCheck(UnregisterCheck check)
    {
        for (Executor<?> executor : this.getExecutors())
        {
            if (executor.accept(check))
            {
                executor.unregister(check);
                return;
            }
        }
        logger.warn("Failed to unregister check, no executor found for: " + check);
    }
}

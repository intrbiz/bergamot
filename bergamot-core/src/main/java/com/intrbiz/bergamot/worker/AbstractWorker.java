package com.intrbiz.bergamot.worker;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.component.AbstractComponent;
import com.intrbiz.bergamot.config.EngineCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.task.Task;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.GenericKey;
import com.intrbiz.queue.name.Queue;

public abstract class AbstractWorker extends AbstractComponent<WorkerCfg> implements Worker, DeliveryHandler<Message>
{
    private Logger logger = Logger.getLogger(AbstractWorker.class);
    
    private Map<String, Engine> engines = new TreeMap<String, Engine>();
    
    private List<Consumer<Message, GenericKey>> consumers = new LinkedList<Consumer<Message, GenericKey>>();
    
    public AbstractWorker()
    {
        super();
    }

    @Override
    protected void configure() throws Exception
    {
        // load our runners
        for (EngineCfg engineCfg : this.config.getEngines())
        {
            Engine engine = (Engine) engineCfg.create();
            engine.setWorker(this);
            logger.info("Adding engine: " + engine);
            this.engines.put(engine.getName(), engine);
        }
    }
    
    public Collection<Engine> getEngines()
    {
        return this.engines.values();
    }
    
    public void start()
    {
        // start all the consumers
        for (int i = 0; i < this.config.getThreads(); i ++)
        {
            logger.info("Creating consumer " + i);
            Exchange exchange = this.config.getExchange().asExchange();
            Queue queue = this.config.getQueue() == null ? null : this.config.getQueue().asQueue();
            GenericKey[] bindings = this.config.asBindings();
            Consumer<Message, GenericKey> consumer = this.getBergamot().getManifold().setupConsumer(this, exchange, queue, bindings);
            this.consumers.add(consumer);
        }
    }
    
    protected void executeTask(Task task)
    {
        Engine engine = this.engines.get(task.getEngine());
        if (engine != null)
        {
            engine.execute(task);
        }
        else
        {
            logger.warn("No engine '" + task.getEngine() + "' could be found, dropping task.");
        }
    }

    @Override
    public void handleDevliery(Message event) throws IOException
    {
        if (event instanceof Task)
        {
            logger.debug("Got task: " + event);
            this.executeTask((Task) event);
        }
        else
        {
            logger.warn("Got non-task message, ignoring: " + event);
        }
    }
}

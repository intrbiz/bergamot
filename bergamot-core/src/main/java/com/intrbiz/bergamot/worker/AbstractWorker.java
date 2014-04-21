package com.intrbiz.bergamot.worker;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.RunnerCfg;
import com.intrbiz.bergamot.config.WorkerCfg;
import com.intrbiz.bergamot.engine.AbstractEngine;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.task.Task;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.GenericKey;
import com.intrbiz.queue.name.Queue;

public abstract class AbstractWorker extends AbstractEngine<WorkerCfg> implements Worker, DeliveryHandler<Message>
{
    private Logger logger = Logger.getLogger(AbstractWorker.class);
    
    private List<Runner> runners = new LinkedList<Runner>();
    
    private List<Consumer<Message, GenericKey>> consumers = new LinkedList<Consumer<Message, GenericKey>>();
    
    public AbstractWorker()
    {
        super();
    }

    @Override
    protected void configure() throws Exception
    {
        // load our runners
        for (RunnerCfg runnerCfg : this.config.getRunners())
        {
            Runner runner = (Runner) runnerCfg.create();
            runner.setWorker(this);
            logger.info("Adding runner: " + runner);
            this.runners.add(runner);
        }
    }
    
    public Collection<Runner> getRunners()
    {
        return this.runners;
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
        for (Runner runner : this.runners)
        {
            if (runner.accept(task))
            {
                runner.execute(task);
                return;
            }
        }
        logger.error("No runner for task: " + task.getClass() + ", cannot run task!");
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

package com.intrbiz.bergamot.scheduler;

import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.scheduler.DisableCheck;
import com.intrbiz.bergamot.model.message.scheduler.EnableCheck;
import com.intrbiz.bergamot.model.message.scheduler.PauseScheduler;
import com.intrbiz.bergamot.model.message.scheduler.RescheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.ResumeScheduler;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;
import com.intrbiz.bergamot.model.message.scheduler.UnscheduleCheck;
import com.intrbiz.bergamot.queue.SchedulerQueue;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.SchedulerKey;
import com.intrbiz.bergamot.queue.key.WorkerKey;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.RoutedProducer;


public abstract class AbstractScheduler implements Scheduler
{   
    private WorkerQueue workerQueue;
    
    private RoutedProducer<ExecuteCheck, WorkerKey> executeCheckProducer;
    
    private SchedulerQueue schedulerQueue;
    
    private Consumer<SchedulerAction, SchedulerKey> schedulerActionConsumer;
    
    public AbstractScheduler()
    {
        super();
    }
    
    protected void startQueues() throws Exception
    {
        this.workerQueue = WorkerQueue.open();
        this.executeCheckProducer = this.workerQueue.publishChecks();
        this.schedulerQueue = SchedulerQueue.open();
        // TODO scheduler names
        this.schedulerActionConsumer = this.schedulerQueue.consumeSchedulerActions((h, a) -> { executeAction(a); });
    }
    
    public void start() throws Exception
    {
        this.startQueues();
    }
    
    protected void shutdownQueues()
    {
        this.schedulerActionConsumer.close();
        this.schedulerQueue.close();
        this.executeCheckProducer.close();
        this.workerQueue.close();
    }
    
    public void shutdown()
    {
        this.shutdownQueues();
    }
    
    @Override
    public void ownPool(UUID site, int pool)
    {
        // bind any queues for the given pool
        this.schedulerActionConsumer.addBinding(new SchedulerKey(site, pool));
    }

    @Override
    public void disownPool(UUID site, int pool)
    {
        // unbind any queues for the given pool
        try
        {
            this.schedulerActionConsumer.removeBinding(new SchedulerKey(site, pool));
        }
        catch (QueueException e)
        {
        }
        // remove the jobs in the given pool
        this.removeJobsInPool(site, pool);
    }
    
    protected abstract void removeJobsInPool(UUID site, int pool);

    protected void publishExecuteCheck(ExecuteCheck check, WorkerKey routingKey, long ttl)
    {
        this.executeCheckProducer.publish(routingKey, check, ttl);
    }
    
    protected void executeAction(SchedulerAction action)
    {
        if (action instanceof PauseScheduler)
        {
            this.pause();
        }
        else if (action instanceof ResumeScheduler)
        {
            this.resume();
        }
        else if (action instanceof ScheduleCheck)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                ActiveCheck<?,?> check = (ActiveCheck<?,?>) db.getCheck(((ScheduleCheck) action).getCheck());
                if (check != null)
                {
                    this.schedule(check);
                }
            }
        }
        else if (action instanceof RescheduleCheck)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                ActiveCheck<?,?> check = (ActiveCheck<?,?>) db.getCheck(((RescheduleCheck) action).getCheck());
                if (check != null)
                {
                    this.reschedule(check, ((RescheduleCheck) action).getInterval());
                }
            }
        }
        else if (action instanceof EnableCheck)
        {
            this.enable(((EnableCheck) action).getCheck());
        }
        else if (action instanceof DisableCheck)
        {
            this.disable(((DisableCheck) action).getCheck());
        }
        else if (action instanceof UnscheduleCheck)
        {
            this.unschedule(((UnscheduleCheck) action).getCheck());
        }
    }
}

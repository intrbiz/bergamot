package com.intrbiz.bergamot.scheduler;

import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.scheduler.DisableCheck;
import com.intrbiz.bergamot.model.message.scheduler.EnableCheck;
import com.intrbiz.bergamot.model.message.scheduler.PauseScheduler;
import com.intrbiz.bergamot.model.message.scheduler.UnscheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.RescheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.ResumeScheduler;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;
import com.intrbiz.bergamot.queue.SchedulerQueue;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.SchedulerKey;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.GenericKey;


public abstract class AbstractScheduler implements Scheduler
{   
    private WorkerQueue workerQueue;
    
    private RoutedProducer<ExecuteCheck> executeCheckProducer;
    
    private SchedulerQueue schedulerQueue;
    
    private Consumer<SchedulerAction> schedulerActionConsumer;
    
    public AbstractScheduler()
    {
        super();
    }
    
    public void start() throws Exception
    {
        this.workerQueue = WorkerQueue.open();
        this.executeCheckProducer = this.workerQueue.publishChecks();
        this.schedulerQueue = SchedulerQueue.open();
        // TODO scheduler names
        this.schedulerActionConsumer = this.schedulerQueue.consumeSchedulerActions((a) -> { executeAction(a); });
        
    }
    
    public void shutdown()
    {
        this.schedulerActionConsumer.close();
        this.schedulerQueue.close();
        this.executeCheckProducer.close();
        this.workerQueue.close();
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

    protected void publishExecuteCheck(ExecuteCheck check, GenericKey routingKey, long ttl)
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
                    this.reschedule(check);
                }
            }
        }
        else if (action instanceof EnableCheck)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                ActiveCheck<?,?> check = (ActiveCheck<?,?>) db.getCheck(((EnableCheck) action).getCheck());
                if (check != null)
                {
                    this.enable(check);
                }
            }
        }
        else if (action instanceof DisableCheck)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                ActiveCheck<?,?> check = (ActiveCheck<?,?>) db.getCheck(((DisableCheck) action).getCheck());
                if (check != null)
                {
                    this.disable(check);
                }
            }
        }
        else if (action instanceof UnscheduleCheck)
        {
            this.unschedule(((UnscheduleCheck) action).getCheck());
        }
    }
}

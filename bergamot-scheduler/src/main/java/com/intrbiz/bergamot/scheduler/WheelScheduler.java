package com.intrbiz.bergamot.scheduler;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.accounting.Accounting;
import com.intrbiz.bergamot.accounting.model.ExecuteCheckAccountingEvent;
import com.intrbiz.bergamot.cluster.dispatcher.CheckDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.timeperiod.TimeRange;
import com.intrbiz.util.IBThreadFactory;

/**
 * A tick-wheel based scheduler for scheduling checks.
 * 
 * This is designed to be an efficient way to scheduled thousands of 
 * jobs repeatedly. Conceptually it is a clock wheel which rotates at 
 * a specific frequency, as defined by 1/tickPeriod. The wheel is 
 * split into a number of segments, each segment containing a set of 
 * jobs. The rotationPeriod, the time to complete a full cycle of the 
 * wheel is tickPeriod * segmentCount.
 * 
 * By default the wheel has 1200 segments ticking at 4Hz. As such the 
 * wheel rotates once 5 minutes.
 * 
 * Jobs are balanced over the segments within the wheel when they 
 * are first scheduled. As such, the first execution of a job is at 
 * most rotationPeriod.
 * 
 * Note: this scheduler is an approximating scheduler. It can never 
 * be more accurate than the tickPeriod.
 * 
 * @author Chris Ellis
 */
public class WheelScheduler extends AbstractScheduler
{
    private static final Logger logger = Logger.getLogger(WheelScheduler.class);
    
    // Wheel ticks 4 times a second
    private static final long TICK_PERIOD_MS = 250;
    
    // Wheel represents 5 minutes
    private static final int SEGMENTS = 300 * ((int)(1000L / TICK_PERIOD_MS));

    // ticker
    protected volatile boolean run = true;

    protected final Object tickerWaitLock = new Object();

    protected Thread ticker;

    // state
    protected volatile boolean schedulerEnabled = true;

    // wheel
    protected final ConcurrentMap<UUID, Job> jobs;

    protected final Segment[] orange;

    protected final long tickPeriod;

    protected final long orangePeriod;

    protected volatile int tick = -1;

    protected volatile long tickTime = System.currentTimeMillis();

    protected volatile Calendar tickCalendar = Calendar.getInstance();

    // initial delay allocation
    protected final SecureRandom initialDelay = new SecureRandom();
    
    // task executor
    protected final ExecutorService taskExecutor;
    
    // accounting
    protected final Accounting accounting = Accounting.create(WheelScheduler.class);

    public WheelScheduler(UUID processorId, CheckDispatcher checkDispatcher, ProcessorDispatcher processorDispatcher)
    {
        super(processorId, checkDispatcher, processorDispatcher);
        // setup the wheel structure
        this.tickPeriod = TICK_PERIOD_MS;
        this.orange = new Segment[SEGMENTS];
        this.orangePeriod = this.orange.length * this.tickPeriod;
        for (int i = 0; i < orange.length; i++)
        {
            this.orange[i] = new Segment(i);
        }
        this.jobs = new ConcurrentHashMap<UUID, Job>();
        logger.debug("Initalised wheel with " + this.orange.length + " segments, rotation period: " + this.orangePeriod);
        // create our task executor
        this.taskExecutor = Executors.newFixedThreadPool(
                Integer.parseInt(Util.coalesceEmpty(System.getenv("BERGAMOT_SCHEDULER_TASK_THREADS"), System.getProperty("bergamot.scheduler.task.threads"), String.valueOf(Runtime.getRuntime().availableProcessors()))),
                new IBThreadFactory("bergamot-scheduler-task", true)
        );
    }

    protected void tick()
    {
        // tick tock
        this.tick = (this.tick + 1) % this.orange.length;
        this.tickTime = System.currentTimeMillis();
        this.tickCalendar.setTimeInMillis(this.tickTime);
        // if (logger.isTraceEnabled()) logger.trace("Tick " + this.tick + " at " + this.tickTime);
        // process any jobs in the current segment
        if (this.schedulerEnabled)
        {
            this.processSegment(this.orange[this.tick]);
        }
    }

    protected void processSegment(Segment current)
    {
        if (logger.isTraceEnabled()) logger.trace("Processing " + current.jobs.size() + " jobs");
        for (Job job : current.jobs.values())
        {
            if (job.enabled)
            {
                if (logger.isTraceEnabled()) logger.trace("Job " + job.id + " expires at " + job.expires + " <= " + this.tickTime);
                if (job.expires <= this.tickTime)
                {
                    // compute the next expiry time
                    job.lastExpires = job.expires;
                    job.expires = job.lastExpires + job.interval;
                    // check the time period
                    if (job.timeRange == null || this.isInTimeRange(job.timeRange, this.tickCalendar))
                    {
                        if (logger.isTraceEnabled()) logger.trace("Job " + job.id + " expired, executing. Next expires at " + job.expires);
                        this.runJob(job);
                    }
                    else
                    {
                        if (logger.isTraceEnabled()) logger.trace("Job " + job.id + " is not in time period, skiping this check");
                    }
                }
            }
            else
            {
                if (logger.isTraceEnabled()) logger.trace("Skipping disabled job " + job.id);
            }
        }
    }

    protected boolean isInTimeRange(TimeRange range, Calendar calendar)
    {
        long s = System.nanoTime();
        boolean res = range.isInTimeRange(calendar);
        long e = System.nanoTime();
        if (logger.isTraceEnabled()) logger.trace("Is in time range check: " + (((double) (e - s)) / 1000D) + "us");
        return res;
    }

    protected void runJob(final Job job)
    {
        // execute the actual task out of the scheduling thread
        this.taskExecutor.execute(new Runnable() {
            public void run()
            {
                try
                {
                    job.command.run();
                }
                catch (Exception e)
                {
                    logger.error("Error executing scheduled job " + job.id, e);
                }
            }
        });
    }

    protected long validateInterval(long interval)
    {
        if (interval < this.tickPeriod)
        {
            logger.warn("Currently jobs cannot be scheduled more frequently than every " + this.tickPeriod + " ms, rounding up.");
            interval = this.tickPeriod;
        }
        return interval;
    }
    
    protected boolean isMultiSegment(long interval)
    {
        return interval < this.orangePeriod;
    }
    
    protected void addJobToSegments(Job job, long interval, long initialDelay)
    {
        // pick the initial segment
        int segmentStart = ((int) ((initialDelay / this.tickPeriod) % this.orange.length));
        if (logger.isTraceEnabled())logger.trace("Scheduling job " + job.id + " with initial segment " + segmentStart);
        // how many segments should this check be placed into
        int segmentCount = (int) Math.min(isMultiSegment(interval) ? (this.orangePeriod / interval) : 1, this.orange.length);
        if (logger.isTraceEnabled())logger.trace("Scheduling job " + job.id + " into " + segmentCount + " segments");
        for (int i = 0; i < segmentCount; i++)
        {
            int segmentIdx = (segmentStart + (i * (this.orange.length / segmentCount))) % this.orange.length;
            if (logger.isTraceEnabled()) logger.trace("Adding job " + job.id + " to segment: " + segmentIdx + " with interval " + interval + "ms");
            this.orange[segmentIdx].jobs.put(job.id, job);
        }
    }
    
    protected void removeJobFromSegments(UUID id)
    {
        for (Segment segment : this.orange)
        {
            Job removed = segment.jobs.remove(id);
            if (logger.isTraceEnabled() && removed != null) logger.trace("Removed job " + id + " from segment " + segment.id);
        }
    }
    
    protected void removeJobsFromSegments(Collection<UUID> ids)
    {
        for (Segment segment : this.orange)
        {
            for (UUID id : ids)
            {
                Job removed = segment.jobs.remove(id);
                if (logger.isTraceEnabled() && removed != null) logger.trace("Removed job " + id + " from segment " + segment.id);
            }
        }
    }

    protected void scheduleJob(UUID id, int processingPool, UUID site, int pool, long interval, long initialDelay, TimeRange timeRange, Runnable command)
    {
        if (!this.jobs.containsKey(id))
        {
            // validate the interval
            interval = this.validateInterval(interval);
            logger.info("Scheduling job " + id + " with interval " + interval + "ms and initial delay " + initialDelay + " ms");
            // the job
            Job job = new Job(id, processingPool, interval, initialDelay, timeRange, command);
            this.jobs.put(job.id, job);
            // place the job into segments
            this.addJobToSegments(job, interval, initialDelay);
        }
        else
        {
            this.rescheduleJob(id, interval, timeRange, command);
        }
    }
    
    protected void rescheduleJob(UUID id, long newInterval)
    {
        newInterval = this.validateInterval(newInterval);
        Job job = this.jobs.get(id);
        if (job != null)
        {
            job.interval = newInterval;
            // compute the new expiry
            job.expires = job.lastExpires + newInterval;
            job.enabled = true;
            logger.info("Rescheduled job " + job.id + ", new expiry: " + job.expires);
            // move the job between segments
            // remove the job from all segments
            this.removeJobFromSegments(id);
            // add the job to segments
            this.addJobToSegments(job, newInterval, job.initialDelay);
        }
    }

    protected void rescheduleJob(UUID id, long newInterval, TimeRange timeRange, Runnable command)
    {
        newInterval = this.validateInterval(newInterval);
        Job job = this.jobs.get(id);
        if (job != null)
        {
            job.interval = newInterval;
            if (timeRange != null) job.timeRange = timeRange;
            if (command != null)   job.command = command;
            // compute the new expiry
            job.expires = job.lastExpires + newInterval;
            job.enabled = true;
            logger.info("Rescheduled job " + job.id + ", new expiry: " + job.expires);
            // move the job between segments
            // remove the job from all segments
            this.removeJobFromSegments(id);
            // add the job to segments
            this.addJobToSegments(job, newInterval, job.initialDelay);
        }
    }
    
    protected void removeJob(UUID id)
    {
        logger.info("Removing job " + id + " from scheduling");
        // ensure the given job is removed
        this.jobs.remove(id);
        // remove the job from all segments
        this.removeJobFromSegments(id);
    }
    
    protected void removeJobs(Collection<UUID> ids)
    {
        logger.info("Removing jobs " + ids + " from scheduling");
        // ensure the given job is removed
        for (UUID id : ids)
        {
            this.jobs.remove(id);
        }
        // remove the job from all segments
        this.removeJobsFromSegments(ids);
    }

    protected void enableJob(UUID id)
    {
        Job job = this.jobs.get(id);
        if (job != null)
        {
            job.enabled = true;
        }
    }

    protected void disableJob(UUID id)
    {
        Job job = this.jobs.get(id);
        if (job != null)
        {
            job.enabled = false;
        }
    }

    protected void pauseScheduler()
    {
        this.schedulerEnabled = false;
    }

    protected void resumeScheduler()
    {
        this.schedulerEnabled = true;
    }

    @Override
    public void pause()
    {
        this.pauseScheduler();
    }

    @Override
    public void resume()
    {
        this.resumeScheduler();
    }

    @Override
    public void enable(UUID check)
    {
        this.enableJob(check);
    }

    @Override
    public void disable(UUID check)
    {
        this.disableJob(check);
    }
    
    @Override
    public void unschedule(UUID check)
    {
        this.removeJob(check);
    }

    @Override
    public void schedule(ActiveCheck<?,?> check)
    {
        // randomly distribute the initial delay
        long initialDelay = (long) (this.initialDelay.nextDouble() * ((double) check.getCurrentInterval()));
        logger.info("Scheduling " + check + " with interval " + check.getCurrentInterval() + " and initial delay " + initialDelay);
        this.scheduleJob(check.getId(), check.getPool(), check.getSiteId(), check.getPool(), check.getCurrentInterval(), initialDelay, check.getTimePeriod(), new CheckRunner(check));
    }

    @Override
    public void reschedule(ActiveCheck<?,?> check, long interval)
    {
        interval = interval > 0 ? interval : check.getCurrentInterval();
        logger.info("Rescheduling " + check.getId() + " with interval " + interval);
        this.rescheduleJob(check.getId(), interval, check.getTimePeriod(), new CheckRunner(check));
    }
    
    @Override
    public void reschedule(UUID checkId, long interval)
    {
        if (interval > 0)
        {
            logger.info("Rescheduling " + checkId + " with interval " + interval);
            this.rescheduleJob(checkId, interval);
        }
        else
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                ActiveCheck<?,?> check = db.getActiveCheck(checkId);
                if (check != null)
                {
                    interval = check.getCurrentInterval();
                    logger.info("Rescheduling " + check.getId() + " with interval " + interval);
                    this.rescheduleJob(check.getId(), interval, check.getTimePeriod(), new CheckRunner(check));            
                }
            }
        }
    }
    
    @Override
    public void unschedule(Collection<UUID> checks)
    {
        this.removeJobs(checks);
    }
    
    public void unschedulePool(int pool)
    {
        logger.info("Unscheduling all checks in pool: " + pool);
        for (Segment segment : this.orange)
        {
            for (Job job : segment.jobs.values())
            {
                if (pool == job.pool)
                {
                    segment.jobs.remove(job.id);
                    this.jobs.remove(job.id);
                    if (logger.isTraceEnabled() && job != null)
                        logger.trace("Removed job " + job.id + " from segment " + segment.id);
                }
            }
        }
    }

    @Override
    public void start() throws Exception
    {
        // ensure that we are ready to run
        this.resumeScheduler();
        // setup the ticker thread
        if (this.ticker == null)
        {
            this.ticker = new Thread(new Ticker(), "wheel-scheduler");
            this.ticker.start();
        }
    }
    
    @Override
    public void shutdown()
    {
        this.run = false;
        synchronized (this.tickerWaitLock)
        {
            this.tickerWaitLock.notifyAll();
        }
        try
        {
            this.ticker.join();
        }
        catch (InterruptedException e)
        {
        }
        this.taskExecutor.shutdown();
        try
        {
            this.taskExecutor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
        }
    }

    /**
     * Simply tick at a specific frequency
     */
    private class Ticker implements Runnable
    {
        public void run()
        {
            if (logger.isTraceEnabled())
                logger.trace("Ticker starting running: " + run + ", tick every " + tickPeriod + "ms, rotating every " + orangePeriod + "ms, currently " + jobs.size() + " jobs loaded");
            while (run)
            {
                long tickStart = System.currentTimeMillis();
                tick();
                long tickEnd = System.currentTimeMillis();
                long sleepDuration = tickPeriod - (tickEnd - tickStart);
                // if (logger.isTraceEnabled()) logger.trace("Tick took " + (tickEnd - tickStart) + "ms to run, sleeping for " + sleepDuration + "ms");
                if (sleepDuration > 0)
                {
                    try
                    {
                        synchronized (tickerWaitLock)
                        {
                            tickerWaitLock.wait(tickPeriod);
                        }
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
            }
            ticker = null;
        }
    }

    /**
     * A segment within the wheel
     */
    private class Segment
    {
        public final int id;
        
        public final ConcurrentMap<UUID, Job> jobs = new ConcurrentHashMap<UUID, Job>();

        public Segment(int id)
        {
            super();
            this.id = id;
        }
    }

    /**
     * A scheduled job
     */
    private static class Job
    {
        // details
        public final UUID id;
        
        public final int pool;
        
        public final long initialDelay;

        public volatile Runnable command;

        public volatile TimeRange timeRange;
        
        public volatile boolean enabled = true;

        public volatile long interval;

        public volatile long expires;

        public volatile long lastExpires;

        public Job(UUID id, int pool, long interval, long initialDelay, TimeRange timeRange, Runnable command)
        {
            super();
            this.id = id;
            this.pool = pool;
            this.interval = interval;
            this.timeRange = timeRange;
            this.command = command;
            this.initialDelay  = initialDelay;
            // compute the expiry time
            this.lastExpires = this.expires = System.currentTimeMillis() + initialDelay;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Job other = (Job) obj;
            if (id == null)
            {
                if (other.id != null) return false;
            }
            else if (!id.equals(other.id)) return false;
            return true;
        }
    }

    private class CheckRunner implements Runnable
    {
        public final ActiveCheck<?,?> check;

        public CheckRunner(ActiveCheck<?,?> check)
        {
            this.check = check;
        }

        public void run()
        {
            // fire off the check
            ExecuteCheck executeCheck = this.check.executeCheck();
            if (executeCheck != null)
            {
                // publish the check
                PublishStatus result = WheelScheduler.this.publishExecuteCheck(executeCheck);
                if (result == PublishStatus.Success)
                {
                    WheelScheduler.this.accounting.account(new ExecuteCheckAccountingEvent(executeCheck.getSiteId(), executeCheck.getId(), check.getId(), executeCheck.getEngine(), executeCheck.getExecutor(), executeCheck.getName()));
                }
            }
        }
    }
}

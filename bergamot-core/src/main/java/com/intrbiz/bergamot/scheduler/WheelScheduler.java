package com.intrbiz.bergamot.scheduler;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.message.task.check.ExecuteCheck;
import com.intrbiz.bergamot.model.timeperiod.TimeRange;

/**
 * A tick-wheel based scheduler for scheduling checks.
 * 
 * This is designed to be an efficient way to scheduled thousands of jobs repeatedly. Conceptually it is a clock wheel which rotates at a specific frequency, as defined by 1/tickPeriod. The wheel is split into a number of segments, each segment containing a set of jobs. The rotationPeriod, the time to complete a full cycle of the wheel is tickPeriod * segmentCount.
 * 
 * By default the wheel has 60 segments ticking at 1Hz. As such the wheel rotates once every minute.
 * 
 * Jobs are balanced over the segments within the wheel when they are first scheduled. As such, the first execution of a job is at most rotationPeriod.
 * 
 * Note: this scheduler is an approximating scheduler. It can never be more accurate than the tickPeriod. Currently this implementation can never be more accurate than the rotation period.
 * 
 * TODO list: 1. Add time-period support rather than check blindly 24x7 2. Look at check latencies and pause scheduling if needed? 3. Support intervals < rotationPeriod
 * 
 * @author Chris Ellis
 * 
 */
public class WheelScheduler extends AbstractScheduler
{
    private Logger logger = Logger.getLogger(WheelScheduler.class);

    // ticker
    private volatile boolean run = true;

    private final Object tickerWaitLock = new Object();

    private Thread ticker;

    // state

    private volatile boolean schedulerEnabled = true;

    // wheel

    private Map<UUID, Job> jobs;

    private Segment[] orange;

    private long tickPeriod;

    private long orangePeriod;

    private volatile int tick = -1;

    private volatile long tickTime = System.currentTimeMillis();

    private volatile Calendar tickCalendar = Calendar.getInstance();

    public WheelScheduler()
    {
        super();
        this.setupWheel(1_000L, 60);
    }

    private void setupWheel(long tickPeriod, int segments)
    {
        this.tickPeriod = 1_000L;
        this.orange = new Segment[60];
        this.orangePeriod = this.orange.length * this.tickPeriod;
        for (int i = 0; i < orange.length; i++)
        {
            this.orange[i] = new Segment();
        }
        this.jobs = new HashMap<UUID, Job>();
        logger.debug("Initalised wheel with " + this.orange.length + " segments, rotation period: " + this.orangePeriod);
    }

    protected void tick()
    {
        // tick tock
        this.tick = (this.tick + 1) % this.orange.length;
        this.tickTime = System.currentTimeMillis();
        this.tickCalendar.setTimeInMillis(this.tickTime);
        logger.trace("Tick " + this.tick + " at " + this.tickTime);
        // process any jobs in the current segment
        if (this.schedulerEnabled)
        {
            this.processSegment(this.orange[this.tick]);
        }
    }

    protected void processSegment(Segment current)
    {
        logger.trace("Processing " + current.jobs.size() + " jobs");
        synchronized (current)
        {
            for (Job job : current.jobs)
            {
                if (job.enabled)
                {
                    long timeSinceLastRun = this.tickTime - job.lastRunAt;
                    if ((timeSinceLastRun) > (job.interval - this.tickPeriod))
                    {
                        // check the time period
                        if (job.timeRange == null || this.isInTimeRange(job.timeRange, this.tickCalendar))
                        {
                            logger.trace("Job " + job.id + " last run " + timeSinceLastRun + "ms ago, executing.");
                            this.runJob(job);
                            job.lastRunAt = this.tickTime;
                            // TODO should we disable a job until we get it's result ?
                        }
                        else
                        {
                            logger.trace("Job " + job.id + " is not in time period, skiping this check");
                        }
                    }
                }
                else
                {
                    logger.trace("Skipping disabled job " + job.id);
                }
            }
        }
    }

    protected boolean isInTimeRange(TimeRange range, Calendar calendar)
    {
        long s = System.nanoTime();
        boolean res = range.isInTimeRange(calendar);
        long e = System.nanoTime();
        logger.trace("Is in time range check: " + (((double) (e - s)) / 1000D) + "us");
        return res;
    }

    protected void runJob(Job job)
    {
        // TODO run in a thread pool ?
        try
        {
            job.command.run();
        }
        catch (Exception e)
        {
            logger.error("Error executing scheduled job " + job.id);
        }
    }

    protected int pickSegmentToInsertInto()
    {
        // find the segment with the least jobs
        int winningSegment = 0;
        int minJobs = this.orange[0].jobs.size();
        for (int i = 1; i < this.orange.length; i++)
        {
            int jobs = this.orange[i].jobs.size();
            if (jobs < minJobs)
            {
                minJobs = jobs;
                winningSegment = i;
            }
        }
        return winningSegment;
    }

    protected void scheduleJob(UUID id, long interval, TimeRange timeRange, Runnable command)
    {
        synchronized (this.jobs)
        {
            if (!this.jobs.containsKey(id))
            {
                interval = this.validateInterval(interval);
                // the job
                Job job = new Job(id, interval, timeRange, command);
                this.jobs.put(job.id, job);
                // pick a randomised segment to insert the job into
                int segmentIdx = this.pickSegmentToInsertInto();
                logger.trace("Adding job to segment: " + segmentIdx + " with interval " + interval);
                Segment segment = this.orange[segmentIdx];
                synchronized (segment)
                {
                    segment.jobs.add(job);
                }
            }
            else
            {
                logger.debug("Job " + id + " is already scheduled, not adding it again.");
            }
        }
    }

    protected void rescheduleJob(UUID id, long newInterval, TimeRange timeRange)
    {
        synchronized (this.jobs)
        {
            newInterval = this.validateInterval(newInterval);
            Job job = this.jobs.get(id);
            if (job != null)
            {
                job.interval = newInterval;
                job.timeRange = timeRange;
                job.enabled = true;
            }
        }
    }
    
    protected void enableJob(UUID id)
    {
        synchronized (this.jobs)
        {
            Job job = this.jobs.get(id);
            if (job != null)
            {
                job.enabled = true;
            }
        }
    }
    
    protected void disableJob(UUID id)
    {
        synchronized (this.jobs)
        {
            Job job = this.jobs.get(id);
            if (job != null)
            {
                job.enabled = false;
            }
        }
    }

    protected long validateInterval(long interval)
    {
        if (interval < this.orangePeriod)
        {
            logger.warn("Currently jobs cannot be scheduled more frequently than every " + this.tickPeriod + "ms, rounding up.");
            interval = orangePeriod;
        }
        return interval;
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
    public void enable(Check check)
    {
        this.enableJob(check.getId());
    }

    @Override
    public void disable(Check check)
    {
        this.disableJob(check.getId());
    }

    @Override
    public void schedule(Check check)
    {
        logger.info("Scheduling " + check + " with interval " + check.getCurrentInterval());
        this.scheduleJob(check.getId(), check.getCurrentInterval(), check.getCheckPeriod(), new CheckRunner(check));
    }

    @Override
    public void reschedule(Check check)
    {
        logger.info("Rescheduling " + check + " with interval " + check.getCurrentInterval());
        this.rescheduleJob(check.getId(), check.getCurrentInterval(), check.getCheckPeriod());
    }

    @Override
    protected void configure() throws Exception
    {
    }

    @Override
    public void start() throws Exception
    {
        this.resumeScheduler();
        // setup the ticker thread
        if (this.ticker == null)
        {
            this.ticker = new Thread(new Ticker(), "WheelScheduler-Ticker");
            this.ticker.start();
        }
    }

    /**
     * Simply tick at a specific frequency
     */
    private class Ticker implements Runnable
    {
        public void run()
        {
            logger.debug("Ticker starting running: " + run + ", tick every " + tickPeriod + "ms, rotating every " + orangePeriod + "ms, currently " + jobs.size() + " jobs loaded");
            while (run)
            {
                long tickStart = System.currentTimeMillis();
                tick();
                long tickEnd = System.currentTimeMillis();
                long sleepDuration = tickPeriod - (tickEnd - tickStart);
                logger.trace("Tick took " + (tickEnd - tickStart) + "ms to run, sleeping for " + sleepDuration + "ms");
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
        public final Set<Job> jobs = new HashSet<Job>();

        public Segment()
        {
            super();
        }
    }

    /**
     * A scheduled job
     */
    private class Job
    {
        // details

        public volatile boolean enabled = true;

        public volatile long interval;

        public final UUID id;

        public volatile TimeRange timeRange;

        public final Runnable command;

        // state

        public long lastRunAt = 0L;

        public Job(UUID id, long interval, TimeRange timeRange, Runnable command)
        {
            super();
            this.id = id;
            this.interval = interval;
            this.timeRange = timeRange;
            this.command = command;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
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
            if (!getOuterType().equals(other.getOuterType())) return false;
            if (id == null)
            {
                if (other.id != null) return false;
            }
            else if (!id.equals(other.id)) return false;
            return true;
        }

        private WheelScheduler getOuterType()
        {
            return WheelScheduler.this;
        }
    }

    private class CheckRunner implements Runnable
    {
        public final Check check;

        public CheckRunner(Check check)
        {
            this.check = check;
        }

        public void run()
        {
            // fire off the check
            ExecuteCheck executeCheck = this.check.createExecuteCheck();
            if (executeCheck != null)
            {
                getBergamot().getManifold().publish(executeCheck);
            }
        }
    }
}

package com.intrbiz.bergamot.scheduler;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.timeperiod.TimeRange;

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
 * By default the wheel has 60 segments ticking at 1Hz. As such the 
 * wheel rotates once every minute.
 * 
 * Jobs are balanced over the segments within the wheel when they 
 * are first scheduled. As such, the first execution of a job is at 
 * most rotationPeriod.
 * 
 * Note: this scheduler is an approximating scheduler. It can never 
 * be more accurate than the tickPeriod. Currently this implementation 
 * can never be more accurate than the rotation period.
 * 
 * TODO list: 
 * 1. Support intervals < rotationPeriod
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

    private ConcurrentMap<UUID, Job> jobs;

    private Segment[] orange;

    private long tickPeriod;

    private long orangePeriod;

    private volatile int tick = -1;

    private volatile long tickTime = System.currentTimeMillis();

    private volatile Calendar tickCalendar = Calendar.getInstance();

    // initial delay allocation

    private SecureRandom initialDelay = new SecureRandom();
    

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
        this.jobs = new ConcurrentHashMap<UUID, Job>();
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
        for (Job job : current.jobs.values())
        {
            if (job.enabled)
            {
                logger.trace("Job " + job.id + " expires at " + job.expires + " <= " + this.tickTime);
                if (job.expires <= this.tickTime)
                {
                    // compute the next expiry time
                    job.lastExpires = job.expires;
                    job.expires = job.lastExpires + job.interval;
                    // check the time period
                    if (job.timeRange == null || this.isInTimeRange(job.timeRange, this.tickCalendar))
                    {
                        logger.trace("Job " + job.id + " expired, executing. Next expires at " + job.expires);
                        this.runJob(job);
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

    protected void scheduleJob(UUID id, long interval, long initialDelay, TimeRange timeRange, Runnable command)
    {
        if (!this.jobs.containsKey(id))
        {
            interval = this.validateInterval(interval);
            // the job
            Job job = new Job(id, interval, initialDelay, timeRange, command);
            this.jobs.put(job.id, job);
            // pick the segment based on the initial delay
            int segmentIdx = ((int) ((initialDelay / this.tickPeriod) % this.orange.length));
            logger.trace("Adding job " + job.id + " to segment: " + segmentIdx + " with interval " + interval + "ms and initial delay " + initialDelay + "ms");
            Segment segment = this.orange[segmentIdx];
            segment.jobs.put(job.id, job);
        }
        else
        {
            logger.debug("Job " + id + " is already scheduled, not adding it again.");
        }
    }

    protected void rescheduleJob(UUID id, long newInterval, TimeRange timeRange)
    {
        newInterval = this.validateInterval(newInterval);
        Job job = this.jobs.get(id);
        if (job != null)
        {
            job.interval = newInterval;
            job.timeRange = timeRange;
            // compute the new expiry
            job.expires = job.lastExpires + newInterval;
            job.enabled = true;
            logger.trace("Rescheduled job " + job.id + ", new expiry: " + job.expires);
        }
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

    protected long validateInterval(long interval)
    {
        if (interval < this.orangePeriod)
        {
            logger.warn("Currently jobs cannot be scheduled more frequently than every " + this.orangePeriod + " ms, rounding up.");
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
    public void enable(ActiveCheck<?,?> check)
    {
        this.enableJob(check.getId());
    }

    @Override
    public void disable(ActiveCheck<?,?> check)
    {
        this.disableJob(check.getId());
    }

    @Override
    public void schedule(ActiveCheck<?,?> check)
    {
        // randomly distribute the initial delay
        long initialDelay = (long) (this.initialDelay.nextDouble() * ((double) check.getCurrentInterval()));
        logger.info("Scheduling " + check + " with interval " + check.getCurrentInterval() + " and initial delay " + initialDelay);
        this.scheduleJob(check.getId(), check.getCurrentInterval(), initialDelay, check.getTimePeriod(), new CheckRunner(check));
    }

    @Override
    public void reschedule(ActiveCheck<?,?> check)
    {
        logger.info("Rescheduling " + check + " with interval " + check.getCurrentInterval());
        this.rescheduleJob(check.getId(), check.getCurrentInterval(), check.getTimePeriod());
    }

    @Override
    public void start() throws Exception
    {
        super.start();
        // ensure that we are ready to run
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
        public final ConcurrentMap<UUID, Job> jobs = new ConcurrentHashMap<UUID, Job>();

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

        public final UUID id;

        public final Runnable command;

        public volatile boolean enabled = true;

        public volatile long interval;

        public volatile TimeRange timeRange;

        public volatile long expires;

        public volatile long lastExpires;

        public Job(UUID id, long interval, long initialDelay, TimeRange timeRange, Runnable command)
        {
            super();
            this.id = id;
            this.interval = interval;
            this.timeRange = timeRange;
            this.command = command;
            // compute the expiry time
            this.lastExpires = this.expires = System.currentTimeMillis() + initialDelay;
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
                WheelScheduler.this.publishExecuteCheck(executeCheck, this.check.getRoutingKey(), this.check.getMessageTTL());
            }
        }
    }
}

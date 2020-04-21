package com.intrbiz.bergamot.cluster.consumer.hz;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.ringbuffer.Ringbuffer;
import com.hazelcast.ringbuffer.StaleSequenceException;

public abstract class HZBaseConsumer<T>
{   
    private static final Logger logger = Logger.getLogger(HZBaseConsumer.class);
    
    private static final int MIN_SIZE = 1;
    
    private static final int MAX_SIZE = 64;
    
    private static final Timer commitTimer = new Timer("consumer-commit-timer", true);
    
    private static final long COMMIT_INTERVAL_MS = 30_000L;
    
    private final HazelcastInstance hazelcast;
    
    private final UUID id;
    
    private final Ringbuffer<T> ringbuffer;
    
    private final IMap<UUID, Long> sequences;
    
    private final AtomicBoolean run = new AtomicBoolean(false);
    
    private volatile long sequence;
    
    private long lastCommitsequence;
    
    private TimerTask commitTask;
    
    protected HZBaseConsumer(HazelcastInstance hazelcast, UUID id, Function<UUID, String> ringbufferName, Supplier<String> sequenceMapName)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.id = id;
        this.ringbuffer = this.hazelcast.getRingbuffer(ringbufferName.apply(this.id));
        this.sequences = this.hazelcast.getMap(sequenceMapName.get());
        this.sequence = this.initSequence();
        this.lastCommitsequence = -1;
    }
    
    private long initSequence()
    {
        Long lks = this.sequences.get(this.id);
        return lks != null ? lks : this.ringbuffer.headSequence();
    }
    
    public UUID getId()
    {
        return this.id;
    }
    
    public long getSequence()
    {
        return this.sequence;
    }
    
    public long getTailSequence()
    {
        return this.ringbuffer.tailSequence();
    }
    
    public long getHeadSequence()
    {
        return this.ringbuffer.headSequence();
    }
    
    public boolean start(Consumer<T> consumer)
    {
        if (this.run.compareAndSet(false, true))
        {
            // Setup commit task
            this.commitTask = new TimerTask()
            {
                public void run()
                {
                    commit();
                }
            };
            commitTimer.schedule(this.commitTask, COMMIT_INTERVAL_MS, COMMIT_INTERVAL_MS);
            // Start consuming
            this.consume(consumer);
            return true;
        }
        return false;
    }
    
    private void commit()
    {
        try
        {
            if (this.sequence != this.lastCommitsequence)
            {
                // Commit the sequence
                long seq = this.sequence;
                this.sequences.putAsync(this.id, seq);
                this.lastCommitsequence = seq;
                if (logger.isDebugEnabled()) logger.debug("Committed sequence " + this.id + " " + this.sequence);
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to commit consumer position", e);
        }
    }
    
    private void consume(final Consumer<T> consumer)
    {
        if (logger.isTraceEnabled()) logger.trace("Reading from " + this.sequence);
        this.ringbuffer.readManyAsync(this.sequence, MIN_SIZE, MAX_SIZE, null).whenComplete((result, error) -> {
            try
            {
                if (result != null)
                {
                    // Process the messages
                    for (T message : result)
                    {
                        consumer.accept(message);
                    }
                    this.sequence = result.getNextSequenceToReadFrom();
                }
            }
            finally
            {
                if (this.run.get())
                {
                    if (error != null)
                    {
                        logger.warn("Error fetching batch", error);
                        if (error instanceof StaleSequenceException)
                        {
                            this.sequence = ((StaleSequenceException) error).getHeadSeq();
                        }
                    }
                    // Fetch the next batch
                    this.consume(consumer);
                }
            }
        });
    }
    
    public void stop()
    {
        if (this.run.compareAndSet(true, false))
        {
            this.commitTask.cancel();
            this.commitTask = null;
        }
    }
    
    public void drainTo(Consumer<T> consumer)
    {
        long tail = this.ringbuffer.tailSequence();
        logger.info("Draining ringbuffer " + this.id + " from=" + this.sequence + " to=" + tail);
        while (this.sequence < tail)
        {
            try
            {
                consumer.accept(this.ringbuffer.readOne(this.sequence));
                this.sequence++;
            }
            catch (StaleSequenceException sse)
            {
                this.sequence = sse.getHeadSeq();
            }
            catch (IllegalArgumentException iae)
            {
                break;
            }
            catch (InterruptedException e)
            {
            }
        }
    }
    
    public void destroy()
    {
        this.sequences.remove(this.id);
        this.ringbuffer.destroy();
    }
}

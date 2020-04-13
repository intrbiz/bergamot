package com.intrbiz.bergamot.executor;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.intrbiz.Util;
import com.intrbiz.bergamot.agent.AgentRegistrationService;
import com.intrbiz.bergamot.cluster.consumer.ProcessorConsumer;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;
import com.intrbiz.bergamot.model.message.processor.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.lamplighter.reading.ReadingProcessor;
import com.intrbiz.util.IBThreadFactory;

public class ProcessorExecutor
{
    private static final Logger logger = Logger.getLogger(ProcessorExecutor.class);
    
    private final ResultProcessor resultProcessor;
    
    private final ReadingProcessor readingProcessor;
    
    private final AgentRegistrationService agentRegistrationService;
    
    private final ProcessorConsumer processorConsumer;
    
    private final ExecutorService executor;
    
    private final AtomicBoolean run = new AtomicBoolean(false);
    
    private Thread runner;
    
    private CountDownLatch shutdownLatch;
    
    public ProcessorExecutor(ResultProcessor resultProcessor, ReadingProcessor readingProcessor, AgentRegistrationService agentRegistrationService, ProcessorConsumer processorConsumer)
    {
        super();
        this.resultProcessor = Objects.requireNonNull(resultProcessor);
        this.readingProcessor = Objects.requireNonNull(readingProcessor);
        this.agentRegistrationService = Objects.requireNonNull(agentRegistrationService);
        this.processorConsumer = Objects.requireNonNull(processorConsumer);
        // Thread pool to execute processing task in
        int threads = Integer.parseInt(Util.coalesceEmpty(System.getenv("PROCESSOR_THREADS"), System.getProperty("processor.threads"), String.valueOf(Runtime.getRuntime().availableProcessors() * 4)));
        logger.info("Using " + threads + " execution threads");
        this.executor = Executors.newFixedThreadPool(threads, new IBThreadFactory("bergamot-processor-executor", true));
    }
    
    public void start() throws Exception
    {
        if (this.run.compareAndSet(false, true))
        {
            this.shutdownLatch = new CountDownLatch(1);
            this.runner = new Thread(this::run, "bergamot-processor-executor");
            this.runner.start();
        }
    }

    protected void run()
    {
        logger.info("Processor " + this.processorConsumer.getId() + " executor starting.");
        try
        {
            while (this.run.get())
            {
                try
                {
                    ProcessorMessage message = this.processorConsumer.poll(5, TimeUnit.SECONDS);
                    if (message != null)
                    {
                        this.executor.execute(() -> {
                            this.processMessage(message);
                        });
                    }
                }
                catch (HazelcastInstanceNotActiveException e)
                {
                    logger.warn("Hazelcast no longer active, exiting processor executor.");
                    break;
                }
                catch (Exception e)
                {
                    logger.error("Error dispatching processor message", e);
                }
            }
        }
        finally
        {
            this.shutdownLatch.countDown();
        }
    }
    
    protected void processMessage(ProcessorMessage message)
    {
        try
        {
            if (message instanceof ResultMessage)
            {
                this.resultProcessor.process((ResultMessage) message);
            }
            else if (message instanceof ReadingParcelMO)
            {
                this.readingProcessor.process((ReadingParcelMO) message);
            }
            else if (message instanceof AgentMessage)
            {
                this.agentRegistrationService.process((AgentMessage) message);
            }
        }
        catch (Exception e)
        {
            logger.error("Error");
        }
    }
    
    public void stop()
    {
        if (this.run.compareAndSet(true, false))
        {
            try
            {
                this.shutdownLatch.await();
            }
            catch (InterruptedException e)
            {
            }
            this.runner = null;
            this.shutdownLatch = null;
        }
    }
}

package com.intrbiz.bergamot.executor;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

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
        this.processorConsumer.start(this::processMessage);
    }
    
    protected void processMessage(ProcessorMessage message)
    {
        if (message != null)
        {
            this.executor.execute(() -> {
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
                    logger.error("Error processing message", e);
                }
            });
        }
    }
    
    public void stop()
    {
        this.processorConsumer.stop();
    }
}

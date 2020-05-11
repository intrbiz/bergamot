package com.intrbiz.bergamot.executor;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.agent.AgentProcessorService;
import com.intrbiz.bergamot.cluster.consumer.ProcessorConsumer;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;
import com.intrbiz.bergamot.model.message.processor.agent.ProcessorAgentMessage;
import com.intrbiz.bergamot.model.message.processor.proxy.ProcessorProxyMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.proxy.ProxyProcessorService;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.lamplighter.reading.ReadingProcessor;
import com.intrbiz.util.IBThreadFactory;

public class ProcessorExecutor
{
    private static final Logger logger = Logger.getLogger(ProcessorExecutor.class);
    
    private final ResultProcessor resultProcessor;
    
    private final ReadingProcessor readingProcessor;
    
    private final AgentProcessorService agentProcessorService;
    
    private final ProxyProcessorService proxyProcessorService;
    
    private final ProcessorConsumer processorConsumer;
    
    private final ExecutorService executor;
    
    public ProcessorExecutor(ResultProcessor resultProcessor, ReadingProcessor readingProcessor, AgentProcessorService agentProcessorService, ProxyProcessorService proxyProcessorService, ProcessorConsumer processorConsumer)
    {
        super();
        this.resultProcessor = Objects.requireNonNull(resultProcessor);
        this.readingProcessor = Objects.requireNonNull(readingProcessor);
        this.agentProcessorService = Objects.requireNonNull(agentProcessorService);
        this.proxyProcessorService = Objects.requireNonNull(proxyProcessorService);
        this.processorConsumer = Objects.requireNonNull(processorConsumer);
        // Thread pool to execute processing task in
        int threads = Integer.parseInt(Util.coalesceEmpty(System.getenv("PROCESSOR_THREADS"), System.getProperty("processor.threads"), String.valueOf(Runtime.getRuntime().availableProcessors() * 4)));
        logger.info("Using " + threads + " execution threads");
        this.executor = Executors.newFixedThreadPool(threads, new IBThreadFactory("bergamot-processor-executor", true));
    }
    
    public void start() throws Exception
    {
        this.processorConsumer.start(this.executor, this::processMessage);
    }
    
    protected void processMessage(ProcessorMessage message)
    {
        if (message != null)
        {
            try
            {
                if (message instanceof ResultMessage)
                {
                    this.resultProcessor.process((ResultMessage) message);
                }
                else if (message instanceof ReadingParcelMessage)
                {
                    this.readingProcessor.process((ReadingParcelMessage) message);
                }
                else if (message instanceof ProcessorAgentMessage)
                {
                    this.agentProcessorService.process((ProcessorAgentMessage) message);
                }
                else if (message instanceof ProcessorProxyMessage)
                {
                    this.proxyProcessorService.process((ProcessorProxyMessage) message);
                }
            }
            catch (Exception e)
            {
                logger.error("Error processing message", e);
            }
        }
    }
    
    public void stop()
    {
        this.processorConsumer.stop();
    }
}

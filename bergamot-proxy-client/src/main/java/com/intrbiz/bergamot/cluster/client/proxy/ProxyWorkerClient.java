package com.intrbiz.bergamot.cluster.client.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.intrbiz.bergamot.cluster.client.WorkerClient;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.config.ClusterCfg;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;
import com.intrbiz.bergamot.model.message.processor.agent.ProcessorAgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.model.message.proxy.AgentState;
import com.intrbiz.bergamot.model.message.worker.WorkerMessage;
import com.intrbiz.bergamot.proxy.model.ClientHeader;

/**
 * A Bergamot Worker proxy client
 */
public class ProxyWorkerClient extends ProxyBaseClient implements WorkerClient
{
    private final ProxyWorkerConsumer consumer;
    
    private final ProxyProcessorDispatcher dispatcher;
    
    public ProxyWorkerClient(ClusterCfg config, Consumer<Void> onPanic, String application, String info, String workerPool, Set<String> availableEngines) throws Exception
    {
        super(config, onPanic, new ClientHeader().userAgent(application).info(info).proxyForWorker().workerPool(workerPool).engines(availableEngines));
        this.consumer = new ProxyWorkerConsumer();
        this.dispatcher = new ProxyProcessorDispatcher();
    }

    @Override
    protected void handleMessage(Message message)
    {
        if (message instanceof WorkerMessage)
        {
            this.consumer.processMessage((WorkerMessage) message);
        }
    }

    public WorkerConsumer getWorkerConsumer()
    {
        return this.consumer;
    }

    public ProcessorDispatcher getProcessorDispatcher()
    {
        return this.dispatcher;
    }

    public void registerAgent(UUID agentId) throws Exception
    {
        this.channel.writeAndFlush(new AgentState(agentId, true));
    }
    
    public void unregisterAgent(UUID agentId) throws Exception
    {
        this.channel.writeAndFlush(new AgentState(agentId, false));
    }
    
    private class ProxyWorkerConsumer implements WorkerConsumer
    {
        private volatile List<WorkerMessage> buffer;
        
        private volatile Executor executor;
        
        private volatile Consumer<WorkerMessage> consumer;
        
        void processMessage(WorkerMessage check)
        {
            synchronized (this)
            {
                if (this.consumer != null && this.executor != null)
                {
                    this.executor.execute(() -> {
                        this.consumer.accept(check);
                    });
                }
                else
                {
                    if (this.buffer == null) this.buffer = new ArrayList<>();
                    this.buffer.add(check);
                }
            }
        }
        
        @Override
        public UUID getId()
        {
            return id;
        }

        @Override
        public long getSequence()
        {
            return 0;
        }

        @Override
        public long getTailSequence()
        {
            return 0;
        }

        @Override
        public long getHeadSequence()
        {
            return 0;
        }

        @Override
        public boolean start(Executor executor, Consumer<WorkerMessage> consumer)
        {
            synchronized (this)
            {
                this.executor = executor;
                this.consumer = consumer;
                if (this.buffer != null)
                {
                    for (WorkerMessage message : this.buffer)
                    {
                        this.executor.execute(() -> {
                            consumer.accept(message);
                        });
                    }
                    this.buffer = null;
                }
            }
            return true;
        }

        @Override
        public void stop()
        {
        }

        @Override
        public void drainTo(Consumer<WorkerMessage> consumer)
        {
        }

        @Override
        public void destroy()
        {
        }
    }
    
    private class ProxyProcessorDispatcher implements ProcessorDispatcher
    {
        @Override
        public PublishStatus dispatch(ProcessorMessage message)
        {
            channel.writeAndFlush(message);
            return PublishStatus.Success;
        }

        @Override
        public PublishStatus dispatchResult(ResultMessage result)
        {
            return this.dispatch(result);
        }

        @Override
        public PublishStatus dispatchReading(ReadingParcelMessage reading)
        {
            return this.dispatch(reading);
        }

        @Override
        public PublishStatus dispatchAgentMessage(ProcessorAgentMessage action)
        {
            return this.dispatch(action);
        }
    }
}

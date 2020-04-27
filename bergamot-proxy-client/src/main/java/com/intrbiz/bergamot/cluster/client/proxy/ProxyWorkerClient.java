package com.intrbiz.bergamot.cluster.client.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import com.intrbiz.bergamot.cluster.client.WorkerClient;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyLookup;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.config.ClusterCfg;
import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;
import com.intrbiz.bergamot.model.message.processor.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.model.message.proxy.AgentState;
import com.intrbiz.bergamot.model.message.proxy.FoundAgentKey;
import com.intrbiz.bergamot.model.message.proxy.LookupAgentKey;
import com.intrbiz.bergamot.proxy.model.ClientHeader;

/**
 * A Bergamot Worker proxy client
 */
public class ProxyWorkerClient extends ProxyBaseClient implements WorkerClient
{
    private final ProxyWorkerConsumer consumer;
    
    private final ProxyProcessorDispatcher dispatcher;
    
    private final ProxyAgentKeyLookup lookup;
    
    public ProxyWorkerClient(ClusterCfg config, Consumer<Void> onPanic, String info, String workerPool, Set<String> availableEngines) throws Exception
    {
        super(config, onPanic, new ClientHeader().userAgent(info).proxyForWorker().workerPool(workerPool).engines(availableEngines));
        this.consumer = new ProxyWorkerConsumer();
        this.dispatcher = new ProxyProcessorDispatcher();
        this.lookup = new ProxyAgentKeyLookup();
    }

    @Override
    protected void handleMessage(Message message)
    {
        if (message instanceof ExecuteCheck)
        {
            this.consumer.processMessage((ExecuteCheck) message);
        }
        else if (message instanceof FoundAgentKey)
        {
            this.lookup.processMessage((FoundAgentKey) message);
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
    
    public AgentKeyLookup getAgentKeyLookup()
    {
        return this.lookup;
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
        private volatile List<ExecuteCheck> buffer;
        
        private volatile Consumer<ExecuteCheck> consumer;
        
        void processMessage(ExecuteCheck check)
        {
            synchronized (this)
            {
                if (this.consumer != null)
                {
                    this.consumer.accept(check);
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
        public boolean start(Consumer<ExecuteCheck> consumer)
        {
            synchronized (this)
            {
                this.consumer = consumer;
                if (this.buffer != null)
                {
                    for (ExecuteCheck check : this.buffer)
                    {
                        consumer.accept(check);
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
        public void drainTo(Consumer<ExecuteCheck> consumer)
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
        public PublishStatus dispatch(UUID processor, ProcessorMessage message)
        {
            return this.dispatch(message);
        }

        @Override
        public PublishStatus dispatchResult(ResultMessage result)
        {
            return this.dispatch(result);
        }

        @Override
        public PublishStatus dispatchResult(UUID processor, ResultMessage result)
        {
            return this.dispatch(result);
        }

        @Override
        public PublishStatus dispatchReading(ReadingParcelMO reading)
        {
            return this.dispatch(reading);
        }

        @Override
        public PublishStatus dispatchReading(UUID processor, ReadingParcelMO reading)
        {
            return this.dispatch(reading);
        }

        @Override
        public PublishStatus dispatchAgentMessage(AgentMessage action)
        {
            return this.dispatch(action);
        }
    }
    
    private class ProxyAgentKeyLookup implements AgentKeyLookup
    {
        private final ConcurrentMap<UUID, Consumer<AgentAuthenticationKey>> callbacks = new ConcurrentHashMap<>();
        
        private void processMessage(FoundAgentKey response)
        {
            Consumer<AgentAuthenticationKey> callback = this.callbacks.remove(response.getReplyTo());
            if (callback != null)
            {
                callback.accept(new AgentAuthenticationKey(response.getKey()));
            }
        }
        
        @Override
        public void lookupAgentKey(UUID keyId, Consumer<AgentAuthenticationKey> callback)
        {
            LookupAgentKey lookup = new LookupAgentKey(keyId);
            this.callbacks.put(lookup.getId(), callback);
            channel.writeAndFlush(lookup);
        }
    }
}

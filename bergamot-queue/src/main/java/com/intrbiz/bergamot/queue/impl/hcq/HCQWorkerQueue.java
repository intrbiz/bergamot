package com.intrbiz.bergamot.queue.impl.hcq;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.AdhocResultKey;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.queue.key.WorkerKey;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.hcq.client.HCQBatch;
import com.intrbiz.hcq.client.HCQClient;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.hcq.HCQConsumer;
import com.intrbiz.queue.hcq.HCQPool;
import com.intrbiz.queue.hcq.HCQProducer;
import com.intrbiz.queue.name.NullKey;

public class HCQWorkerQueue extends WorkerQueue
{
    public static final int QUEUE_SIZE = 100;
    
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(WorkerQueue.class, HCQPool.TYPE, HCQWorkerQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<HCQClient> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public HCQWorkerQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<HCQClient>) broker;
    }

    public String getName()
    {
        return "worker-queue";
    }

    @Override
    public RoutedProducer<ExecuteCheck, WorkerKey> publishChecks(WorkerKey defaultKey)
    {
        return new HCQProducer<ExecuteCheck, WorkerKey>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), defaultKey, this.source.getRegistry().timer("publish-check"))
        {
            protected String setupExchange(HCQBatch on) throws Exception
            {
                // the dead check queue
                on.getOrCreateQueue("bergamot.dead_check_queue", QUEUE_SIZE, false);
                on.getOrCreateExchange("bergamot.dead_check", "fanout");
                on.bindQueueToExchange("bergamot.dead_check", "", "bergamot.dead_check_queue");
                // the dead agent check queue
                on.getOrCreateQueue("bergamot.dead_agent_check_queue", QUEUE_SIZE, false);
                on.getOrCreateExchange("bergamot.dead_agent_check", "fanout");
                on.bindQueueToExchange("bergamot.dead_agent_check", "", "bergamot.dead_agent_check_queue");
                // common exchanges
                on.getOrCreateExchange("bergamot.check", "topic");
                // default exchanges
                on.getOrCreateExchange("bergamot.check.site.default", "topic");
                on.getOrCreateExchange("bergamot.check.worker_pool.default.any", "topic");
                // alternate exchange bindings
                on.bindAlternateExchange("bergamot.check", "bergamot.check.site.default");
                on.bindAlternateExchange("bergamot.check.site.default", "bergamot.check.worker_pool.default.any");
                on.bindAlternateExchange("bergamot.check.worker_pool.default.any", "bergamot.dead_check");
                //
                return "bergamot.check";
            }
        };
    }

    @Override
    public Consumer<ExecuteCheck, WorkerKey> consumeChecks(DeliveryHandler<ExecuteCheck> handler, UUID site, String theWorkerPool, String engine, boolean agentRouting)
    {
        // validate arguments
        final String workerPool = Util.isEmpty(theWorkerPool) ? null : theWorkerPool;
        if (Util.isEmpty(engine)) throw new IllegalArgumentException("Engine name must be given!");
        // the engine exchange
        final String engineExchangeName = "bergamot.check.engine." + Util.coalesce(site, "default") + "." + Util.coalesce(workerPool, "any") + "." + engine;
        // create the consumer
        return new HCQConsumer<ExecuteCheck, WorkerKey>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), handler, this.source.getRegistry().timer("consume-check"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                // the dead check queue
                on.getOrCreateQueue("bergamot.dead_check_queue", QUEUE_SIZE, false);
                on.getOrCreateExchange("bergamot.dead_check", "fanout");
                on.bindQueueToExchange("bergamot.dead_check", "", "bergamot.dead_check_queue");
                // the dead agent check queue
                on.getOrCreateQueue("bergamot.dead_agent_check_queue", QUEUE_SIZE, false);
                on.getOrCreateExchange("bergamot.dead_agent_check", "fanout");
                on.bindQueueToExchange("bergamot.dead_agent_check", "", "bergamot.dead_agent_check_queue");
                // setup our worker queue
                String queueName = null;
                if (agentRouting)
                {
                    // for agent routed check, we use a transient queue
                    queueName = "bergamot.check." + Util.coalesce(site, "default") + "." + Util.coalesceEmpty(workerPool, "any") + "." + engine + "." + UUID.randomUUID();
                    on.getOrCreateTempQueue(queueName, QUEUE_SIZE);
                    // TODO: on.getOrCreateQueue(queueName, false, true, true, args("x-dead-letter-exchange", "bergamot.dead_agent_check"));
                }
                else
                {
                    // for non agent routed checks we use a shared queue
                    queueName = "bergamot.check." + Util.coalesce(site, "default") + "." + Util.coalesceEmpty(workerPool, "any") + "." + engine;
                    on.getOrCreateQueue(queueName, QUEUE_SIZE, false);
                    // TODO: on.getOrCreateQueue(queueName, true, false, false, args("x-dead-letter-exchange", "bergamot.dead_check"));
                }
                // common exchanges
                on.getOrCreateExchange("bergamot.check", "topic");
                // default exchanges
                on.getOrCreateExchange("bergamot.check.site.default", "topic");
                on.getOrCreateExchange("bergamot.check.worker_pool.default.any", "topic");
                // alternate exchange bindings
                on.bindAlternateExchange("bergamot.check", "bergamot.check.site.default");
                on.bindAlternateExchange("bergamot.check.site.default", "bergamot.check.worker_pool.default.any");
                on.bindAlternateExchange("bergamot.check.worker_pool.default.any", "bergamot.dead_check");
                // site default exchanges
                if (site != null)
                {
                    on.getOrCreateExchange("bergamot.check.site." + site, "topic");
                    on.getOrCreateExchange("bergamot.check.worker_pool." + site + ".any", "topic");
                    // alternates
                    on.bindAlternateExchange("bergamot.check.site." + site, "bergamot.check.worker_pool." + site + ".any");
                    on.bindAlternateExchange("bergamot.check.worker_pool." + site + ".any", "bergamot.dead_check");
                    // bind the site to check exchange
                    on.bindExchangeToExchange("bergamot.check", site + ".*.*.*", "bergamot.check.site." + site);
                }
                // worker pool default exchanges
                if (workerPool != null)
                {
                    on.getOrCreateExchange("bergamot.check.worker_pool.default." + workerPool, "topic");
                    // alternates
                    on.bindAlternateExchange("bergamot.check.worker_pool.default." + workerPool, "bergamot.dead_check");
                    // bind the worker pool to default site exchange
                    // TODO: is this right?
                    on.bindExchangeToExchange("bergamot.check.worker_pool.default." + workerPool, "*." + workerPool + ".*.*", "bergamot.check.site.default");
                }
                // specific exchanges
                if (site != null && workerPool != null)
                {
                    on.getOrCreateExchange("bergamot.check.worker_pool." + site + "." + workerPool, "topic");
                    // alternates
                    on.bindAlternateExchange("bergamot.check.worker_pool." + site + "." + workerPool, "bergamot.dead_check");
                    // bind the worker pool exchange to the site exchange
                    on.bindExchangeToExchange("bergamot.check.site." + site, "*." + workerPool + ".*.*", "bergamot.check.worker_pool." + site + "." + workerPool);
                }
                // declare the engine exchange
                // the engine exchange dead checks to the dead agent check exchange
                on.getOrCreateExchange(engineExchangeName, "topic");
                on.bindAlternateExchange(engineExchangeName, "bergamot.dead_agent_check");
                // bind the engine exchange to our worker pool
                on.bindExchangeToExchange("bergamot.check.worker_pool." + Util.coalesce(site, "default") + "." + Util.coalesce(workerPool, "any"), "*.*." + engine + ".*", engineExchangeName);                
                // bind our queue to the engine exchange
                if (! agentRouting)
                {
                    // for agent routing we bind when an agent connects rather than now
                    on.bindQueueToExchange(engineExchangeName, "*.*.*.*", queueName);
                }
                return queueName;
            }
            
            @Override
            protected void addQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.bindQueueToExchange(engineExchangeName, binding, this.queue);
            }
            
            @Override
            protected void removeQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.unbindQueueToExchange(engineExchangeName, binding, this.queue);
            }
        };
    }

    @Override
    public Consumer<ExecuteCheck, NullKey> consumeDeadChecks(DeliveryHandler<ExecuteCheck> handler)
    {
        return new HCQConsumer<ExecuteCheck, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), handler, this.source.getRegistry().timer("consume-dead-check"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                on.getOrCreateQueue("bergamot.dead_check_queue", QUEUE_SIZE, false);
                on.getOrCreateExchange("bergamot.dead_check", "fanout");
                on.bindQueueToExchange("bergamot.dead_check", "", "bergamot.dead_check_queue");
                return "bergamot.dead_check_queue";
            }
        };
    }
    
    @Override
    public Consumer<ExecuteCheck, NullKey> consumeDeadAgentChecks(DeliveryHandler<ExecuteCheck> handler)
    {
        return new HCQConsumer<ExecuteCheck, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), handler, this.source.getRegistry().timer("consume-dead-check"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                on.getOrCreateQueue("bergamot.dead_agent_check_queue", QUEUE_SIZE, false);
                on.getOrCreateExchange("bergamot.dead_agent_check", "fanout");
                on.bindQueueToExchange("bergamot.dead_agent_check", "", "bergamot.dead_agent_check_queue");
                return "bergamot.dead_agent_check_queue";
            }
        };
    }
    
    // results

    @Override
    public RoutedProducer<ResultMO, ResultKey> publishResults(ResultKey defaultKey)
    {
        return new HCQProducer<ResultMO, ResultKey>(this.broker, this.transcoder.asQueueEventTranscoder(ResultMO.class), defaultKey, this.source.getRegistry().timer("publish-result"))
        {
            protected String setupExchange(HCQBatch on) throws IOException
            {
                // the fallback queue
                on.getOrCreateQueue("bergamot.result.fallback_queue", QUEUE_SIZE, false);
                on.getOrCreateExchange("bergamot.result.fallback", "fanout");
                on.bindQueueToExchange("bergamot.result.fallback", "", "bergamot.result.fallback_queue");
                // the result exchange
                on.getOrCreateExchange("bergamot.result", "topic");
                on.bindAlternateExchange("bergamot.result", "bergamot.result.fallback");
                return "bergamot.result";
            }
        };
    }

    @Override
    public Consumer<ResultMO, ResultKey> consumeResults(DeliveryHandler<ResultMO> handler, String instance)
    {
        return new HCQConsumer<ResultMO, ResultKey>(this.broker, this.transcoder.asQueueEventTranscoder(ResultMO.class), handler, this.source.getRegistry().timer("consume-result"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                // the fallback queue
                on.getOrCreateQueue("bergamot.result.fallback_queue", QUEUE_SIZE, false);
                on.getOrCreateExchange("bergamot.result.fallback", "fanout");
                on.bindQueueToExchange("bergamot.result.fallback", "", "bergamot.result.fallback_queue");
                // Use a transient queue since
                // processing duties could be moved at any time
                String queueName = "bergamot.result.processor." + instance;
                on.getOrCreateTempQueue(queueName, QUEUE_SIZE);
                // the result exchange
                on.getOrCreateExchange("bergamot.result", "topic");
                on.bindAlternateExchange("bergamot.result", "bergamot.result.fallback");
                return queueName;
            }

            @Override
            protected void addQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.bindQueueToExchange("bergamot.result", binding, this.queue);
            }

            @Override
            protected void removeQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.unbindQueueToExchange("bergamot.result", binding, this.queue);
            }
        };
    }
    
    @Override
    public Consumer<ResultMO, ResultKey> consumeFallbackResults(DeliveryHandler<ResultMO> handler)
    {
        return new HCQConsumer<ResultMO, ResultKey>(this.broker, this.transcoder.asQueueEventTranscoder(ResultMO.class), handler, this.source.getRegistry().timer("consume-fallback-result"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                on.getOrCreateQueue("bergamot.result.fallback_queue", QUEUE_SIZE, false);
                on.getOrCreateExchange("bergamot.result.fallback", "fanout");
                on.bindQueueToExchange("bergamot.result.fallback", "", "bergamot.result.fallback_queue");
                return "bergamot.result.fallback_queue";
            }
        };
    }
    
    // readings
    
    @Override
    public RoutedProducer<ReadingParcelMO, ReadingKey> publishReadings(ReadingKey defaultKey)
    {
        return new HCQProducer<ReadingParcelMO, ReadingKey>(this.broker, this.transcoder.asQueueEventTranscoder(ReadingParcelMO.class), defaultKey, this.source.getRegistry().timer("publish-reading"))
        {
            protected String setupExchange(HCQBatch on) throws IOException
            {
                // the fallback queue
                on.getOrCreateQueue("bergamot.reading.fallback_queue", QUEUE_SIZE, false);
                on.getOrCreateExchange("bergamot.reading.fallback", "fanout");
                on.bindQueueToExchange("bergamot.reading.fallback", "", "bergamot.reading.fallback_queue");
                // the result exchange
                on.getOrCreateExchange("bergamot.reading", "topic");
                on.bindAlternateExchange("bergamot.reading", "bergamot.reading.fallback");
                return "bergamot.reading";
            }
        };
    }
    
    @Override
    public Consumer<ReadingParcelMO, ReadingKey> consumeReadings(DeliveryHandler<ReadingParcelMO> handler, String instance)
    {
        return new HCQConsumer<ReadingParcelMO, ReadingKey>(this.broker, this.transcoder.asQueueEventTranscoder(ReadingParcelMO.class), handler, this.source.getRegistry().timer("consume-reading"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                // the fallback queue
                on.getOrCreateQueue("bergamot.reading.fallback_queue", QUEUE_SIZE, false);
                on.getOrCreateExchange("bergamot.reading.fallback", "fanout");
                on.bindQueueToExchange("bergamot.reading.fallback", "", "bergamot.reading.fallback_queue");
                // Use a transient queue since
                // processing duties could be moved at any time
                String queueName = "bergamot.reading.processor." + instance;
                on.getOrCreateTempQueue(queueName, QUEUE_SIZE);
                // the result exchange
                on.getOrCreateExchange("bergamot.reading", "topic");
                on.bindAlternateExchange("bergamot.reading", "bergamot.reading.fallback");
                return queueName;
            }

            @Override
            protected void addQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.bindQueueToExchange("bergamot.reading", binding, this.queue);
            }

            @Override
            protected void removeQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.unbindQueueToExchange("bergamot.reading", binding, this.queue);
            }
        };
    }
    
    @Override
    public Consumer<ReadingParcelMO, ReadingKey> consumeFallbackReadings(DeliveryHandler<ReadingParcelMO> handler)
    {
        return new HCQConsumer<ReadingParcelMO, ReadingKey>(this.broker, this.transcoder.asQueueEventTranscoder(ReadingParcelMO.class), handler, this.source.getRegistry().timer("consume-fallback-reading"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.reading.fallback", "fanout");
                on.getOrCreateQueue("bergamot.reading.fallback_queue", QUEUE_SIZE, false);
                on.bindQueueToExchange("bergamot.reading.fallback", "", "bergamot.reading.fallback_queue");
                return "bergamot.reading.fallback_queue";
            }
        };
    }
    
    // adhoc
    
    @Override
    public RoutedProducer<ResultMO, AdhocResultKey> publishAdhocResults()
    {
        return new HCQProducer<ResultMO, AdhocResultKey>(this.broker, this.transcoder.asQueueEventTranscoder(ResultMO.class), null, this.source.getRegistry().timer("publish-result"))
        {
            protected String setupExchange(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.adhocresult", "topic");
                return "bergamot.adhocresult";
            }
        };
    }

    @Override
    public Consumer<ResultMO, AdhocResultKey> consumeAdhocResults(UUID adhocId, DeliveryHandler<ResultMO> handler)
    {
        return new HCQConsumer<ResultMO, AdhocResultKey>(this.broker, this.transcoder.asQueueEventTranscoder(ResultMO.class), handler, this.source.getRegistry().timer("consume-result"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.adhocresult", "topic");
                String queueName = "adhoc-result-" + UUID.randomUUID();
                on.getOrCreateTempQueue(queueName, QUEUE_SIZE);
                on.bindQueueToExchange("bergamot.adhocresult", new AdhocResultKey(adhocId).toString(), queueName);
                return queueName;
            }
        };
    }

    @Override
    public void close()
    {
    }
}

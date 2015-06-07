package com.intrbiz.bergamot.queue.impl;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.queue.key.WorkerKey;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.NullKey;
import com.intrbiz.queue.rabbit.RabbitConsumer;
import com.intrbiz.queue.rabbit.RabbitProducer;
import com.rabbitmq.client.Channel;

public class RabbitWorkerQueue extends WorkerQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(WorkerQueue.class, RabbitWorkerQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<Channel> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public RabbitWorkerQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<Channel>) broker;
    }

    public String getName()
    {
        return "worker-queue";
    }

    @Override
    public RoutedProducer<ExecuteCheck, WorkerKey> publishChecks(WorkerKey defaultKey)
    {
        return new RabbitProducer<ExecuteCheck, WorkerKey>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), defaultKey, this.source.getRegistry().timer("publish-check"))
        {
            protected String setupExchange(Channel on) throws IOException
            {
                on.exchangeDeclare("bergamot.check", "topic", true, false, args("alternate-exchange", "bergamot.check.site.default"));
                return "bergamot.check";
            }
        };
    }

    @Override
    public Consumer<ExecuteCheck, WorkerKey> consumeChecks(DeliveryHandler<ExecuteCheck> handler, UUID site, String theWorkerPool, String engine, boolean agentRouting, UUID workerId)
    {
        // validate arguments
        final String workerPool = Util.isEmpty(theWorkerPool) ? null : theWorkerPool;
        if (Util.isEmpty(engine)) throw new IllegalArgumentException("Engine name must be given!");
        if (agentRouting && workerId == null) throw new IllegalArgumentException("For agent routing an worker id must be given");
        // the engine exchange
        final String engineExchangeName = "bergamot.check.engine." + Util.coalesce(site, "default") + "." + Util.coalesce(workerPool, "any") + "." + engine;
        // create the consumer
        return new RabbitConsumer<ExecuteCheck, WorkerKey>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), handler, this.source.getRegistry().timer("consume-check"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                // the dead check queue
                on.queueDeclare("bergamot.dead_check_queue", true, false, false, null);
                on.exchangeDeclare("bergamot.dead_check", "fanout", true);
                on.queueBind("bergamot.dead_check_queue", "bergamot.dead_check", "");
                // the dead agent check queue
                on.queueDeclare("bergamot.dead_agent_check_queue", true, false, false, null);
                on.exchangeDeclare("bergamot.dead_agent_check", "fanout", true);
                on.queueBind("bergamot.dead_agent_check_queue", "bergamot.dead_agent_check", "");
                // setup our worker queue
                String queueName = null;
                if (agentRouting)
                {
                    // for agent routed check, we use a transient queue
                    queueName = "bergamot.check." + Util.coalesce(site, "default") + "." + Util.coalesceEmpty(workerPool, "any") + "." + engine + "." + workerId;
                    on.queueDeclare(queueName, false, true, true, args("x-dead-letter-exchange", "bergamot.dead_check"));
                }
                else
                {
                    // for non agent routed checks we use a shared queue
                    queueName = "bergamot.check." + Util.coalesce(site, "default") + "." + Util.coalesceEmpty(workerPool, "any") + "." + engine;
                    on.queueDeclare(queueName, true, false, false, args("x-dead-letter-exchange", "bergamot.dead_check"));
                }
                // common exchanges
                on.exchangeDeclare("bergamot.check", "topic", true, false, args("alternate-exchange", "bergamot.check.site.default"));
                // default exchanges
                on.exchangeDeclare("bergamot.check.site.default", "topic", true, false, args("alternate-exchange", "bergamot.check.worker_pool.default.any"));
                on.exchangeDeclare("bergamot.check.worker_pool.default.any", "topic", true, false, args("alternate-exchange", "bergamot.dead_check"));
                // site default exchanges
                if (site != null)
                {
                    on.exchangeDeclare("bergamot.check.site." + site, "topic", true, false, args("alternate-exchange", "bergamot.check.worker_pool." + site + ".any"));
                    on.exchangeDeclare("bergamot.check.worker_pool." + site + ".any", "topic", true, false, args("alternate-exchange", "bergamot.dead_check"));
                    // bind the site to check exchange
                    on.exchangeBind("bergamot.check.site." + site, "bergamot.check", site + ".*.*.*");
                }
                // worker pool default exchanges
                if (workerPool != null)
                {
                    on.exchangeDeclare("bergamot.check.worker_pool.default." + workerPool, "topic", true, false, args("alternate-exchange", "bergamot.dead_check"));
                    // bind the worker pool to default site exchange
                    on.exchangeBind("bergamot.check.site.default", "bergamot.check.worker_pool.default." + workerPool, "*." + workerPool + ".*.*");
                }
                // specific exchanges
                if (site != null && workerPool != null)
                {
                    on.exchangeDeclare("bergamot.check.worker_pool." + site + "." + workerPool, "topic", true, false, args("alternate-exchange", "bergamot.dead_check"));
                    // bind the worker pool exchange to the site exchange
                    on.exchangeBind("bergamot.check.worker_pool." + site + "." + workerPool, "bergamot.check.site." + site, "*." + workerPool + ".*.*");
                }
                // declare the engine exchange
                // the engine exchange dead checks to the dead agent check exchange
                on.exchangeDeclare(engineExchangeName, "topic", true, false, args("alternate-exchange", "bergamot.dead_agent_check"));
                // bind the engine exchange to our worker pool
                on.exchangeBind(engineExchangeName, "bergamot.check.worker_pool." + Util.coalesce(site, "default") + "." + Util.coalesce(workerPool, "any"), "*.*." + engine + ".*");                
                // bind our queue to the engine exchange
                if (! agentRouting)
                {
                    // for agent routing we bind when an agent connects rather than now
                    on.queueBind(queueName, engineExchangeName, "*.*.*.*");
                }
                return queueName;
            }
            
            @Override
            protected void addQueueBinding(Channel on, String binding) throws IOException
            {
                on.queueBind(this.queue, engineExchangeName, binding);
            }
            
            @Override
            protected void removeQueueBinding(Channel on, String binding) throws IOException
            {
                on.queueUnbind(this.queue, engineExchangeName, binding);
            }
        };
    }

    @Override
    public Consumer<ExecuteCheck, NullKey> consumeDeadChecks(DeliveryHandler<ExecuteCheck> handler)
    {
        return new RabbitConsumer<ExecuteCheck, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), handler, this.source.getRegistry().timer("consume-dead-check"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                on.queueDeclare("bergamot.dead_check_queue", true, false, false, null);
                on.exchangeDeclare("bergamot.dead_check", "fanout", true);
                on.queueBind("bergamot.dead_check_queue", "bergamot.dead_check", "");
                return "bergamot.dead_check_queue";
            }
        };
    }
    
    @Override
    public Consumer<ExecuteCheck, NullKey> consumeDeadAgentChecks(DeliveryHandler<ExecuteCheck> handler)
    {
        return new RabbitConsumer<ExecuteCheck, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), handler, this.source.getRegistry().timer("consume-dead-check"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                on.queueDeclare("bergamot.dead_agent_check_queue", true, false, false, null);
                on.exchangeDeclare("bergamot.dead_agent_check", "fanout", true);
                on.queueBind("bergamot.dead_agent_check_queue", "bergamot.dead_agent_check", "");
                return "bergamot.dead_agent_check_queue";
            }
        };
    }
    
    // results

    @Override
    public RoutedProducer<ResultMO, ResultKey> publishResults(ResultKey defaultKey)
    {
        return new RabbitProducer<ResultMO, ResultKey>(this.broker, this.transcoder.asQueueEventTranscoder(ResultMO.class), defaultKey, this.source.getRegistry().timer("publish-result"))
        {
            protected String setupExchange(Channel on) throws IOException
            {
                // the fallback queue
                on.queueDeclare("bergamot.result.fallback_queue", true, false, false, null);
                on.exchangeDeclare("bergamot.result.fallback", "fanout", true, false, null);
                on.queueBind("bergamot.result.fallback_queue", "bergamot.result.fallback", "");
                // the result exchange
                on.exchangeDeclare("bergamot.result", "topic", true, false, args("alternate-exchange", "bergamot.result.fallback"));
                return "bergamot.result";
            }
        };
    }

    @Override
    public Consumer<ResultMO, ResultKey> consumeResults(DeliveryHandler<ResultMO> handler, String instance)
    {
        return new RabbitConsumer<ResultMO, ResultKey>(this.broker, this.transcoder.asQueueEventTranscoder(ResultMO.class), handler, this.source.getRegistry().timer("consume-result"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                // the fallback queue
                on.queueDeclare("bergamot.result.fallback_queue", true, false, false, null);
                on.exchangeDeclare("bergamot.result.fallback", "fanout", true, false, null);
                on.queueBind("bergamot.result.fallback_queue", "bergamot.result.fallback", "");
                // Use a transient queue since
                // processing duties could be moved at any time
                String queueName = "bergamot.result.processor." + instance;
                on.queueDeclare(queueName, false, true, true, null);
                // the result exchange
                on.exchangeDeclare("bergamot.result", "topic", true, false, args("alternate-exchange", "bergamot.result.fallback"));
                return queueName;
            }

            @Override
            protected void addQueueBinding(Channel on, String binding) throws IOException
            {
                on.queueBind(this.queue, "bergamot.result", binding);
            }

            @Override
            protected void removeQueueBinding(Channel on, String binding) throws IOException
            {
                // seems odd, but unbind is non-idempotent, binding then unbinding is a poor workaround
                on.queueBind(this.queue, "bergamot.result", binding);
                on.queueUnbind(this.queue, "bergamot.result", binding);
            }
        };
    }
    
    @Override
    public Consumer<ResultMO, ResultKey> consumeFallbackResults(DeliveryHandler<ResultMO> handler)
    {
        return new RabbitConsumer<ResultMO, ResultKey>(this.broker, this.transcoder.asQueueEventTranscoder(ResultMO.class), handler, this.source.getRegistry().timer("consume-fallback-result"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                on.queueDeclare("bergamot.result.fallback_queue", true, false, false, null);
                on.exchangeDeclare("bergamot.result.fallback", "fanout", true, false, null);
                on.queueBind("bergamot.result.fallback_queue", "bergamot.result.fallback", "");
                return "bergamot.result.fallback_queue";
            }
        };
    }
    
    // readings
    
    @Override
    public RoutedProducer<ReadingParcelMO, ReadingKey> publishReadings(ReadingKey defaultKey)
    {
        return new RabbitProducer<ReadingParcelMO, ReadingKey>(this.broker, this.transcoder.asQueueEventTranscoder(ReadingParcelMO.class), defaultKey, this.source.getRegistry().timer("publish-reading"))
        {
            protected String setupExchange(Channel on) throws IOException
            {
                // the fallback queue
                on.queueDeclare("bergamot.reading.fallback_queue", true, false, false, null);
                on.exchangeDeclare("bergamot.reading.fallback", "fanout", true, false, null);
                on.queueBind("bergamot.reading.fallback_queue", "bergamot.reading.fallback", "");
                // the result exchange
                on.exchangeDeclare("bergamot.reading", "topic", true, false, args("alternate-exchange", "bergamot.reading.fallback"));
                return "bergamot.reading";
            }
        };
    }
    
    @Override
    public Consumer<ReadingParcelMO, ReadingKey> consumeReadings(DeliveryHandler<ReadingParcelMO> handler, String instance)
    {
        return new RabbitConsumer<ReadingParcelMO, ReadingKey>(this.broker, this.transcoder.asQueueEventTranscoder(ReadingParcelMO.class), handler, this.source.getRegistry().timer("consume-reading"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                // the fallback queue
                on.queueDeclare("bergamot.reading.fallback_queue", true, false, false, null);
                on.exchangeDeclare("bergamot.reading.fallback", "fanout", true, false, null);
                on.queueBind("bergamot.reading.fallback_queue", "bergamot.reading.fallback", "");
                // Use a transient queue since
                // processing duties could be moved at any time
                String queueName = "bergamot.reading.processor." + instance;
                on.queueDeclare(queueName, false, true, true, null);
                // the result exchange
                on.exchangeDeclare("bergamot.reading", "topic", true, false, args("alternate-exchange", "bergamot.reading.fallback"));
                return queueName;
            }

            @Override
            protected void addQueueBinding(Channel on, String binding) throws IOException
            {
                on.queueBind(this.queue, "bergamot.reading", binding);
            }

            @Override
            protected void removeQueueBinding(Channel on, String binding) throws IOException
            {
                // seems odd, but unbind is non-idempotent, binding then unbinding is a poor workaround
                on.queueBind(this.queue, "bergamot.reading", binding);
                on.queueUnbind(this.queue, "bergamot.reading", binding);
            }
        };
    }
    
    @Override
    public Consumer<ReadingParcelMO, ReadingKey> consumeFallbackReadings(DeliveryHandler<ReadingParcelMO> handler)
    {
        return new RabbitConsumer<ReadingParcelMO, ReadingKey>(this.broker, this.transcoder.asQueueEventTranscoder(ReadingParcelMO.class), handler, this.source.getRegistry().timer("consume-fallback-reading"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                on.queueDeclare("bergamot.reading.fallback_queue", true, false, false, null);
                on.exchangeDeclare("bergamot.reading.fallback", "fanout", true, false, null);
                on.queueBind("bergamot.reading.fallback_queue", "bergamot.reading.fallback", "");
                return "bergamot.reading.fallback_queue";
            }
        };
    }

    @Override
    public void close()
    {
    }
}

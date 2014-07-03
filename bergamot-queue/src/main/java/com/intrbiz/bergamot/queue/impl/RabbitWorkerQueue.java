package com.intrbiz.bergamot.queue.impl;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.queue.WorkerQueue;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.GenericKey;
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

    public RabbitWorkerQueue(QueueBrokerPool<Channel> broker)
    {
        this.broker = broker;
    }

    public String getName()
    {
        return "worker-queue";
    }

    @Override
    public RoutedProducer<ExecuteCheck> publishChecks(GenericKey defaultKey)
    {
        return new RabbitProducer<ExecuteCheck>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), defaultKey, this.source.getRegistry().timer("publish-check"))
        {
            protected String setupExchange(Channel on) throws IOException
            {
                on.exchangeDeclare("bergamot.check", "topic", true, false, args("alternate-exchange", "bergamot.check.site.default"));
                return "bergamot.check";
            }
        };
    }

    @Override
    public Consumer<ExecuteCheck> consumeChecks(DeliveryHandler<ExecuteCheck> handler, UUID site, String theWorkerPool, String engine)
    {
        final String workerPool = Util.isEmpty(theWorkerPool) ? null : theWorkerPool;
        if (Util.isEmpty(engine)) throw new IllegalArgumentException("The parameter: engine must be given");
        return new RabbitConsumer<ExecuteCheck>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), handler, this.source.getRegistry().timer("consume-check"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                // the dead check queue
                on.queueDeclare("bergamot.dead_check_queue", true, false, false, null);
                on.exchangeDeclare("bergamot.dead_check", "fanout", true);
                on.queueBind("bergamot.dead_check_queue", "bergamot.dead_check", "");
                // the check queue
                String queueName = "bergamot.check." + (site == null ? "default" : site.toString()) + "." + Util.coalesceEmpty(workerPool, "default") + "." + engine;
                // setup the queue
                on.queueDeclare(queueName, true, false, false, args("x-dead-letter-exchange", "bergamot.dead_check"));
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
                    on.exchangeBind("bergamot.check.site." + site, "bergamot.check", site + ".*.*");
                }
                // worker pool default exchanges
                if (workerPool != null)
                {
                    on.exchangeDeclare("bergamot.check.worker_pool.default." + workerPool, "topic", true, false, args("alternate-exchange", "bergamot.dead_check"));
                    // bind the worker pool to default site exchange
                    on.exchangeBind("bergamot.check.site.default", "bergamot.check.worker_pool.default." + workerPool, "*." + workerPool + ".*");
                }
                // specific exchanges
                if (site != null && workerPool != null)
                {
                    on.exchangeDeclare("bergamot.check.worker_pool." + site + "." + workerPool, "topic", true, false, args("alternate-exchange", "bergamot.dead_check"));
                    // bind the worker pool exchange to the site exchange
                    on.exchangeBind("bergamot.check.worker_pool." + site + "." + workerPool, "bergamot.check.site." + site, "*." + workerPool + ".*");
                }
                // bind the queue
                on.queueBind(queueName, "bergamot.check.worker_pool." + (site == null ? "default" : site) + "." + (workerPool == null ? "any" : workerPool), "*.*." + engine);
                return queueName;
            }
        };
    }

    @Override
    public Consumer<ExecuteCheck> consumeDeadChecks(DeliveryHandler<ExecuteCheck> handler)
    {
        return new RabbitConsumer<ExecuteCheck>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), handler, this.source.getRegistry().timer("consume-dead-check"))
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
    public RoutedProducer<Result> publishResults(GenericKey defaultKey)
    {
        return new RabbitProducer<Result>(this.broker, this.transcoder.asQueueEventTranscoder(Result.class), defaultKey, this.source.getRegistry().timer("publish-result"))
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
    public Consumer<Result> consumeResults(DeliveryHandler<Result> handler, String instance)
    {
        return new RabbitConsumer<Result>(this.broker, this.transcoder.asQueueEventTranscoder(Result.class), handler, this.source.getRegistry().timer("consume-result"))
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
    public Consumer<Result> consumeFallbackResults(DeliveryHandler<Result> handler)
    {
        return new RabbitConsumer<Result>(this.broker, this.transcoder.asQueueEventTranscoder(Result.class), handler, this.source.getRegistry().timer("consume-fallback-result"))
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

    @Override
    public void close()
    {
    }
}

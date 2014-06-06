package com.intrbiz.bergamot.queue.impl;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.queue.WorkerQueue;
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
        return new RabbitProducer<ExecuteCheck>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), defaultKey)
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
        return new RabbitConsumer<ExecuteCheck>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), handler)
        {
            public String setupQueue(Channel on) throws IOException
            {
                String queueName = "bergamot.check." + (site == null ? "default" : site.toString()) + "." + Util.coalesceEmpty(workerPool, "default") + "." + engine;
                // setup the queue
                on.queueDeclare(queueName, true, false, false, args("x-dead-letter-exchange", "bergamot.dead_check"));
                // common exchanges
                on.exchangeDeclare("bergamot.check", "topic", true, false, args("alternate-exchange", "bergamot.check.site.default"));
                on.exchangeDeclare("bergamot.dead_check", "topic", true);
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
    public Consumer<ExecuteCheck> consumeDeadChecks(DeliveryHandler<ExecuteCheck> handler, UUID site)
    {
        return new RabbitConsumer<ExecuteCheck>(this.broker, this.transcoder.asQueueEventTranscoder(ExecuteCheck.class), handler)
        {
            public String setupQueue(Channel on) throws IOException
            {
                String queueName = "bergamot.dead_check." + (site == null ? "default" : site.toString());
                on.queueDeclare(queueName, true, false, false, null);
                on.exchangeDeclare("bergamot.dead_check", "topic", true);
                on.queueBind(queueName, "bergamot.dead_check", site == null ? "#" : site.toString() + ".*.*");
                return queueName;
            }
        };
    }

    @Override
    public RoutedProducer<Result> publishResults(GenericKey defaultKey)
    {
        return new RabbitProducer<Result>(this.broker, this.transcoder.asQueueEventTranscoder(Result.class), defaultKey)
        {
            protected String setupExchange(Channel on) throws IOException
            {
                on.exchangeDeclare("bergamot.result", "topic", true);
                return "bergamot.result";
            }
        };
    }

    @Override
    public Consumer<Result> consumeResults(DeliveryHandler<Result> handler, UUID site)
    {
        return new RabbitConsumer<Result>(this.broker, this.transcoder.asQueueEventTranscoder(Result.class), handler)
        {
            public String setupQueue(Channel on) throws IOException
            {
                String queueName = "bergamot.result." + (site == null ? "default" : site.toString());
                on.queueDeclare(queueName, true, false, false, null);
                on.exchangeDeclare("bergamot.result", "topic", true);
                on.queueBind(queueName, "bergamot.result", site == null ? "#" : site.toString());
                return queueName;
            }
        };
    }

    @Override
    public void close()
    {
    }
}

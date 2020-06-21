package com.intrbiz.bergamot.cluster.client.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.intrbiz.bergamot.cluster.client.NotifierClient;
import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.proxy.model.ClientHeader;

import io.netty.channel.Channel;

/**
 * A Bergamot Notifier proxy client
 */
public class ProxyNotifierClient extends ProxyBaseClient implements NotifierClient
{
    private final ProxyNotificationConsumer consumer;
    
    public ProxyNotifierClient(Consumer<Void> onPanic, String application, String info, Set<String> availableEngines) throws Exception
    {
        super(onPanic, new ClientHeader().userAgent(application).info(info).proxyForNotifier().engines(availableEngines));
        this.consumer = new ProxyNotificationConsumer();
    }

    @Override
    protected void handleMessage(Message message, Channel channel)
    {
        if (message instanceof Notification)
        {
            this.consumer.acceptNotification((Notification) message);
        }
    }
    
    @Override
    public NotificationConsumer getNotifierConsumer()
    {
        return this.consumer;
    }

    private class ProxyNotificationConsumer implements NotificationConsumer
    {   
        private volatile List<Notification> buffer;
        
        private volatile Executor executor;
        
        private volatile Consumer<Notification> consumer;
        
        void acceptNotification(Notification notification)
        {
            synchronized (this)
            {
                if (this.consumer != null && this.executor != null)
                {
                    this.executor.execute(() -> {
                        this.consumer.accept(notification);
                    });
                }
                else
                {
                    if (this.buffer == null) this.buffer = new ArrayList<>();
                    this.buffer.add(notification);
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
        public boolean start(Executor executor, Consumer<Notification> consumer)
        {
            synchronized (this)
            {
                this.executor = executor;
                this.consumer = consumer;
                if (this.buffer != null)
                {
                    for (Notification notification : this.buffer)
                    {
                        this.executor.execute(() -> {
                            consumer.accept(notification);
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
        public void drainTo(Consumer<Notification> consumer)
        {
        }

        @Override
        public void destroy()
        {
        }
    }
    
}

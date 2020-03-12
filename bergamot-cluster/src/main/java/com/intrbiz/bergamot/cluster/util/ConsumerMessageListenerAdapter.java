package com.intrbiz.bergamot.cluster.util;

import java.util.Objects;
import java.util.function.Consumer;

import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;

public class ConsumerMessageListenerAdapter<T> implements MessageListener<T>
{
    private final Consumer<T> consumer;
    
    public ConsumerMessageListenerAdapter(Consumer<T> consumer)
    {
        super();
        this.consumer = Objects.requireNonNull(consumer);
    }

    @Override
    public void onMessage(Message<T> message)
    {
        if (message != null && message.getMessageObject() != null)
        {
            this.consumer.accept(message.getMessageObject());
        }
    }
}

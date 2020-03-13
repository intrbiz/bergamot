package com.intrbiz.bergamot.cluster.broker;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import com.intrbiz.bergamot.cluster.util.ConsumerMessageListenerAdapter;
import com.intrbiz.bergamot.model.message.MessageObject;

public abstract class GenericGlobalTopic<T extends MessageObject>
{
    protected final HazelcastInstance hazelcast;
    
    protected final ITopic<T> topic;

    protected GenericGlobalTopic(HazelcastInstance hazelcast, String topicName)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        // Create our topic
        this.topic = this.hazelcast.getReliableTopic(topicName);
    }
    
    public void publish(T event)
    {
        this.topic.publish(event);
    }
    
    public UUID listen(Consumer<T> listener)
    {
        return this.topic.addMessageListener(new ConsumerMessageListenerAdapter<>(listener));
    }
    
    public void unlisten(UUID listenId)
    {
        this.topic.removeMessageListener(listenId);
    }
}

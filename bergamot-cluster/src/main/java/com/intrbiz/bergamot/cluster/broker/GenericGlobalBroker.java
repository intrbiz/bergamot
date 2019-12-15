package com.intrbiz.bergamot.cluster.broker;

import java.util.Objects;
import java.util.function.Consumer;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.intrbiz.bergamot.cluster.util.ConsumerMessageListenerAdapter;
import com.intrbiz.bergamot.model.message.MessageObject;

public class GenericGlobalBroker<T extends MessageObject>
{
    protected final HazelcastInstance hazelcast;
    
    protected final ITopic<T> topic;

    protected GenericGlobalBroker(HazelcastInstance hazelcast, String topicName)
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
    
    public String listen(Consumer<T> listener)
    {
        return this.topic.addMessageListener(new ConsumerMessageListenerAdapter<>(listener));
    }
    
    public void unlisten(String listenId)
    {
        this.topic.removeMessageListener(listenId);
    }
}

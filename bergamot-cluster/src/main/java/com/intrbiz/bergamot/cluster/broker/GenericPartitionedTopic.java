package com.intrbiz.bergamot.cluster.broker;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;

import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.impl.reliable.ReliableTopicService;
import com.intrbiz.bergamot.cluster.util.ConsumerMessageListenerAdapter;
import com.intrbiz.bergamot.model.message.MessageObject;

public abstract class GenericPartitionedTopic<K, T extends MessageObject>
{
    protected final HazelcastInstance hazelcast;
    
    protected final Function<K, String> topicName;
    
    protected final ConcurrentMap<String, ITopic<T>> topics;

    protected GenericPartitionedTopic(HazelcastInstance hazelcast, Function<K, String> topicName)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.topicName = Objects.requireNonNull(topicName);
        // Create our topic
        this.topics = new ConcurrentHashMap<>();
        // Setup a object listener to clean up our siteTopics cache
        this.hazelcast.addDistributedObjectListener(new TopicListener());
    }
    
    private ITopic<T> getSiteTopic(K siteId)
    {
        return this.topics.computeIfAbsent(this.topicName.apply(siteId), this.hazelcast::getReliableTopic);
    }
    
    public void publish(K key, T message)
    {
        this.getSiteTopic(key)
            .publish(message);
    }
    
    public UUID listen(K key, Consumer<T> listener)
    {
        return this.getSiteTopic(key)
            .addMessageListener(new ConsumerMessageListenerAdapter<>(listener));
    }
    
    public void unlisten(K key, UUID listenId)
    {
        this.getSiteTopic(key)
            .removeMessageListener(listenId);
    }
    
    private class TopicListener implements DistributedObjectListener
    {
        @Override
        public void distributedObjectCreated(DistributedObjectEvent event)
        {
        }

        @Override
        public void distributedObjectDestroyed(DistributedObjectEvent event)
        {
            if (ReliableTopicService.SERVICE_NAME.equals(event.getServiceName()))
            {
                GenericPartitionedTopic.this.topics.remove(event.getObjectName());
            }
        }
    }
}

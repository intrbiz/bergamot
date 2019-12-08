package com.intrbiz.bergamot.cluster.broker;

import java.util.function.Consumer;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.util.ConsumerMessageListenerAdapter;
import com.intrbiz.bergamot.model.message.event.site.SiteEvent;

public class SiteEventBroker
{
    protected final HazelcastInstance hazelcast;
    
    protected final ITopic<SiteEvent> siteTopic;

    public SiteEventBroker(HazelcastInstance hazelcast)
    {
        super();
        this.hazelcast = hazelcast;
        // Create our topic
        this.siteTopic = this.hazelcast.getReliableTopic(ObjectNames.getSiteTopicName());
    }
    
    public void publish(SiteEvent event)
    {
        this.siteTopic.publish(event);
    }
    
    public String listen(Consumer<SiteEvent> listener)
    {
        return this.siteTopic.addMessageListener(new ConsumerMessageListenerAdapter<>(listener));
    }
    
    public void unlisten(String listenId)
    {
        this.siteTopic.removeMessageListener(listenId);
    }
}

package com.intrbiz.bergamot.cluster.coordinator;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.model.NotifierRegistration;

/**
 * Co-ordinate notifiers available
 */
public abstract class NotifierCoordinator
{   
    protected final SecureRandom random = new SecureRandom();
    
    protected final HazelcastInstance hazelcast;
    
    protected final IMap<UUID, NotifierRegistration> notifiers;
    
    public NotifierCoordinator(HazelcastInstance hazelcast)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.configureHazelcast(this.hazelcast.getConfig());
        // Get our state maps
        this.notifiers = this.hazelcast.getMap(ObjectNames.buildNotifierRegistrationsMapName());
    }
    
    protected void configureHazelcast(Config hazelcastConfig)
    {
    }

    public Collection<NotifierRegistration> getNotifiers()
    {
        return this.notifiers.values();
    }
    
    public NotifierRegistration getNotifier(UUID notifierId)
    {
        return this.notifiers.get(notifierId);
    }
}

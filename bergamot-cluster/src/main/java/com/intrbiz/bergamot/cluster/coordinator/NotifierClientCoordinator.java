package com.intrbiz.bergamot.cluster.coordinator;

import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.model.NotifierRegistration;
import com.intrbiz.bergamot.cluster.queue.NotifierConsumer;

/**
 * Co-ordinate notifiers
 */
public class NotifierClientCoordinator extends NotifierCoordinator
{
    private static final Logger logger = Logger.getLogger(NotifierClientCoordinator.class);
    
    public NotifierClientCoordinator(HazelcastInstance hazelcast)
    {
        super(hazelcast);
    }
    
    /**
     * Register a notifier with the cluster so that it can receive notifications to send
     * @param notifierId the random notifier UUID
     * @param restrictedSiteIds an optional set of sites that this worker can process checks for
     * @param availableEngines that can be used to send notifications
     * @return the {@code NotifierConsumer} associated to this notifier
     */
    public NotifierConsumer registerNotifier(UUID notifierId, boolean proxy, String application, String info, String hostName, Set<UUID> restrictedSiteIds, Set<String> availableEngines)
    {
        // We don't really want to allow multiple workers with the same id to register
        // TODO: we should probably just do the equivalent of ConcurrentMap.compute()
        if (this.notifiers.containsKey(notifierId))
            throw new RuntimeException("Notifier already registered, wait for registration to timeout or use a new id");
        // Register the worker
        logger.info("Registering notifier " + notifierId);
        NotifierRegistration reg = new NotifierRegistration(notifierId, proxy, application, info, hostName, restrictedSiteIds, availableEngines);
        this.notifiers.put(notifierId, reg);
        // Build the consumer for this registered worker
        return new NotifierConsumer(this.hazelcast, notifierId, this::unregisterNotifier, this::updateNotifierWatchdog);
    }
    
    private void updateNotifierWatchdog(UUID notifierId)
    {
        // Perform a get on the key to invalidate the idle timeouts
        this.notifiers.get(notifierId);
    }
    
    /**
     * Unregister a notifier
     */
    public void unregisterNotifier(UUID workerId)
    {
        // Remove the notifier from the registration map
        this.notifiers.remove(workerId);
    }
}

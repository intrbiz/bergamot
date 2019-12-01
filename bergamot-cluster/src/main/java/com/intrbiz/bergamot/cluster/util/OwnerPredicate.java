package com.intrbiz.bergamot.cluster.util;

import java.util.Map.Entry;
import java.util.UUID;

import com.hazelcast.query.Predicate;
import com.intrbiz.bergamot.cluster.model.ProcessingPoolRegistration;

/**
 * Filter the processing pool map based on the owning member of the processing pool
 */
public class OwnerPredicate implements Predicate<UUID, ProcessingPoolRegistration>
{
    private static final long serialVersionUID = 1L;
    
    private UUID owner;
    
    public OwnerPredicate()
    {
        super();
    }
    
    public OwnerPredicate(UUID owner)
    {
        this();
        this.owner = owner;
    }

    public UUID getOwner()
    {
        return owner;
    }

    public void setOwner(UUID owner)
    {
        this.owner = owner;
    }

    @Override
    public boolean apply(Entry<UUID, ProcessingPoolRegistration> mapEntry)
    {
        return this.owner.equals(mapEntry.getValue().getOwner());
    }
}

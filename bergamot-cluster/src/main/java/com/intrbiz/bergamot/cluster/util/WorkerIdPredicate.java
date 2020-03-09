package com.intrbiz.bergamot.cluster.util;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import com.hazelcast.query.Predicate;

public class WorkerIdPredicate implements Predicate<UUID, UUID>
{
    private static final long serialVersionUID = 1L;
    
    private final UUID workerId;
    
    public WorkerIdPredicate(UUID workerId)
    {
        super();
        this.workerId = Objects.requireNonNull(workerId);
    }

    @Override
    public boolean apply(Entry<UUID, UUID> mapEntry)
    {
        return this.workerId.equals(mapEntry.getValue());
    }
}

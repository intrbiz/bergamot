package com.intrbiz.bergamot.cluster;

import java.util.Map.Entry;

import com.hazelcast.query.Predicate;

/**
 * Filter the processing pool map based on the owning member of the processing pool
 */
public class OwnerPredicate implements Predicate<String, ProcessingPool>
{
    private static final long serialVersionUID = 1L;
    
    private String owner;
    
    public OwnerPredicate()
    {
        super();
    }
    
    public OwnerPredicate(String owner)
    {
        this();
        this.owner = owner;
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    @Override
    public boolean apply(Entry<String, ProcessingPool> mapEntry)
    {
        return this.owner.equals(mapEntry.getValue().getOwner());
    }
}

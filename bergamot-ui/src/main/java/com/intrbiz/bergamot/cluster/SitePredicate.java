package com.intrbiz.bergamot.cluster;

import java.util.Map.Entry;
import java.util.UUID;

import com.hazelcast.query.Predicate;

/**
 * Filter the processing pool map based on the site of the processing pool
 */
public class SitePredicate implements Predicate<String, ProcessingPool>
{
    private static final long serialVersionUID = 1L;

    private UUID site;

    public SitePredicate()
    {
        super();
    }

    public SitePredicate(UUID site)
    {
        this();
        this.site = site;
    }

    public UUID getSite()
    {
        return site;
    }

    public void setSite(UUID site)
    {
        this.site = site;
    }

    @Override
    public boolean apply(Entry<String, ProcessingPool> mapEntry)
    {
        return this.site.equals(mapEntry.getValue().getSite());
    }
}

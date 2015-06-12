package com.intrbiz.bergamot.cluster.migration;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.ClusterManager;

public class DeregisterPoolTask implements ClusterMigration
{
    private static final long serialVersionUID = 1L;

    private UUID site;
    
    private int pool;
    
    public DeregisterPoolTask()
    {
        super();
    }
    
    public DeregisterPoolTask(UUID site, int pool)
    {
        super();
        this.site = site;
        this.pool = pool;
    }

    public UUID getSite()
    {
        return site;
    }

    public void setSite(UUID site)
    {
        this.site = site;
    }

    public int getPool()
    {
        return pool;
    }

    public void setPool(int pool)
    {
        this.pool = pool;
    }

    @Override
    public boolean applyMigration(ClusterManager clusterManager) throws Exception
    {
        Logger logger = Logger.getLogger(DeregisterPoolTask.class);
        logger.info("Deregistering Pool " + this.site + "." + this.pool + " on member " + clusterManager.getLocalMemberUUID());
        // unsetup result processing
        if (!Boolean.getBoolean("bergamot.ui.resultprocessor.off"))
        {
            // tell the processor we are registering this  pool with it
            clusterManager.getResultProcessor().disownPool(this.getSite(), this.getPool());
        }
        // unsetup reading processing
        if (!Boolean.getBoolean("bergamot.ui.readingprocessor.off"))
        {
            // tell the processor we are registering this  pool with it
            clusterManager.getReadingProcessor().disownPool(this.getSite(), this.getPool());
        }
        // unsetup scheduling
        if (!Boolean.getBoolean("bergamot.ui.scheduler.off"))
        {
            // tell the scheduler we are registering this pool with it
            clusterManager.getScheduler().disownPool(this.getSite(), this.getPool());
        }
        return true;
    }
    
    public String toString()
    {
        return "Deregister pool " + this.site + "." + this.pool;
    }
}

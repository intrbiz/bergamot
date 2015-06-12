package com.intrbiz.bergamot.cluster.migration;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.ClusterManager;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.scheduler.Scheduler;

public class RegisterPoolTask implements ClusterMigration
{
    private static final long serialVersionUID = 1L;

    private UUID site;
    
    private int pool;
    
    public RegisterPoolTask()
    {
        super();
    }
    
    public RegisterPoolTask(UUID site, int pool)
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
        Logger logger = Logger.getLogger(RegisterPoolTask.class);
        logger.info("Registering Pool " + this.site + "." + this.pool + " on member " + clusterManager.getLocalMemberUUID());
        // setup result processing
        if (!Boolean.getBoolean("bergamot.ui.resultprocessor.off"))
        {
            // tell the processor we are registering this  pool with it
            clusterManager.getResultProcessor().ownPool(this.getSite(), this.getPool());
        }
        // setup reading processing
        if (!Boolean.getBoolean("bergamot.ui.readingprocessor.off"))
        {
            // tell the processor we are registering this  pool with it
            clusterManager.getReadingProcessor().ownPool(this.getSite(), this.getPool());
        }
        // setup scheduling
        if (!Boolean.getBoolean("bergamot.ui.scheduler.off"))
        {
            // tell the scheduler we are registering this pool with it
            clusterManager.getScheduler().ownPool(this.getSite(), this.getPool());
            // register all hosts and services with the scheduler
            Scheduler scheduler = clusterManager.getScheduler();
            try (BergamotDB db = BergamotDB.connect())
            {
                for (Host host : db.listHostsInPool(this.getSite(), this.getPool()))
                {
                    scheduler.schedule(host);
                }
                for (Service service : db.listServicesInPool(this.getSite(), this.getPool()))
                {
                    scheduler.schedule(service);
                }
            }
        }
        return true;
    }
    
    public String toString()
    {
        return "Register pool " + this.site + "." + this.pool;
    }
}

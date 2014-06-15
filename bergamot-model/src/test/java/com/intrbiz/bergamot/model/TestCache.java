package com.intrbiz.bergamot.model;

import java.util.UUID;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.data.DataManager;
import com.intrbiz.data.cache.HazelcastCacheProvider;
import com.intrbiz.data.cache.tiered.TieredCacheProvider;
import com.intrbiz.util.compiler.CompilerTool;
import com.intrbiz.util.pool.database.DatabasePool;

public class TestCache
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        Logger.getLogger(CompilerTool.class).setLevel(Level.TRACE);
        // setup cache
        DataManager.get().registerDefaultCacheProvider(new TieredCacheProvider(new HazelcastCacheProvider("hazelcast.cache")));
        // create the schema
        DataManager.getInstance().registerDefaultServer(DatabasePool.Default.create(org.postgresql.Driver.class, "jdbc:postgresql://127.0.0.1/bergamot", "bergamot", "bergamot"));
        //
        try (BergamotDB db = BergamotDB.connect())
        {
            UUID siteId = Site.randomSiteId();
            Site site = new Site(siteId, "test", "Test");
            // add the site
            db.setSite(site);
            // get the site
            for (int i = 0; i < 10; i++)
            {
                long s = System.nanoTime();
                Site gotSite = db.getSite(siteId);
                long e = System.nanoTime();
                //
                System.out.println("Got: " + gotSite + " in " + ((e-s)/1000) + "us");
            }
            // update the site
            site.setSummary("Testing 123...");
            db.setSite(site);
            // get the site
            for (int i = 0; i < 10; i++)
            {
                long s = System.nanoTime();
                Site gotSite = db.getSite(siteId);
                long e = System.nanoTime();
                //
                System.out.println("Got: " + gotSite + " in " + ((e-s)/1000) + "us");
            }
            // get site by name
            for (int i = 0; i < 10; i++)
            {
                long s = System.nanoTime();
                Site gotSite = db.getSiteByName("test");
                long e = System.nanoTime();
                //
                System.out.println("Got: " + gotSite + " in " + ((e-s)/1000) + "us");
            }
            // remove
            db.removeSite(site.getId());
        }
        System.exit(0);
    }
}

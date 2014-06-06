package com.intrbiz.bergamot.model;

import java.io.File;
import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.BergamotConfigReader;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.data.BergamotConfigImporter;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.state.GroupState;
import com.intrbiz.data.DataManager;
import com.intrbiz.util.compiler.CompilerTool;
import com.intrbiz.util.pool.database.DatabasePool;

public class TestDB
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        Logger.getLogger(CompilerTool.class).setLevel(Level.TRACE);
        // create the schema
        DataManager.getInstance().registerDefaultServer(DatabasePool.Default.create(org.postgresql.Driver.class, "jdbc:postgresql://127.0.0.1/bergamot", "bergamot", "bergamot"));
        BergamotDB.install();
        //
        Collection<BergamotCfg> configs = new BergamotConfigReader().includeDir(new File("/home/cellis/Intrbiz/workspace-new/bergamot/cfg/local/")).build();
        for (BergamotCfg config : configs)
        {
            System.out.println("Importing configuration for " + config.getSite());
            // load
            new BergamotConfigImporter(config).resetState(true).importConfiguration();
        }
        //
        try (BergamotDB db = BergamotDB.connect())
        {
            for (Site site : db.listSites())
            {
                for (Group group : db.listGroups(site.getId()))
                {
                    GroupState state = group.getState();
                    System.out.println("Group state: " + state.isOk() + " " + state.getStatus() + " " + state.getPendingCount() + " " + state.getOkCount());
                }
                for (Location location : db.listLocations(site.getId()))
                {
                    GroupState state = location.getState();
                    System.out.println("Location state: " + state.isOk() + " " + state.getStatus() + " " + state.getPendingCount() + " " + state.getOkCount());
                }
            }
        }
        //
        System.exit(0);
    }
}

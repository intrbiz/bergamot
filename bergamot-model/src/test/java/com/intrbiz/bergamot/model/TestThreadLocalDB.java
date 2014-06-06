package com.intrbiz.bergamot.model;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.data.DataManager;
import com.intrbiz.util.compiler.CompilerTool;
import com.intrbiz.util.pool.database.DatabasePool;

public class TestThreadLocalDB
{
    public static void main(String[] args) throws Exception
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.TRACE);
        Logger.getLogger(CompilerTool.class).setLevel(Level.TRACE);
        // create the schema
        DataManager.getInstance().registerDefaultServer(DatabasePool.Default.create(org.postgresql.Driver.class, "jdbc:postgresql://127.0.0.1/bergamot", "bergamot", "bergamot"));
        //
        try (BergamotDB db = BergamotDB.connect())
        {
            //
            try (BergamotDB db2 = BergamotDB.connect())
            {
                System.out.println("DB2 == DB: " + ( db == db2 ));
            }
            //
        }
    }
}

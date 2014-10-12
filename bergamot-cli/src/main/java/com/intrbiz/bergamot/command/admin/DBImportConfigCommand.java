package com.intrbiz.bergamot.command.admin;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.BergamotCLIException;
import com.intrbiz.bergamot.config.BergamotConfigReader;
import com.intrbiz.bergamot.config.UICfg;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.importer.BergamotConfigImporter;
import com.intrbiz.bergamot.importer.BergamotImportReport;
import com.intrbiz.data.DataManager;
import com.intrbiz.util.pool.database.DatabasePool;

public class DBImportConfigCommand extends BergamotCLICommand
{
    public DBImportConfigCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "db-import-config";
    }
    
    @Override
    public boolean admin()
    {
        return true;
    }

    @Override
    public String usage()
    {
        return "<config-dir>";
    }

    @Override
    public String help()
    {
        return "Import Bergamot configuration directly into the database\n" +
                "  Eg: bergamot-cli db-import-config '/etc/bergamot/config/bergamot.local/'\n" +
                "\n" +
                "Arguments:\n" +
                "  <config-dir> the path of the directory containing the Bergamot configuration, Eg: '/etc/bergamot/config/bergamot.local'\n" +
                "\n" +
                "Note: this command must to be run locally on the UI node, once the config is imported all UI daemons should be restarted.";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        if (args.size() != 1) throw new BergamotCLIException("No configuration directory given");
        // the config dir
        File confDir = new File(args.get(0));
        if (! confDir.isDirectory()) throw new BergamotCLIException("The path '" + confDir.getAbsolutePath() + "' is not a directory!");
        // read the UI config and connect to the database
        UICfg config = UICfg.loadConfiguration();
        // setup the data manager
        DataManager.getInstance().registerDefaultServer(DatabasePool.Default.with().postgresql().url(config.getDatabase().getUrl()).username(config.getDatabase().getUsername()).password(config.getDatabase().getPassword()).build());
        // ensure the DB schema is installed
        BergamotDB.install();
        // load the config
        Collection<ValidatedBergamotConfiguration> bcfgs = new BergamotConfigReader().includeDir(new File("/home/cellis/Intrbiz/workspace-new/bergamot/cfg/local/")).build();
        // assert the configuration is valid
        for (ValidatedBergamotConfiguration vbcfg : bcfgs)
        {
            if (! vbcfg.getReport().isValid())
            {
                throw new BergamotCLIException(vbcfg.getReport().toString());
            }
        }
        // import import import
        for (ValidatedBergamotConfiguration bcfg : bcfgs)
        {
            System.out.println("Importing configuration for " + bcfg.getConfig().getSite());
            BergamotImportReport report = new BergamotConfigImporter(bcfg).createSite(true).resetState(true).importConfiguration();
            System.out.println(report.toString());
            System.out.println("Import committed for: " + bcfg.getConfig().getSite());
        }
        // all ok
        return 0;
    }
}

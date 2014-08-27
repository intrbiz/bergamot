package com.intrbiz.bergamot.command.admin;

import java.util.List;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.BergamotCLIException;
import com.intrbiz.bergamot.config.UICfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.data.DataManager;
import com.intrbiz.util.pool.database.DatabasePool;

public class CreateSiteCommand extends BergamotCLICommand
{
    public CreateSiteCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "create-site";
    }
    
    @Override
    public boolean admin()
    {
        return true;
    }

    @Override
    public String usage()
    {
        return "'<site-name>' '<site-summary>' [<site-alias>, ]";
    }

    @Override
    public String help()
    {
        return "Create a Bergamot site with the given <site-name> and optional <site-alias>es.\n" +
                "  Eg: bergamot-cli create-site 'bergamot.local' 'bergamot.mydomain.com' 'localhost'\n" +
                "\n" +
                "Arguments:\n" +
                "  <site-name> the host name of this Bergamot site, Eg: 'bergamot.local'\n" +
                "  <site-summary> a short description of this site, Eg: 'Local Bergamot Instance'\n" +
                "  <site-alias> additional host names for this Bergamot site, Eg: 'bergamot.mydomain.com'\n" +
                "\n" +
                "Note: this command must to be run locally on the UI node.";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        if (args.size() < 2) throw new BergamotCLIException("No site name or summary given");
        // the site name
        String siteName = args.remove(0);
        String siteSummary = args.remove(0);
        // read the UI config and connect to the database
        UICfg config = UICfg.loadConfiguration();
        // setup the data manager
        DataManager.getInstance().registerDefaultServer(DatabasePool.Default.with().postgresql().url(config.getDatabase().getUrl()).username(config.getDatabase().getUsername()).password(config.getDatabase().getPassword()).build());
        // ensure the DB schema is installed
        BergamotDB.install();
        // now actually create the site
        try (BergamotDB db = BergamotDB.connect())
        {
            // check the site does not already exist!
            if (db.getSiteByName(siteName) != null)
            {
                throw new BergamotCLIException("A site with the name '" + siteName + "' already exists!");
            }
            // check aliases
            for (String alias : args)
            {
                if (db.getSiteByName(alias) != null)
                {
                    throw new BergamotCLIException("A site with the name '" + alias + "' already exists!");
                }    
            }
            // create it
            Site site = new Site(Site.randomSiteId(), siteName, siteSummary);
            // aliases
            for (String alias : args)
            {
                site.getAliases().add(alias);
            }
            // create
            db.setSite(site);
            // all done
            System.out.println("Created the site '" + siteName + "' with id: " + site.getId());
        }
        // all ok
        return 0;
    }
}

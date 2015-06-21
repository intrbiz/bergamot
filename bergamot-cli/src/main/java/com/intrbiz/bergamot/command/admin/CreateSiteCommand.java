package com.intrbiz.bergamot.command.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.BergamotCLIException;
import com.intrbiz.bergamot.config.BergamotConfigReader;
import com.intrbiz.bergamot.config.UICfg;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.importer.BergamotConfigImporter;
import com.intrbiz.bergamot.importer.BergamotImportReport;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.model.message.agent.manager.request.CreateSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.response.CreatedSiteCA;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerRequest;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerResponse;
import com.intrbiz.bergamot.model.message.cluster.manager.request.InitSite;
import com.intrbiz.bergamot.model.message.cluster.manager.response.InitedSite;
import com.intrbiz.bergamot.queue.BergamotAgentManagerQueue;
import com.intrbiz.bergamot.queue.BergamotClusterManagerQueue;
import com.intrbiz.data.DataManager;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.name.RoutingKey;
import com.intrbiz.queue.rabbit.RabbitPool;
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
        UUID   siteId      = Site.randomSiteId();
        String siteName    = args.remove(0);
        String siteSummary = args.remove(0);
        // check for site config
        File siteConfigDir = new File(new File(System.getProperty("bergamot.site.config.base", "/etc/bergamot/config/")), siteName);
        if (siteConfigDir.exists()) throw new BergamotCLIException("Looks like the site '" + siteName + "' already exists, or at least it's config dir does: '" + siteConfigDir.getAbsolutePath() + "'");
        // check that the template config dir exists
        File templateConfigDir = new File(new File(System.getProperty("bergamot.site.config.base", "/etc/bergamot/config/")), "template");
        if (! (templateConfigDir.exists() && templateConfigDir.isDirectory())) throw new BergamotCLIException("The Bergamot template configuration directory: '" + templateConfigDir.getAbsolutePath() + "' could not be found, please check your Bergamot installation!");
        // read the UI config and connect to the database
        UICfg config = UICfg.loadConfiguration();
        // setup the data manager
        DataManager.getInstance().registerDefaultServer(DatabasePool.Default.with().postgresql().url(config.getDatabase().getUrl()).username(config.getDatabase().getUsername()).password(config.getDatabase().getPassword()).build());
        // setup the queue manager
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool(config.getBroker().getUrl(), config.getBroker().getUsername(), config.getBroker().getPassword()));
        // ensure the DB schema is installed
        BergamotDB.install();
        // now check that we can create the site and it's aliases
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
        }
        // now setup the site config dir by copying the templates over
        if (! siteConfigDir.mkdirs() ) throw new BergamotCLIException("Failed to create the site configuration directory '" + siteConfigDir.getAbsolutePath() + "', can't create the site '" + siteName + "'");
        this.copySiteTemplateConfiguration(templateConfigDir, siteConfigDir, siteName);
        // load the site configuration
        Collection<ValidatedBergamotConfiguration> vbcfgs = new BergamotConfigReader().includeDir(siteConfigDir).build();
        // assert the configuration is valid
        for (ValidatedBergamotConfiguration vbcfg : vbcfgs)
        {
            if (! vbcfg.getReport().isValid())
            {
                throw new BergamotCLIException(vbcfg.getReport().toString());
            }
            else
            {
                System.out.println(vbcfg.getReport().toString());
            }
        }
        // now create the site
        try (BergamotDB db = BergamotDB.connect())
        {
            // create it
            Site site = new Site(siteId, siteName, siteSummary);
            // aliases
            for (String alias : args)
            {
                site.getAliases().add(alias);
            }
            // create
            db.setSite(site);
        }
        // now import the site config
        for (ValidatedBergamotConfiguration vbcfg : vbcfgs)
        {
            BergamotImportReport report = new BergamotConfigImporter(vbcfg).resetState(true).importConfiguration();
            System.out.println(report.toString());
        }
        // create the Site CA
        try (BergamotAgentManagerQueue queue = BergamotAgentManagerQueue.open())
        {
            try (RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> client = queue.createBergamotAgentManagerRPCClient())
            {
                try
                {
                    AgentManagerResponse response = client.publish(new CreateSiteCA(siteId, siteName)).get(5, TimeUnit.SECONDS);
                    if (response instanceof CreatedSiteCA)
                    {
                        System.out.println("Created Bergamot Agent site Certificate Authority");
                    }
                }
                catch (Exception e)
                {
                }
            }
        }
        // message the UI cluster to setup the site
        try (BergamotClusterManagerQueue queue = BergamotClusterManagerQueue.open())
        {
            try (RPCClient<ClusterManagerRequest, ClusterManagerResponse, RoutingKey> client = queue.createBergamotClusterManagerRPCClient())
            {
                try
                {
                    ClusterManagerResponse response = client.publish(new InitSite(siteId, siteName)).get(5, TimeUnit.SECONDS);
                    if (response instanceof InitedSite)
                    {
                        System.out.println("Initialised site with UI cluster");
                    }
                }
                catch (Exception e)
                {
                }
            }
        }
        // all done
        System.out.println("Created the site '" + siteName + "' and imported the default configuration, have fun :)");
        // all ok
        return 0;
    }

    protected void copySiteTemplateConfiguration(File templateConfigDir, File siteConfigDir, String siteName) throws Exception
    {
        // copy the template dir over to the site dir
        // as we copy we load each config file and alter the site name
        Stack<File> workQueue = new Stack<File>();
        workQueue.push(templateConfigDir);
        while (! workQueue.isEmpty())
        {
            File work = workQueue.pop();
            if (work.isDirectory())
            {
                for (File file : work.listFiles())
                {
                    workQueue.push(file);
                }
            }
            else if (work.isFile())
            {
                if (work.getName().endsWith(".xml"))
                {
                    try (FileInputStream in = new FileInputStream(work))
                    {
                        BergamotCfg cfg = BergamotCfg.read(BergamotCfg.class, in);
                        cfg.setSite(siteName);
                        // output the config
                        String destFileRelPath = work.getAbsolutePath().substring(templateConfigDir.getAbsolutePath().length() + 1);
                        File destFile = new File(siteConfigDir, destFileRelPath);
                        destFile.getParentFile().mkdirs();
                        // save the config out
                        try (FileOutputStream out = new FileOutputStream(destFile))
                        {
                            BergamotCfg.write(BergamotCfg.class, cfg, out);
                        }
                    }
                }
            }
        }
    }
    
}

package com.intrbiz.bergamot.command.admin;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.config.UICfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.model.message.agent.manager.request.CreateSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.response.CreatedSiteCA;
import com.intrbiz.bergamot.queue.BergamotAgentManagerQueue;
import com.intrbiz.bergamot.queue.util.QueueUtil;
import com.intrbiz.data.DataManager;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.name.RoutingKey;
import com.intrbiz.util.pool.database.DatabasePool;

public class GenerateSiteCAsCommand extends BergamotCLICommand
{
    public GenerateSiteCAsCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "generate-site-cas";
    }
    
    @Override
    public boolean admin()
    {
        return true;
    }

    @Override
    public String usage()
    {
        return "";
    }

    @Override
    public String help()
    {
        return "Ensure that a Site certificate authority is generated for each site\n";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        // read the UI config and connect to the database
        UICfg config = UICfg.loadConfiguration();
        // setup the data manager
        DataManager.getInstance().registerDefaultServer(DatabasePool.Default.with().postgresql().url(config.getDatabase().getUrl()).username(config.getDatabase().getUsername()).password(config.getDatabase().getPassword()).build());
        // setup the queue manager
        QueueUtil.setupQueueBroker(config.getBroker(), "bergamot-cli");
        // ensure the DB schema is installed
        BergamotDB.install();
        // now actually create the site
        try (BergamotDB db = BergamotDB.connect())
        {
            // create the Site CA
            try (BergamotAgentManagerQueue queue = BergamotAgentManagerQueue.open())
            {
                try (RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> client = queue.createBergamotAgentManagerRPCClient())
                {            
                    for (Site site : db.listSites())
                    {
                        try
                        {
                            AgentManagerResponse response = client.publish(new CreateSiteCA(site.getId(), site.getName())).get(5, TimeUnit.SECONDS);
                            if (response instanceof CreatedSiteCA)
                            {
                                System.out.println("Created Bergamot Agent site Certificate Authority for site " + site.getName());
                            }
                        }
                        catch (Exception e)
                        {
                        }
                    }
                    System.out.println("Finished creating Bergamot Agent site Certificate Authorities for all sites");
                }
            }
        }
        // all ok
        return 0;
    }
}

package com.intrbiz.bergamot.command.admin;

import java.util.List;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.config.UICfg;
import com.intrbiz.bergamot.health.HealthTracker;
import com.intrbiz.bergamot.health.model.KnownDaemon;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.rabbit.RabbitPool;

public class DiscoverDaemonsCommand extends BergamotCLICommand
{
    public DiscoverDaemonsCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "discover-daemons";
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
        return "Discover the running daemons in a Bergamot Monitoring cluster";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        // read the UI config and connect to the database
        UICfg config = UICfg.loadConfiguration();
        // setup the queue manager
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool(config.getBroker().getUrl(), config.getBroker().getUsername(), config.getBroker().getPassword()));
        // setup the health checker
        HealthTracker tracker = HealthTracker.getInstance();
        tracker.init();
        // health check joins are async, so we need to wait for message to be recieved
        Thread.sleep(1_000L);
        // list our the daemons we found
        System.out.println("Running Daemons:");
        for (KnownDaemon daemon : tracker.getDaemons())
        {
            System.out.println("  " + daemon.getDaemonName() + " (" + daemon.getInstanceId() + "/" + daemon.getRuntimeId() + ") on host " + daemon.getHostName() + " (" + daemon.getHostId() + ") is online");
        }
        return 0;
    }
}

package com.intrbiz.bergamot.command.admin;

import java.util.List;
import java.util.UUID;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.BergamotCLIException;
import com.intrbiz.bergamot.config.UICfg;
import com.intrbiz.bergamot.health.HealthTracker;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.rabbit.RabbitPool;

public class UnjoinDaemonCommand extends BergamotCLICommand
{
    public UnjoinDaemonCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "unjoin-daemon";
    }
    
    @Override
    public boolean admin()
    {
        return true;
    }

    @Override
    public String usage()
    {
        return "<instance-uuid>";
    }

    @Override
    public String help()
    {
        return "Send an unjoin request on behalf of a failed daemon";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        if (args.size() != 1) throw new BergamotCLIException("The instance UUID of the daemon are required");
        UUID instanceId = UUID.fromString(args.remove(0));
        // read the UI config and connect to the database
        UICfg config = UICfg.loadConfiguration();
        // setup the queue manager
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool(config.getBroker().getUrl(), config.getBroker().getUsername(), config.getBroker().getPassword()));
        // setup the health checker
        HealthTracker tracker = HealthTracker.getInstance();
        tracker.init();
        tracker.unjoinDaemon(instanceId);
        return 0;
    }
}

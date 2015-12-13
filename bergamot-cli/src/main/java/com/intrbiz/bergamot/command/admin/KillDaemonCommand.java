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

public class KillDaemonCommand extends BergamotCLICommand
{
    public KillDaemonCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "kill-daemon";
    }
    
    @Override
    public boolean admin()
    {
        return true;
    }

    @Override
    public String usage()
    {
        return "<instance-uuid> <runtime-uuid>";
    }

    @Override
    public String help()
    {
        return "Request that the given daemon immediately terminates";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        if (args.size() != 2) throw new BergamotCLIException("The instance UUID and runtime UUID of the daemon are required");
        UUID instanceId = UUID.fromString(args.remove(0));
        UUID runtimeId = UUID.fromString(args.remove(0));
        // read the UI config and connect to the database
        UICfg config = UICfg.loadConfiguration();
        // setup the queue manager
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool(config.getBroker().getUrl(), config.getBroker().getUsername(), config.getBroker().getPassword()));
        // setup the health checker
        HealthTracker tracker = HealthTracker.getInstance();
        tracker.init();
        tracker.killDaemon(instanceId, runtimeId);
        return 0;
    }
}

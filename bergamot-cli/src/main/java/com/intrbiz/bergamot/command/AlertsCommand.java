package com.intrbiz.bergamot.command;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.BergamotCLIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.config.CLICfg;
import com.intrbiz.bergamot.config.CLISiteCfg;
import com.intrbiz.bergamot.model.message.AlertMO;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.bergamot.model.message.ServiceMO;
import com.intrbiz.bergamot.model.message.TrapMO;

public class AlertsCommand extends BergamotCLICommand
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    
    public AlertsCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "alerts";
    }

    @Override
    public String usage()
    {
        return "<site-name>";
    }

    @Override
    public String help()
    {
        return "Get the current alerts for a site\n" +
                "\n" +
                "Arguments:\n" +
                "  <site-name> a configured site name, eg: 'bergamot.local'\n" +
                "\n";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        if (args.size() != 1) throw new BergamotCLIException("No site name given");
        String siteName = args.remove(0);
        CLISiteCfg site = CLICfg.loadConfiguration().getSite(siteName);
        if (site == null) throw new BergamotCLIException("No site configured with the name '" + siteName + "'");
        // connect to the API
        BergamotClient client = new BergamotClient(site.getUrl(), site.getAuthToken());
        // call the hello world test
        for (AlertMO alert : client.callGetAlerts().execute())
        {
            System.out.print("Alert [" + alert.getId() + "] - " + alert.getStatus() + " " + alert.getCheck().getCheckType() + " " + alert.getCheck().getName());
            if (alert.getCheck() instanceof ServiceMO) System.out.print(" on host " + ((ServiceMO) alert.getCheck()).getHost().getName());
            if (alert.getCheck() instanceof TrapMO) System.out.print(" on host " + ((TrapMO) alert.getCheck()).getHost().getName());
            if (alert.getCheck() instanceof ResourceMO) System.out.print(" on cluster " + ((ResourceMO) alert.getCheck()).getCluster().getName());
            System.out.print(" raised at " + DATE_FORMAT.format(new Date(alert.getRaised())));
            System.out.println(" - " + alert.getOutput().replace("\r", "").replace("\n", "\\n"));
        }
        return 0;
    }
}

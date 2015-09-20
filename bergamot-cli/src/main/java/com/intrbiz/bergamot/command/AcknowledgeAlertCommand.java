package com.intrbiz.bergamot.command;

import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.intrbiz.Util;
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

public class AcknowledgeAlertCommand extends BergamotCLICommand
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    
    public AcknowledgeAlertCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "acknowledge-alert";
    }

    @Override
    public String usage()
    {
        return "<site-name> <alert-id> <summary>";
    }

    @Override
    public String help()
    {
        return "Acknowledge the given alert\n" +
                "\n" +
                "Arguments:\n" +
                "  <site-name> a configured site name, eg: 'bergamot.local'\n" +
                "  <alert-id> the UUID of the alert to acknowledge\n" +
                "  <summary> the summary of your acknowledgement\n" +
                "\n" +
                "Optionally a comment can be provided to std-in\n" +
                "\n";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        if (args.isEmpty()) throw new BergamotCLIException("No site name given");
        String siteName = args.remove(0);
        CLISiteCfg site = CLICfg.loadConfiguration().getSite(siteName);
        if (site == null) throw new BergamotCLIException("No site configured with the name '" + siteName + "'");
        // alert id
        if (args.isEmpty()) throw new BergamotCLIException("No alert id given");
        UUID alertId = UUID.fromString(args.remove(0));
        // summary
        StringBuilder summary = new StringBuilder();
        boolean ns = false;
        for (String arg : args)
        {
            if (ns) summary.append(" ");
            summary.append(arg);
            ns = true;
        }
        if (Util.isEmpty(summary.toString().trim())) throw new BergamotCLIException("No summary given");
        // comment
        StringBuilder comment = new StringBuilder();
        // only read a comment from std-in when in non-interactive mode
        if (System.console() == null)
        {
            char[] buffer = new char[1024];
            int l;
            try (Reader in = new InputStreamReader(System.in))
            {
                while ((l = in.read(buffer)) != -1)
                    comment.append(buffer, 0, l);
            }
        }
        // connect to the API
        BergamotClient client = new BergamotClient(site.getUrl(), site.getAuthToken());
        // acknowledge the alert
        AlertMO alert = client.callAcknowledgeAlert().id(alertId).summary(summary.toString()).comment(comment.toString()).execute();
        // print the acked alert
        System.out.print("Acknowledged Alert [" + alert.getId() + "] - " + alert.getStatus() + " " + alert.getCheck().getCheckType() + " " + alert.getCheck().getName());
        if (alert.getCheck() instanceof ServiceMO) System.out.print(" on host " + ((ServiceMO) alert.getCheck()).getHost().getName());
        if (alert.getCheck() instanceof TrapMO) System.out.print(" on host " + ((TrapMO) alert.getCheck()).getHost().getName());
        if (alert.getCheck() instanceof ResourceMO) System.out.print(" on cluster " + ((ResourceMO) alert.getCheck()).getCluster().getName());
        System.out.print(" raised at " + DATE_FORMAT.format(new Date(alert.getRaised())));
        System.out.println(" - " + alert.getOutput().replace("\r", "").replace("\n", "\\n"));
        return 0;
    }
}

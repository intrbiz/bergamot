package com.intrbiz.bergamot.command;

import java.util.List;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.BergamotCLIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.config.CLICfg;
import com.intrbiz.bergamot.config.CLISiteCfg;

public class SiteXMLCommand extends BergamotCLICommand
{
    public SiteXMLCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "site.xml";
    }

    @Override
    public String usage()
    {
        return "<site-name>";
    }

    @Override
    public String help()
    {
        return "Export the complete site configuration\n" +
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
        System.out.println(client.callBuildSiteConfig().execute().toString());
        return 0;
    }
}

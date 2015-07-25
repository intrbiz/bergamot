package com.intrbiz.bergamot.command;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.BergamotCLIException;
import com.intrbiz.bergamot.BergamotClient;
import com.intrbiz.bergamot.config.CLICfg;
import com.intrbiz.bergamot.config.CLISiteCfg;
import com.intrbiz.bergamot.config.model.BergamotCfg;

public class ApplyConfigChangeCommand extends BergamotCLICommand
{
    public ApplyConfigChangeCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "apply-config-change";
    }

    @Override
    public String usage()
    {
        return "<site-name> <config-file>";
    }

    @Override
    public String help()
    {
        return "Apply the configuration change in the given file to a Bergamot Monitoring site\n" +
                "  Eg: bergamot-cli apply-config-change 'change.xml'\n" +
                "\n" +
                "Arguments:\n" +
                "  <site-name> a configured site name, eg: 'bergamot.local'\n" +
                "  <config-file> the path of the file containing the Bergamot configuration change, Eg: 'change.xml'\n";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        // site name
        if (args.size() < 1) throw new BergamotCLIException("No site name given");
        String siteName = args.remove(0);
        CLISiteCfg site = CLICfg.loadConfiguration().getSite(siteName);
        if (site == null) throw new BergamotCLIException("No site configured with the name '" + siteName + "'");
        // config file
        if (args.size() < 1) throw new BergamotCLIException("No configuration file given");
        File confFile = new File(args.get(0));
        if (! (confFile.isFile() && confFile.exists())) throw new BergamotCLIException("The path '" + confFile.getAbsolutePath() + "' is not a file!");
        // load the config
        BergamotCfg configChange = BergamotCfg.read(BergamotCfg.class, new FileReader(confFile));
        // connect to the API
        BergamotClient client = new BergamotClient(site.getUrl(), site.getAuthToken());
        // apply the change
        System.out.println(client.applyConfigChange().configChange(configChange).execute());
        return 0;
    }
}

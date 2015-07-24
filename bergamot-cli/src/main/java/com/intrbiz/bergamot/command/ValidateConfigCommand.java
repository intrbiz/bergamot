package com.intrbiz.bergamot.command;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.BergamotCLIException;
import com.intrbiz.bergamot.config.BergamotConfigReader;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;

public class ValidateConfigCommand extends BergamotCLICommand
{
    public ValidateConfigCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "validate-config";
    }

    @Override
    public String usage()
    {
        return "<config-dir>";
    }

    @Override
    public String help()
    {
        return "Parse and validate the Bergamot configuration in the given directory\n" +
                "  Eg: bergamot-cli validate-config '/etc/bergamot/config/bergamot.local/'\n" +
                "\n" +
                "Arguments:\n" +
                "  <config-dir> the path of the directory containing the Bergamot configuration, Eg: '/etc/bergamot/config/bergamot.local'\n";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        if (args.size() != 1) throw new BergamotCLIException("No configuration directory given");
        // the config dir
        File confDir = new File(args.get(0));
        if (! confDir.isDirectory()) throw new BergamotCLIException("The path '" + confDir.getAbsolutePath() + "' is not a directory!");
        // load the config
        Collection<ValidatedBergamotConfiguration> bcfgs = new BergamotConfigReader().includeDir(confDir).build();
        // assert the configuration is valid
        for (ValidatedBergamotConfiguration vbcfg : bcfgs)
        {
            System.out.println(vbcfg.getReport().toString());
        }
        // all ok
        return 0;
    }
}

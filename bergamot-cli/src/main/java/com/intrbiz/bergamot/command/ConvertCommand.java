package com.intrbiz.bergamot.command;

import java.io.File;
import java.util.List;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.BergamotCLIException;
import com.intrbiz.bergamot.NagiosToBergamot;

public class ConvertCommand extends BergamotCLICommand
{
    public ConvertCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "convert";
    }

    @Override
    public String usage()
    {
        return "<nagios-config-dir>";
    }

    @Override
    public String help()
    {
        return "Convert Nagios configuration to Bergamot configuration\n" +
                "\n" +
                "For each Nagios configuration file an equvilant Bergamot configuration file.\n" +
                "The resultant configuration will be valid, however due to the differences in \n" +
                "how Bergamot and Nagios work will not be ideal.  As such it is reccomended to \n" +
                "and modify the outputted Bergamot configuration.\n" +
                "\n" +
                "Running this command will not modify the existing Nagios configuration in any way.\n" +
                "\n" +
                "Arguments:\n" +
                "  <nagios-config-dir> a directory containg Nagios object configuration files\n" +
                "\n";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        if (args.size() != 1) throw new BergamotCLIException("No Nagios configuration directory given.");
        File file = new File(args.get(0));
        if (! file.isDirectory()) throw new BergamotCLIException("The path given is not a directory.");
        // convert
        NagiosToBergamot.convert(file);
        return 0;
    }
}

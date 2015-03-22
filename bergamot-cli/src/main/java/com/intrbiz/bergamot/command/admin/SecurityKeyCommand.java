package com.intrbiz.bergamot.command.admin;

import java.util.List;

import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;
import com.intrbiz.bergamot.BergamotCLIException;
import com.intrbiz.bergamot.config.UICfg;
import com.intrbiz.crypto.SecretKey;

public class SecurityKeyCommand extends BergamotCLICommand
{
    public SecurityKeyCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "security-key";
    }
    
    @Override
    public boolean admin()
    {
        return true;
    }

    @Override
    public String usage()
    {
        return "(generate|set)";
    }

    @Override
    public String help()
    {
        return "Manage Bergamot UI installation security keys\n" +
                "\n" +
                "Commands:\n" +
                "  generate - generate a new security key\n" +
                "  set - generate a new security key and update this servers UI configuration file\n" +
                "\n";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args) throws Exception
    {
        if (args.size() < 1) throw new BergamotCLIException("No command given");
        String command = args.remove(0);
        if ("generate".equalsIgnoreCase(command))
        {
            SecretKey key = SecretKey.generate();
            System.out.println(key.toString());
            return 0;
        }
        else if ("set".equalsIgnoreCase(command))
        {
            try
            {
                UICfg config = UICfg.loadConfiguration();
                SecretKey key = SecretKey.generate();
                config.setSecurityKey(key.toString());
                config.saveConfiguration();
                return 0;
            }
            catch (Exception e)
            {
                throw new BergamotCLIException("Failed to set UI security key: " + e.getMessage(), e);    
            }
        }
        else
        {
            throw new BergamotCLIException("Unknown sub command: " + command);
        }
    }
}

package com.intrbiz.bergamot;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.bergamot.command.AcknowledgeAlertCommand;
import com.intrbiz.bergamot.command.AgentCommand;
import com.intrbiz.bergamot.command.AlertsCommand;
import com.intrbiz.bergamot.command.ApplyConfigChangeCommand;
import com.intrbiz.bergamot.command.ConfigCommand;
import com.intrbiz.bergamot.command.ConvertCommand;
import com.intrbiz.bergamot.command.HelpCommand;
import com.intrbiz.bergamot.command.SiteXMLCommand;
import com.intrbiz.bergamot.command.TestCommand;
import com.intrbiz.bergamot.command.ValidateConfigCommand;
import com.intrbiz.bergamot.command.admin.AddSiteAliasCommand;
import com.intrbiz.bergamot.command.admin.CreateSiteCommand;
import com.intrbiz.bergamot.command.admin.DBImportConfigCommand;
import com.intrbiz.bergamot.command.admin.DBInstallCommand;
import com.intrbiz.bergamot.command.admin.DBVersionCommand;
import com.intrbiz.bergamot.command.admin.DiscoverDaemonsCommand;
import com.intrbiz.bergamot.command.admin.GenerateSiteCAsCommand;
import com.intrbiz.bergamot.command.admin.KillDaemonCommand;
import com.intrbiz.bergamot.command.admin.ListSitesCommand;
import com.intrbiz.bergamot.command.admin.SecurityKeyCommand;
import com.intrbiz.bergamot.command.admin.ServerCommand;
import com.intrbiz.bergamot.command.admin.UnjoinDaemonCommand;

public class BergamotCLI
{
    private Map<String, BergamotCLICommand> commands = new LinkedHashMap<String, BergamotCLICommand>();

    private boolean adminMode = false;

    public BergamotCLI()
    {
        super();
        // setup commands
        // help command
        this.addCommand(new HelpCommand());
        // more generic commands
        this.addCommand(new ConfigCommand());
        this.addCommand(new ConvertCommand());
        this.addCommand(new ValidateConfigCommand());
        this.addCommand(new TestCommand());
        this.addCommand(new SiteXMLCommand());
        this.addCommand(new AlertsCommand());
        this.addCommand(new AcknowledgeAlertCommand());
        this.addCommand(new AgentCommand());
        this.addCommand(new ApplyConfigChangeCommand());
        // local commands
        this.addCommand(new CreateSiteCommand());
        this.addCommand(new ListSitesCommand());
        this.addCommand(new DBVersionCommand());
        this.addCommand(new DBInstallCommand());
        this.addCommand(new AddSiteAliasCommand());
        this.addCommand(new DBImportConfigCommand());
        this.addCommand(new GenerateSiteCAsCommand());
        this.addCommand(new ServerCommand());
        this.addCommand(new SecurityKeyCommand());
        this.addCommand(new DiscoverDaemonsCommand());
        this.addCommand(new KillDaemonCommand());
        this.addCommand(new UnjoinDaemonCommand());
    }

    public void addCommand(BergamotCLICommand command)
    {
        this.commands.put(command.name(), command);
    }

    @SuppressWarnings("unchecked")
    public <T extends BergamotCLICommand> T getCommand(String name)
    {
        BergamotCLICommand command = this.commands.get(name);
        if (command != null && (!"help".equals(name)) && command.admin() != this.isAdminMode()) return null;
        return (T) command;
    }

    public List<BergamotCLICommand> getCommands()
    {
        return this.commands.values().stream().filter((c) -> { return "help".equals(c.name()) || c.admin() == this.isAdminMode(); }).collect(Collectors.toList());
    }

    public boolean isAdminMode()
    {
        return adminMode;
    }

    public void setAdminMode(boolean adminMode)
    {
        this.adminMode = adminMode;
    }

    public int execute(List<String> args)
    {
        String commandName = "help";
        // get the command name
        if (! args.isEmpty())
        {
            commandName = args.remove(0);
            // check for admin prefix
            if ("admin".equals(commandName))
            {
                this.adminMode = true;
                // get the command name
                commandName = "help";
                if (! args.isEmpty())
                {
                    commandName = args.remove(0);
                }
            }
        }
        // get the command
        BergamotCLICommand command = this.getCommand(commandName);
        //
        if (command == null)
        {
            System.err.println("Error: Invalid command '" + commandName + "'");
            return -1;
        }
        // execute
        try
        {
            return command.execute(this, args);
        }
        catch (BergamotCLIException e)
        {
            System.err.println("Error: " + e.getMessage());
        }
        catch (Exception e)
        {
            System.err.println("Error: Unhandled error!");
            e.printStackTrace();
        }
        return -1;
    }

    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ERROR);
        System.exit(new BergamotCLI().execute(new LinkedList<String>(Arrays.asList(args))));
    }
}

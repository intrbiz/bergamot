package com.intrbiz.bergamot.command;

import java.util.List;

import com.intrbiz.Util;
import com.intrbiz.bergamot.BergamotCLI;
import com.intrbiz.bergamot.BergamotCLICommand;

public class HelpCommand extends BergamotCLICommand
{
    public HelpCommand()
    {
        super();
    }

    @Override
    public String name()
    {
        return "help";
    }

    @Override
    public String usage()
    {
        return "[<command>]";
    }

    @Override
    public String help()
    {
        return "Display Bergamot CLI command help";
    }

    @Override
    public int execute(BergamotCLI cli, List<String> args)
    {
        if (args.size() != 1)
        {
            this.printCommands(cli);
        }
        else
        {
            String commandName = args.remove(0);
            BergamotCLICommand command = cli.getCommand(commandName);
            if (command != null)
            {
                this.printCommand(cli, command);
            }
            else
            {
                this.printCommands(cli);    
            }
        }
        return 0;
    }
    
    private void printCommand(BergamotCLI cli, BergamotCLICommand command)
    {
        System.out.println("Bergamot CLI - Help");
        System.out.println("Usage:");
        System.out.println("bergamot-cli " + (cli.isAdminMode() ? "admin " : "") + command.name() + " " + command.usage());
        System.out.println();
        System.out.println(command.help());
        System.out.println("Note: remember to quote all arguments");
    }
    
    private void printCommands(BergamotCLI cli)
    {
        System.out.println("Bergamot CLI - Help");
        System.out.println("Commands:");
        // the col width
        int colWidth = cli.getCommands().stream().mapToInt((c) -> { return ((cli.isAdminMode() ? "admin " : "") + c.name() + " " + c.usage()).length(); }).max().getAsInt();
        for (BergamotCLICommand command : cli.getCommands())
        {
            System.out.print("  ");
            System.out.print(Util.rpadTo((cli.isAdminMode() ? "admin " : "") + command.name() + " " + command.usage(), ' ', colWidth + 4));
            System.out.print(command.shortHelp());
            System.out.println();
        }
        System.out.println();
        System.out.println("For further detail about a specific command use:");
        System.out.println("  bergamot-cli help <command>");
        System.out.println();
        System.out.println("Note: remember to quote all arguments");
    }
}

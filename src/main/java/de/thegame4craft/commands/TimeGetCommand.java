package de.thegame4craft.commands;

import net.minestom.server.command.builder.Command;

public class TimeGetCommand extends Command {
    public TimeGetCommand() {
        super("get");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /time get");
        });

        addSubcommand(new TimeGetDayCommand());
        addSubcommand(new TimeGetDaytimeCommand());
        addSubcommand(new TimeGetGametimeCommand());
    }
}

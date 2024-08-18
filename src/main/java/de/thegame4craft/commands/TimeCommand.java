package de.thegame4craft.commands;

import net.minestom.server.command.builder.Command;

public class TimeCommand extends Command {
    public TimeCommand() {
        super("time");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /time <action>");
        });

        addSubcommand(new TimeSetCommand());
        addSubcommand(new TimeAddCommand());
        addSubcommand(new TimeGetCommand());
    }
}


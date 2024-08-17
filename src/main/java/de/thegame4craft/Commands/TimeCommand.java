package de.thegame4craft.Commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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


package de.thegame4craft.Commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

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

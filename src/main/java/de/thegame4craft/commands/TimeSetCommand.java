package de.thegame4craft.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public class TimeSetCommand extends Command {
    public TimeSetCommand() {
        super("set");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /time set <time>");
        });

        addSyntax((sender, context) -> {
            int time = context.get("time");
            for (@NotNull Instance c : MinecraftServer.getInstanceManager().getInstances()) {
                c.setTime(time % 24000);
            }
            sender.sendMessage("Time set to " + time % 24000);
            // Set time
        }, ArgumentType.Integer("time"));
    }
}

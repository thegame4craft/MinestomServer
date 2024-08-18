package de.thegame4craft.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class TimeGetDaytimeCommand extends Command {
    public TimeGetDaytimeCommand() {
        super("daytime");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /time get daytime");
        });

        addSyntax((sender, context) -> {
            if(sender instanceof Player) {
                sender.sendMessage("Time is " + ((Player)sender).getInstance().getTime() % 24000);
            } else {
                sender.sendMessage("Time is (server time) " + MinecraftServer.getInstanceManager().getInstances().iterator().next().getTime() % 24000);
            }
        });
    }
}

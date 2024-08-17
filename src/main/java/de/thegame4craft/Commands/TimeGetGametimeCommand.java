package de.thegame4craft.Commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class TimeGetGametimeCommand extends Command {
    public TimeGetGametimeCommand() {
        super("gametime");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /time get gametime");
        });

        addSyntax((sender, context) -> {
            if(sender instanceof Player) {
                sender.sendMessage("Time is " + ((Player)sender).getInstance().getTime());
            } else {
                sender.sendMessage("Time is (server time) " + MinecraftServer.getInstanceManager().getInstances().iterator().next().getTime());
            }
        });
    }
}

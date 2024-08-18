package de.thegame4craft.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class TimeGetDayCommand extends Command {
    public TimeGetDayCommand() {
        super("day");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /time get day");
        });

        addSyntax((sender, context) -> {
            if(sender instanceof Player p) {
                sender.sendMessage("Day " + Math.round(Math.floor((double)p.getInstance().getTime() / 24000)));
            } else {
                final double time = MinecraftServer.getInstanceManager().getInstances().iterator().next().getTime();
                sender.sendMessage("Day " + Math.round(Math.floor(time / 24000)));
            }
        });
    }
}

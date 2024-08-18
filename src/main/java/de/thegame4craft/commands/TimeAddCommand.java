package de.thegame4craft.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public class TimeAddCommand extends Command {
    public TimeAddCommand() {
        super("add");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /time add <time>");
        });

        addSyntax((sender, context) -> {
            int time = context.get("time");
            for (@NotNull Instance c : MinecraftServer.getInstanceManager().getInstances()) {
                c.setTime(c.getTime() + time);
                if(sender instanceof Player p) {
                    if(p.getInstance().getUniqueId().equals(c.getUniqueId())) {
                        p.sendMessage("Time set to " + c.getTime() % 24000);
                    }
                }
            }
            if(!(sender instanceof Player)) { // If sender is not a player
                sender.sendMessage("Time added " + time);
            }
        }, ArgumentType.Integer("time"));
    }
}

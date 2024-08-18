package de.thegame4craft.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.instance.Instance;

public class StopCommand extends Command {

        public StopCommand() {
            super("stop");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("Stopping server...");
                MinecraftServer.getServer().stop();
                MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.kick("Server is shutting down"));
                for (Instance i : MinecraftServer.getInstanceManager().getInstances()) {
                    i.saveChunksToStorage();
                    i.saveInstance();
                }
                MinecraftServer.stopCleanly();
                System.exit(0);
            });
        }
}

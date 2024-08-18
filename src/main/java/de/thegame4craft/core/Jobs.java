package de.thegame4craft.core;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;

public class Jobs {
    public static void start() {
        final Scheduler scheduler = new Scheduler();
        scheduler.schedule(() -> {
            for (Instance i : MinecraftServer.getInstanceManager().getInstances()) {
                i.saveChunksToStorage();
                i.saveInstance();
            }
        }, new TimeInterval(0, 0, 1));
    }
}

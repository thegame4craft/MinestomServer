package de.thegame4craft;

import de.thegame4craft.commands.FillCommand;
import de.thegame4craft.commands.GameModeCommand;
import de.thegame4craft.world.WorldGeneration;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerGameModeRequestEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Online());
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        Instance instance = instanceManager.createInstanceContainer();
        instance.setChunkSupplier(LightingChunk::new);

        WorldGeneration.generateWorld(instance);
        MinecraftServer.getCommandManager().register(new GameModeCommand());
        MinecraftServer.getCommandManager().register(new FillCommand());

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instance);
        });

        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            event.getPlayer().setPermissionLevel(4);
            event.getPlayer().teleport(Pos.ZERO.add(0, 128, 0));
            event.getPlayer().setGameMode(GameMode.CREATIVE);
        });

        globalEventHandler.addListener(PlayerGameModeRequestEvent.class, event -> {
            event.getPlayer().setGameMode(event.getRequestedGameMode());
        });

        minecraftServer.start("0.0.0.0", 25565);
    }
}

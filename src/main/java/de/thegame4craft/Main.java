package de.thegame4craft;

import de.thegame4craft.commands.GameModeCommand;
import de.thegame4craft.commands.StopCommand;
import de.thegame4craft.commands.TimeCommand;
import de.thegame4craft.core.*;
import de.thegame4craft.events.ItemPickupEvent;
import de.thegame4craft.events.PlayerJoinEvent;
import de.thegame4craft.util.FileSystemHelper;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public class Main {
    private final Logger logger = LoggerFactory.getLogger(Main.class);
    private static Main INSTANCE;
    public WorldManager worldManager;

    static {
        PropertiesManager.generateServerProperties(Path.of("server.properties"));
    }

    public static void main(String[] args) {
        if(INSTANCE != null) {
            throw new IllegalStateException("Main class already initialized");
        }
        INSTANCE = new Main();
        INSTANCE.init();
    }

    private void checkDirs() {
        Path players = Path.of("players");
        FileSystemHelper.createFolderIfNotExists(players.toFile());
        final Path permissionFile = Path.of("permissions.json");
        FileSystemHelper.createFileIfNotExists(permissionFile.toFile(), "{}");
    }

    private void init() {
        checkDirs();
        MinecraftServer minecraftServer = MinecraftServer.init();
        PermissionManager.init();

        worldManager = new WorldManager();
        createDefaultWorld();
        worldManager.saveWorlds();

        Jobs.start();
        Registry.eventRegistry();
        UUIDProvider.initProvider();
        Registry.registerCommands();

        minecraftServer.start("0.0.0.0", 25565);
    }

    private void createDefaultWorld() {
        if(worldManager.countWorlds() <= 0) {
            if(!worldManager.isValidWorld("world")) { // delete world if not valid
                Path world = Path.of("worlds", "world");
                FileSystemHelper.deleteFolder(world.toFile());
            }
            InstanceContainer c = worldManager.createWorld("world");
            if(c == null) {
                logger.error("Failed to create world");
                return;
            }
            c.setGenerator(unit -> {
                final Point start = unit.absoluteStart();
                final Point size = unit.size();
                for (int x = 0; x < size.blockX(); x++) {
                    for (int z = 0; z < size.blockZ(); z++) {
                        for (int y = 0; y < Math.min(40 - start.blockY(), size.blockY()); y++) {
                            unit.modifier().setBlock(start.add(x, y, z), Block.STONE);
                        }
                    }
                }
            });
            worldManager.generateChunks(c);
            worldManager.saveWorld(c);
        }
    }
}

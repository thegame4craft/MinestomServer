package de.thegame4craft;

import de.thegame4craft.commands.GameModeCommand;
import de.thegame4craft.commands.StopCommand;
import de.thegame4craft.commands.TimeCommand;
import de.thegame4craft.core.Jobs;
import de.thegame4craft.core.PermissionManager;
import de.thegame4craft.core.PropertiesManager;
import de.thegame4craft.core.WorldManager;
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
    public final PropertiesManager propertiesManager = new PropertiesManager(Path.of("server.properties"));

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
        if(!players.toFile().exists()) {
            if(!players.toFile().mkdirs()) {
                logger.error("Failed to create players directory");
            }
        }
        final Path permissionFile = Path.of("permissions.json");
        if(!permissionFile.toFile().exists()) {
            try {
                Files.writeString(permissionFile, "{}");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void init() {
        checkDirs();
        MinecraftServer minecraftServer = MinecraftServer.init();
        PermissionManager.init();
        worldManager = new WorldManager();
        createDefaultWorld();

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, PlayerJoinEvent::execute);
        globalEventHandler.addListener(PlayerSpawnEvent.class, PlayerJoinEvent::phase2);
        globalEventHandler.addListener(PlayerMoveEvent.class, de.thegame4craft.events.PlayerMoveEvent::execute);
        globalEventHandler.addListener(PlayerBlockPlaceEvent.class, de.thegame4craft.events.PlayerBlockPlaceEvent::execute);
        globalEventHandler.addListener(PlayerDisconnectEvent.class, playerDisconnectEvent -> {
            worldManager.saveWorlds();
        });

        globalEventHandler.addListener(PlayerBlockBreakEvent.class, event -> {
            if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                var material = event.getBlock().registry().material();
                if(material != null) {
                    ItemStack itemStack = ItemStack.of(material);
                    ItemEntity itemEntity = new ItemEntity(itemStack);
                    itemEntity.setInstance(event.getPlayer().getInstance(), event.getBlockPosition().add(0.5, 0.5, 0.5));
                    itemEntity.setPickupDelay(Duration.ofMillis(500));
                }
            }
        });
        globalEventHandler.addListener(PickupItemEvent.class, event -> {
            ItemStack itemStack = event.getItemEntity().getItemStack();
            if(event.getLivingEntity() instanceof Player player) {
                player.getInventory().addItemStack(itemStack);
            }
        });

        worldManager.saveWorlds();
        Jobs.start();
        MinecraftServer.getConnectionManager()
            .setUuidProvider((playerConnection, username) -> {
                try {
                    final Path playerPath = Path.of("players.json");
                    if(!playerPath.toFile().exists()) {
                        Files.writeString(playerPath, "{}");
                    }
                    JSONObject obj = new JSONObject(Files.readString(playerPath));
                    if(obj.has(username)) {
                        return UUID.fromString(obj.getString(username));
                    } else {
                        UUID uuid = UUID.randomUUID();
                        obj.put(username, uuid.toString());
                        Files.writeString(playerPath, obj.toString());
                        return uuid;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        registerCommands();
        minecraftServer.start("0.0.0.0", 25565);
    }

    private void registerCommands() {
        MinecraftServer.getCommandManager().register(new TimeCommand());
        MinecraftServer.getCommandManager().register(new GameModeCommand());
        MinecraftServer.getCommandManager().register(new StopCommand());
    }

    private void createDefaultWorld() {
        if(worldManager.countWorlds() <= 0) {
            if(!worldManager.isValidWorld("world")) {
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

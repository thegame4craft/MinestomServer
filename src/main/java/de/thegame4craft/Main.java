package de.thegame4craft;

import de.thegame4craft.Commands.TimeCommand;
import de.thegame4craft.Core.Scheduler;
import de.thegame4craft.Core.TimeInterval;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


@SuppressWarnings("UnstableApiUsage")
public class Main {
    private final Logger logger = LoggerFactory.getLogger(Main.class);
    public static Main INSTANCE;
    public InstanceContainer container;

    public static void main(String[] args) {
        if(INSTANCE != null) {
            throw new IllegalStateException("Main class already initialized");
        }
        INSTANCE = new Main();
        INSTANCE.init();
    }

    private void checkDirs() {
        Path worlds = Path.of("worlds", "world", "region");
        if(!worlds.toFile().exists()) {
            if(!worlds.toFile().mkdirs()) {
                logger.error("Failed to create world & region directory");
            }
        }
    }

    private void init() {
        checkDirs();
        MinecraftServer minecraftServer = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        container = instanceManager.createInstanceContainer();
        Scheduler scheduler = new Scheduler();
        AnvilLoader loader = new AnvilLoader(Path.of("worlds/world"));
        container.setChunkLoader(loader);
        container.setChunkSupplier(LightingChunk::new);

        container.setGenerator(unit -> {
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
        generate();

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(container);
            player.setRespawnPoint(new Pos(0, 42, 0));
            player.setGameMode(GameMode.CREATIVE);
        });
        loader.saveChunks(container.getChunks());
        loader.saveInstance(container);

        scheduler.schedule(() -> {
            loader.saveChunks(container.getChunks());
            System.out.println("Saved chunks");
        }, new TimeInterval(0, 0, 0, 30));

        registerCommands();
        minecraftServer.start("0.0.0.0", 25565);
    }

    private void generate() {
        var chunks = new ArrayList<CompletableFuture<Chunk>>();
        ChunkUtils.forChunksInRange(0, 0, 32, (x, z) -> chunks.add(container.loadChunk(x, z)));

        CompletableFuture.runAsync(() -> {
            CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
            LightingChunk.relight(container, container.getChunks());
        });
    }

    private void registerCommands() {
        MinecraftServer.getCommandManager().register(new TimeCommand());
    }
}

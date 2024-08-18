package de.thegame4craft.core;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.*;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class WorldManager {
    private final Logger logger = LoggerFactory.getLogger(WorldManager.class);
    private final HashMap<String, InstanceContainer> containers = new HashMap<>();

    private final Path worldPath = Path.of("worlds");

    public static WorldManager INSTANCE = null;

    public WorldManager() {
        if(INSTANCE != null) {
            throw new IllegalStateException("WorldManager already initialized");
        }
        INSTANCE = this;
        checkDirs();
        loadWorlds();
    }

    private void checkDirs() {
        Path worlds = Path.of("worlds", "world", "region");
        if(!worlds.toFile().exists()) {
            if(!worlds.toFile().mkdirs()) {
                logger.error("Failed to create world & region directory");
            }
        }
    }

    private HashMap<String, InstanceContainer> getContainers() {
        return containers;
    }

    private void loadWorlds() {
        if(!worldPath.toFile().exists()) {
            logger.error("Worlds directory does not exist");
            return;
        }
        for (File p : Objects.requireNonNull(worldPath.toFile().listFiles())) {
            if(p.isDirectory()) {
                loadWorld(p);
            }
        }
    }

    public int countWorlds() {
        if(!worldPath.toFile().exists()) {
            logger.error("Worlds directory does not exist");
            return -1;
        }
        int count = 0;
        for (File p : Objects.requireNonNull(worldPath.toFile().listFiles())) {
            if(isValidWorld(p.getName())) count++;
        }
        return count;
    }

    public boolean isValidWorld(String name) {
        if(!Path.of(worldPath.toString(), name).toFile().isDirectory()) return false;
        Path region = Path.of(worldPath.toString(), name, "region");
        if(!region.toFile().isDirectory()) return false;
        return Objects.requireNonNull(region.toFile().listFiles()).length != 0;
    }

    public void saveWorlds() {
        for (Instance i : MinecraftServer.getInstanceManager().getInstances()) {
            i.saveChunksToStorage();
            i.saveInstance();
        }
    }

    public InstanceContainer getWorld(String name) {
        if(containers.containsKey(name)) {
            return containers.get(name);
        }
        if(!worldPath.resolve(name).toFile().exists()) {
            logger.error("World does not exist");
            return null;
        }
        return loadWorld(worldPath.resolve(name).toFile());
    }

    public @Nullable String getWorldNameByUUID(UUID uuid) {
        for(Map.Entry<String, InstanceContainer> e : getContainers().entrySet()) {
            if(e.getValue().getUniqueId() == uuid) return e.getKey();
        }
        return null;
    }
    public @Nullable InstanceContainer getWorldByUUID(UUID uuid) {
        for(Map.Entry<String, InstanceContainer> e : getContainers().entrySet()) {
            if(e.getValue().getUniqueId() == uuid) return e.getValue();
        }
        return null;
    }


    private InstanceContainer loadWorld(File p) {
        InstanceContainer container = MinecraftServer.getInstanceManager().createInstanceContainer();
        container.setChunkLoader(new AnvilLoader(p.toPath()));
        container.setChunkSupplier(LightingChunk::new);
        containers.put(p.getName(), container);
        return container;
    }

    public InstanceContainer createWorld(String name) {
        Path world = worldPath.resolve(name);
        if(world.toFile().exists()) {
            logger.error("World already exists");
            return null;
        }
        if(!world.toFile().mkdirs()) {
            logger.error("Failed to create world directory");
            return null;
        }
        InstanceContainer container = MinecraftServer.getInstanceManager().createInstanceContainer();
        container.setChunkLoader(new AnvilLoader(world));
        container.setChunkSupplier(LightingChunk::new);
        containers.put(name, container);
        return container;
    }

    public void generateChunks(InstanceContainer container, int chunkX, int chunkZ, int range) {
        var chunks = new ArrayList<CompletableFuture<Chunk>>();
        ChunkUtils.forChunksInRange(chunkX, chunkZ, range, (x, z) -> chunks.add(container.loadChunk(x, z)));

        CompletableFuture.runAsync(() -> {
            CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
            LightingChunk.relight(container, container.getChunks());
        });
    }

    public void generateChunks(InstanceContainer container) {
        generateChunks(container, 0, 0, 32);
    }

    public void generateChunks(InstanceContainer container, int range) {
        generateChunks(container, 0, 0, range);
    }

    public void saveWorld(InstanceContainer c) {
        c.saveChunksToStorage();
        c.saveInstance();
    }
}

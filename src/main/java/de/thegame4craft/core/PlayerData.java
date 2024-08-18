package de.thegame4craft.core;

import com.moandjiezana.toml.TomlWriter;
import de.thegame4craft.Main;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlayerData {
    public String name;
    public String world;
    public Pos respawnPoint;
    public GameMode gameMode;
    public boolean isFlying;
    public boolean isOP;

    public PlayerData(String name, String world, Pos respawnPoint, GameMode gameMode, boolean isFlying, boolean isOP) {
        this.name = name;
        this.world = world;
        this.respawnPoint = respawnPoint;
        this.gameMode = gameMode;
        this.isFlying = isFlying;
        this.isOP = isOP;
    }

    public static void create(Player player, Path playerFile) {
        TomlWriter writer = new TomlWriter();
        final PropertiesManager propertiesManager = new PropertiesManager(Path.of("server.properties"));
        PlayerData data = new PlayerData(player.getUsername(),propertiesManager.getProperty("player.spawn_world"),
                player.getRespawnPoint(), player.getGameMode(), player.isFlying(), player.hasPermission("server.op"));
        String content = writer.write(data);

        try {
            Files.writeString(playerFile, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

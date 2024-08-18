package de.thegame4craft.events;

import de.thegame4craft.Main;
import de.thegame4craft.core.PermissionManager;
import de.thegame4craft.core.PlayerData;
import de.thegame4craft.core.PropertiesManager;
import de.thegame4craft.core.WorldManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.permission.Permission;

import java.io.IOException;
import java.nio.file.Path;

public class PlayerJoinEvent {
    private static final PropertiesManager propertiesManager = new PropertiesManager(Path.of("server.properties"));
    public static void execute(AsyncPlayerConfigurationEvent event) {
        final Player player = event.getPlayer();
        event.setSpawningInstance(WorldManager.INSTANCE
                .getWorld(propertiesManager.getProperty("player.spawn_world")));

        Path playerFile = Path.of("players", player.getUuid().toString() + ".toml");
        PermissionManager pm = PermissionManager.init();
        pm.addPermission(player.getUuid(), "minecraft.command.gamemode");
        /*try {
            pm.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        player.setPermissionLevel(4);

        if(playerFile.toFile().exists()) {
            PropertiesManager playerProperties = new PropertiesManager(playerFile);
            player.setGameMode(GameMode.valueOf(playerProperties.getProperty("gameMode")));
            player.setRespawnPoint(new Pos(
                    playerProperties.getDouble("respawnPoint.x"),
                    playerProperties.getDouble("respawnPoint.y"),
                    playerProperties.getDouble("respawnPoint.z"),
                    playerProperties.getFloat("respawnPoint.yaw"),
                    playerProperties.getFloat("respawnPoint.pitch")
            ));
            
            if(playerProperties.getBoolean("isOP")) {
                player.addPermission(new Permission("server.op"));
            }

        } else {
            player.setRespawnPoint(new Pos(0, 42, 0));
            player.setGameMode(GameMode.SURVIVAL);

            PlayerData.create(player, playerFile);
        }
    }

    public static void phase2(net.minestom.server.event.player.PlayerSpawnEvent event) {
        final Player player = event.getPlayer();
        Path playerFile = Path.of("players", player.getUuid().toString() + ".toml");
        PropertiesManager playerProperties = new PropertiesManager(playerFile);
        try {
            GameMode gm = GameMode.valueOf(playerProperties.getProperty("gameMode"));
            player.setAllowFlying(!gm.canTakeDamage());
            player.setFlying(playerProperties.getBoolean("isFlying"));
        } catch (Exception e) {
            e.printStackTrace(System.out);
            // player.setFlying(false);
        }
    }
}

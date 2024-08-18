package de.thegame4craft.events;

import com.moandjiezana.toml.Toml;

import java.io.IOException;
import java.nio.file.Path;

import com.moandjiezana.toml.TomlWriter;
import de.thegame4craft.core.PlayerData;
import de.thegame4craft.core.WorldManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;

public class PlayerMoveEvent {
    public static void execute(net.minestom.server.event.player.PlayerMoveEvent event) {
        Path playerFile = Path.of("players", event.getPlayer().getUuid() + ".toml");
        if(!playerFile.toFile().exists()) {
            PlayerData.create(event.getPlayer(), playerFile);
        }
        if(!playerFile.toFile().exists()) {
            MinecraftServer.getExceptionManager().handleException(new IOException("Failed to create player file"));
            return;
        }
        Toml toml = new Toml().read(playerFile.toFile());
        PlayerData data = toml.to(PlayerData.class);
        data.respawnPoint = new Pos(event.getNewPosition().x(),
                event.getNewPosition().y(),
                event.getNewPosition().z(),
                event.getNewPosition().yaw(),
                event.getNewPosition().pitch());
        data.gameMode = event.getPlayer().getGameMode();
        data.world = WorldManager.INSTANCE.getWorldNameByUUID(event.getPlayer().getInstance().getUniqueId());
        data.name = event.getPlayer().getUsername();
        data.isFlying = event.getPlayer().isFlying();
        data.isOP = event.getPlayer().hasPermission("server.op");
        TomlWriter writer = new TomlWriter();
        try {
            writer.write(data, playerFile.toFile());
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }
}

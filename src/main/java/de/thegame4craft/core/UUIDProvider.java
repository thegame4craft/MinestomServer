package de.thegame4craft.core;

import de.thegame4craft.util.FileSystemHelper;
import net.minestom.server.MinecraftServer;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class UUIDProvider {
    public static void initProvider() {
        MinecraftServer.getConnectionManager()
            .setUuidProvider((playerConnection, username) -> {
                try {
                    final Path playerPath = Path.of("players.json");
                    FileSystemHelper.createFileIfNotExists(playerPath.toFile(), "{}");
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
    }
}

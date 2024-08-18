package de.thegame4craft.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;

import java.util.Locale;
import java.util.Objects;

public class PlayerBlockPlaceEvent {
    @SuppressWarnings("UnstableApiUsage")
    public static void execute(net.minestom.server.event.player.PlayerBlockPlaceEvent event) {
        event.consumeBlock(event.getPlayer().getGameMode() != GameMode.CREATIVE);
        // rotate block
        //BlockFace face = event.getPlayer().getPosition().yaw() > 0 ? BlockFace.NORTH : BlockFace.SOUTH;


        // sides
        String face = "NORTH";
        Pos pos = event.getPlayer().getPosition();
        if(pos.pitch() >= 45) {
            face = "UP";
        } else if (pos.pitch() <= -45) {
            face = "DOWN";
        }
        else if(pos.yaw() >= -135 && pos.yaw() <= -45 ) { // player is facing east
            face = "WEST";
        } else if(pos.yaw() <= -135 || pos.yaw() >= 135) { // player is facing north
            face = "SOUTH";
        } else if(pos.yaw() >= -45 && pos.yaw() <= 45) { // player is facing south
            face = "NORTH";
        } else if(pos.yaw() >= 45 && pos.yaw() <= 135) { // player is facing west
            face = "EAST";
        }
        String hasFacing = event.getBlock().getProperty("facing");
        if(hasFacing != null) {
            event.setBlock(event.getBlock().withProperty("facing", face.toLowerCase()));
        }
    }
}

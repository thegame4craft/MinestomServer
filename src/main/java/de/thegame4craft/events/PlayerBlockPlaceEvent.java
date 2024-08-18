package de.thegame4craft.events;

import net.minestom.server.coordinate.Pos;

public class PlayerBlockPlaceEvent {
    public static void execute(net.minestom.server.event.player.PlayerBlockPlaceEvent event) {


        String face = "NORTH";
        Pos pos = event.getPlayer().getPosition();

        // generate the face the block should be locking at
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

        // check if the block has a facing property. Some blocks (like the Stone block) don't have a facing property so setting one will result in an exception
        String hasFacing = event.getBlock().getProperty("facing");
        if(hasFacing != null) {
            event.setBlock(event.getBlock().withProperty("facing", face.toLowerCase()));
        }
    }
}

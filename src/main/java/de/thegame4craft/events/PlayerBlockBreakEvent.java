package de.thegame4craft.events;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.item.ItemStack;

import java.time.Duration;

public class PlayerBlockBreakEvent {

    public static void execute(net.minestom.server.event.player.PlayerBlockBreakEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) { // only drop items if not in creative mode
            var material = event.getBlock().registry().material();
            if(material != null) {
                ItemStack itemStack = ItemStack.of(material);
                ItemEntity itemEntity = new ItemEntity(itemStack);
                itemEntity.setInstance(event.getPlayer().getInstance(), event.getBlockPosition().add(0.5, 0.5, 0.5));
                itemEntity.setPickupDelay(Duration.ofMillis(500));
            }
        }
    }
}

package de.thegame4craft.events;

import net.minestom.server.entity.Player;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.item.ItemStack;

public class ItemPickupEvent {
    public static void execute(PickupItemEvent event) {
        ItemStack itemStack = event.getItemEntity().getItemStack();
        if(event.getLivingEntity() instanceof Player player) {
            player.getInventory().addItemStack(itemStack);
        }
    }
}

package de.thegame4craft.core;

import de.thegame4craft.commands.GameModeCommand;
import de.thegame4craft.commands.StopCommand;
import de.thegame4craft.commands.TimeCommand;
import de.thegame4craft.events.ItemPickupEvent;
import de.thegame4craft.events.PlayerJoinEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;

public class Registry {
    public static void eventRegistry() {
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, PlayerJoinEvent::execute);
        globalEventHandler.addListener(PlayerSpawnEvent.class, PlayerJoinEvent::phase2);
        globalEventHandler.addListener(PlayerMoveEvent.class, de.thegame4craft.events.PlayerMoveEvent::execute);
        globalEventHandler.addListener(PlayerBlockPlaceEvent.class, de.thegame4craft.events.PlayerBlockPlaceEvent::execute);
        globalEventHandler.addListener(PlayerBlockBreakEvent.class, de.thegame4craft.events.PlayerBlockBreakEvent::execute);
        globalEventHandler.addListener(PickupItemEvent.class, ItemPickupEvent::execute);
        globalEventHandler.addListener(PlayerDisconnectEvent.class, playerDisconnectEvent -> {
            WorldManager.INSTANCE.saveWorlds();
        });
    }

    public static void registerCommands() {
        MinecraftServer.getCommandManager().register(new TimeCommand());
        MinecraftServer.getCommandManager().register(new GameModeCommand());
        MinecraftServer.getCommandManager().register(new StopCommand());
    }
}

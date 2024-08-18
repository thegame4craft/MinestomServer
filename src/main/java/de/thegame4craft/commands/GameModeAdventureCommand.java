package de.thegame4craft.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GameModeAdventureCommand extends Command {
    public GameModeAdventureCommand() {
        super("adventure");

        setDefaultExecutor((sender, context) -> {
            if(sender instanceof Player p) {
                p.setGameMode(GameMode.ADVENTURE);
            }
        });
    }
}

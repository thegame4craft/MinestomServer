package de.thegame4craft.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GameModeCreativeCommand extends Command {
    public GameModeCreativeCommand() {
        super("creative");

        setDefaultExecutor((sender, context) -> {
            if(sender instanceof Player p) {
                p.setGameMode(GameMode.CREATIVE);
            }
        });
    }
}

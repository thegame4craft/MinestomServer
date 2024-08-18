package de.thegame4craft.commands;

import de.thegame4craft.core.PermissionManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GameModeSurvivalCommand extends Command {
    public GameModeSurvivalCommand() {
        super("survival");

        setDefaultExecutor((sender, context) -> {
            if(sender instanceof Player p) {
                p.setGameMode(GameMode.SURVIVAL);
            }
        });
    }
}

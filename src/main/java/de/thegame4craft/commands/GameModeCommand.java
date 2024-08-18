package de.thegame4craft.commands;

import net.minestom.server.command.builder.Command;

public class GameModeCommand extends Command {
    public GameModeCommand() {
        super("gamemode");

        addSubcommand(new GameModeSurvivalCommand());
        addSubcommand(new GameModeCreativeCommand());
        addSubcommand(new GameModeAdventureCommand());
        addSubcommand(new GameModeSpectatorCommand());
    }
}

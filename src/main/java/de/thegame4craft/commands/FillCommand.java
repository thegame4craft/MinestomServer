package de.thegame4craft.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;

public class FillCommand extends Command {
    public FillCommand() {
        super("fill");

        setDefaultExecutor((sender, context) -> {
            destroyArea(sender, 5);
        });
        var range = ArgumentType.Integer("range");
        range.setCallback((sender, context) -> {

        });

        addSyntax((sender, context) -> {
            final int number = context.get(range);
            destroyArea(sender, number);
        }, range);
    }

    private void destroyArea(CommandSender sender, int area) {
        if(sender instanceof Player player) {
            Pos position = player.getPosition();
            for(int x = -area; x < area; x++) {
                for(int y = -area; y < area; y++) {
                    for(int z = -area; z < area; z++) {
                        if(y + position.blockY() < -64) continue;
                        if(player.getInstance().getBlock(x + position.blockX(), y + position.blockY(), z + position.blockZ()) != Block.BEDROCK) {
                            player.getInstance().setBlock(x + position.blockX(), y + position.blockY(), z + position.blockZ(), Block.AIR);
                        }
                    }
                }
            }
        }
    }
}

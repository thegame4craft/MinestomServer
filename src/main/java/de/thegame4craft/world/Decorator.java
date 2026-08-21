package de.thegame4craft.world;

import net.minestom.server.instance.block.Block;

import static de.thegame4craft.world.NoiseUtil.random;
import static de.thegame4craft.world.NoiseUtil.randomInt;

/**
 * Vegetation. Everything here is driven by a hash of the block position rather than a
 * {@link java.util.Random}, so a tree lands in the same place no matter which chunk happens
 * to generate first.
 * <p>
 * Trees are written through a {@link Block.Setter} taken from
 * {@link net.minestom.server.instance.generator.GenerationUnit#fork}, because canopies spill
 * over chunk borders.
 */
public final class Decorator {

    public enum Tree {OAK, BIRCH, SPRUCE, ACACIA, JUNGLE}

    private static final Block[] FLOWERS = {
            Block.DANDELION, Block.POPPY, Block.CORNFLOWER, Block.OXEYE_DAISY, Block.AZURE_BLUET
    };

    private Decorator() {
    }

    /** Picks the ground cover for a column, or null to leave it bare. */
    public static Block groundCover(WorldBiome biome, long seed, int x, int z) {
        if (biome == WorldBiome.DESERT) {
            return random(seed, x, z, 31) < biome.grassChance() ? Block.DEAD_BUSH : null;
        }
        double roll = random(seed, x, z, 31);
        if (roll < biome.flowerChance()) {
            return FLOWERS[randomInt(seed, x, z, 32, FLOWERS.length)];
        }
        if (roll < biome.flowerChance() + biome.grassChance()) {
            return Block.SHORT_GRASS;
        }
        return null;
    }

    /** {@code y} is the first air block above the ground, where the trunk starts. */
    public static void placeTree(Block.Setter setter, long seed, int x, int y, int z, Tree type) {
        switch (type) {
            case OAK -> broadleaf(setter, seed, x, y, z, Block.OAK_LOG, Block.OAK_LEAVES, 4, 3, 2);
            case BIRCH -> broadleaf(setter, seed, x, y, z, Block.BIRCH_LOG, Block.BIRCH_LEAVES, 5, 3, 2);
            case JUNGLE -> broadleaf(setter, seed, x, y, z, Block.JUNGLE_LOG, Block.JUNGLE_LEAVES, 8, 5, 3);
            case SPRUCE -> spruce(setter, seed, x, y, z);
            case ACACIA -> acacia(setter, seed, x, y, z);
        }
    }

    public static void placeCactus(Block.Setter setter, long seed, int x, int y, int z) {
        int height = 1 + randomInt(seed, x, z, 40, 3);
        for (int dy = 0; dy < height; dy++) {
            setter.setBlock(x, y + dy, z, Block.CACTUS);
        }
    }

    /** Oak, birch and jungle: a straight trunk under a rounded canopy. */
    private static void broadleaf(Block.Setter setter, long seed, int x, int y, int z,
                                  Block log, Block leaves, int minHeight, int spread, int radius) {
        int height = minHeight + randomInt(seed, x, z, 41, spread);
        int top = y + height;

        for (int dy = -2; dy <= 1; dy++) {
            int r = dy <= -1 ? radius : radius - 1;
            disc(setter, x, top + dy, z, r, leaves);
        }
        // Trunk last, so it cuts back through the leaves it just placed.
        for (int dy = 0; dy < height; dy++) {
            setter.setBlock(x, y + dy, z, log);
        }
    }

    /** Conifer: a cone that steps in and out so it keeps the layered spruce silhouette. */
    private static void spruce(Block.Setter setter, long seed, int x, int y, int z) {
        int height = 7 + randomInt(seed, x, z, 42, 5);
        int bottom = y + 2 + randomInt(seed, x, z, 43, 2);
        int top = y + height;

        for (int yy = bottom; yy <= top; yy++) {
            int fromTop = top - yy;
            int r = (int) Math.round(2.6 * fromTop / Math.max(1, top - bottom));
            if (fromTop % 2 == 1) r = Math.max(0, r - 1);
            disc(setter, x, yy, z, r, Block.SPRUCE_LEAVES);
        }
        setter.setBlock(x, top + 1, z, Block.SPRUCE_LEAVES);
        for (int dy = 0; dy < height; dy++) {
            setter.setBlock(x, y + dy, z, Block.SPRUCE_LOG);
        }
    }

    /** Acacia: short trunk, wide flat crown. */
    private static void acacia(Block.Setter setter, long seed, int x, int y, int z) {
        int height = 5 + randomInt(seed, x, z, 44, 3);
        int top = y + height;

        disc(setter, x, top, z, 3, Block.ACACIA_LEAVES);
        disc(setter, x, top + 1, z, 2, Block.ACACIA_LEAVES);
        for (int dy = 0; dy < height; dy++) {
            setter.setBlock(x, y + dy, z, Block.ACACIA_LOG);
        }
    }

    /** Filled circle of blocks, corners trimmed so canopies are round rather than square. */
    private static void disc(Block.Setter setter, int x, int y, int z, int radius, Block block) {
        int limit = radius * radius + 1;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > limit) continue;
                setter.setBlock(x + dx, y, z + dz, block);
            }
        }
    }
}

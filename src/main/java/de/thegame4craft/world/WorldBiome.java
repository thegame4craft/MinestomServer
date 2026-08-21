package de.thegame4craft.world;

import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.biome.Biome;

import java.util.HashMap;
import java.util.Map;

import static de.thegame4craft.world.TerrainShaper.SEA_LEVEL;

/**
 * The biome table. Each entry carries both the vanilla biome (which is what actually tints
 * grass, water, sky and fog on the client) and the blocks and vegetation the generator should
 * use there.
 * <p>
 * Chance values are per block column, so 0.02 is roughly five per chunk.
 */
public enum WorldBiome {

    // --- water ----------------------------------------------------------------------------
    DEEP_OCEAN(new Settings(Biome.DEEP_OCEAN, Block.GRAVEL, Block.GRAVEL)),
    OCEAN(new Settings(Biome.OCEAN, Block.GRAVEL, Block.SAND)),
    FROZEN_OCEAN(new Settings(Biome.FROZEN_OCEAN, Block.GRAVEL, Block.GRAVEL).frozen()),
    RIVER(new Settings(Biome.RIVER, Block.SAND, Block.SAND)),
    FROZEN_RIVER(new Settings(Biome.FROZEN_RIVER, Block.SAND, Block.SAND).frozen()),

    // --- shores ---------------------------------------------------------------------------
    BEACH(new Settings(Biome.BEACH, Block.SAND, Block.SAND)),
    SNOWY_BEACH(new Settings(Biome.SNOWY_BEACH, Block.SAND, Block.SAND).snowy()),
    STONY_SHORE(new Settings(Biome.STONY_SHORE, Block.STONE, Block.STONE)),

    // --- warm -----------------------------------------------------------------------------
    DESERT(new Settings(Biome.DESERT, Block.SAND, Block.SAND)
            .plants(0.004, 0).cactus(0.004)),
    SAVANNA(new Settings(Biome.SAVANNA, Block.GRASS_BLOCK, Block.DIRT)
            .trees(Decorator.Tree.ACACIA, 0.004).plants(0.20, 0.004)),
    JUNGLE(new Settings(Biome.JUNGLE, Block.GRASS_BLOCK, Block.DIRT)
            .trees(Decorator.Tree.JUNGLE, 0.035).plants(0.30, 0.01)),

    // --- temperate ------------------------------------------------------------------------
    PLAINS(new Settings(Biome.PLAINS, Block.GRASS_BLOCK, Block.DIRT)
            .trees(Decorator.Tree.OAK, 0.0015).plants(0.12, 0.02)),
    SUNFLOWER_PLAINS(new Settings(Biome.SUNFLOWER_PLAINS, Block.GRASS_BLOCK, Block.DIRT)
            .trees(Decorator.Tree.OAK, 0.001).plants(0.14, 0.06)),
    FOREST(new Settings(Biome.FOREST, Block.GRASS_BLOCK, Block.DIRT)
            .trees(Decorator.Tree.OAK, 0.030).plants(0.10, 0.01)),
    BIRCH_FOREST(new Settings(Biome.BIRCH_FOREST, Block.GRASS_BLOCK, Block.DIRT)
            .trees(Decorator.Tree.BIRCH, 0.030).plants(0.10, 0.01)),
    DARK_FOREST(new Settings(Biome.DARK_FOREST, Block.GRASS_BLOCK, Block.DIRT)
            .trees(Decorator.Tree.OAK, 0.055).plants(0.06, 0.004)),
    MEADOW(new Settings(Biome.MEADOW, Block.GRASS_BLOCK, Block.DIRT)
            .trees(Decorator.Tree.OAK, 0.0004).plants(0.22, 0.08)),

    // --- cold -----------------------------------------------------------------------------
    TAIGA(new Settings(Biome.TAIGA, Block.GRASS_BLOCK, Block.DIRT)
            .trees(Decorator.Tree.SPRUCE, 0.028).plants(0.08, 0.004)),
    SNOWY_TAIGA(new Settings(Biome.SNOWY_TAIGA, Block.GRASS_BLOCK, Block.DIRT)
            .trees(Decorator.Tree.SPRUCE, 0.020).plants(0.03, 0).snowy()),
    SNOWY_PLAINS(new Settings(Biome.SNOWY_PLAINS, Block.GRASS_BLOCK, Block.DIRT)
            .plants(0.02, 0).snowy()),
    GROVE(new Settings(Biome.GROVE, Block.GRASS_BLOCK, Block.DIRT)
            .trees(Decorator.Tree.SPRUCE, 0.014).snowy()),

    // --- high ground ----------------------------------------------------------------------
    WINDSWEPT_HILLS(new Settings(Biome.WINDSWEPT_HILLS, Block.GRASS_BLOCK, Block.DIRT)
            .trees(Decorator.Tree.SPRUCE, 0.004).plants(0.05, 0.004)),
    SNOWY_SLOPES(new Settings(Biome.SNOWY_SLOPES, Block.SNOW_BLOCK, Block.STONE)),
    STONY_PEAKS(new Settings(Biome.STONY_PEAKS, Block.STONE, Block.STONE)),
    JAGGED_PEAKS(new Settings(Biome.JAGGED_PEAKS, Block.SNOW_BLOCK, Block.STONE));

    /** Mutable while the table above is being built, then copied into the enum constant. */
    private static final class Settings {
        private final RegistryKey<Biome> key;
        private final Block surface;
        private final Block filler;
        private Decorator.Tree tree;
        private double treeChance;
        private double cactusChance;
        private double grassChance;
        private double flowerChance;
        private boolean snowy;
        private boolean frozen;

        Settings(RegistryKey<Biome> key, Block surface, Block filler,) {
            this.key = key;
            this.surface = surface;
            this.filler = filler;
        }

        Settings trees(Decorator.Tree tree, double chance) {
            this.tree = tree;
            this.treeChance = chance;
            return this;
        }

        Settings cactus(double chance) {
            this.cactusChance = chance;
            return this;
        }

        Settings plants(double grass, double flowers) {
            this.grassChance = grass;
            this.flowerChance = flowers;
            return this;
        }

        /** Lays a snow layer over the surface. */
        Settings snowy() {
            this.snowy = true;
            return this;
        }

        /** Freezes the top water block into ice. */
        Settings frozen() {
            this.frozen = true;
            this.snowy = true;
            return this;
        }
    }

    // --- climate thresholds ---------------------------------------------------------------

    private static final double COLD = -0.35;
    /**
     * Large bodies of water moderate the climate, so ice and snowy shores need it properly
     * cold rather than merely cold.
     */
    private static final double FREEZING = -0.62;
    private static final double WARM = 0.35;
    private static final double DRY = -0.25;
    private static final double WET = 0.25;

    /** Above these the ground is bare rock and snow whatever the climate says. */
    private static final int ALPINE = 108;
    private static final int SUBALPINE = 88;

    // --------------------------------------------------------------------------------------

    private final RegistryKey<Biome> key;
    private final Block surface;
    private final Block filler;
    private final Decorator.Tree tree;
    private final double treeChance;
    private final double cactusChance;
    private final double grassChance;
    private final double flowerChance;
    private final boolean snowy;
    private final boolean frozen;

    WorldBiome(Settings settings) {
        this.key = settings.key;
        this.surface = settings.surface;
        this.filler = settings.filler;
        this.tree = settings.tree;
        this.treeChance = settings.treeChance;
        this.cactusChance = settings.cactusChance;
        this.grassChance = settings.grassChance;
        this.flowerChance = settings.flowerChance;
        this.snowy = settings.snowy;
        this.frozen = settings.frozen;
    }

    public RegistryKey<Biome> key() {
        return key;
    }

    public Block surface() {
        return surface;
    }

    public Block filler() {
        return filler;
    }

    public Decorator.Tree tree() {
        return tree;
    }

    public double treeChance() {
        return treeChance;
    }

    public double cactusChance() {
        return cactusChance;
    }

    public double grassChance() {
        return grassChance;
    }

    public double flowerChance() {
        return flowerChance;
    }

    public boolean snowy() {
        return snowy;
    }

    public boolean frozen() {
        return frozen;
    }

    /**
     * Picks the biome for a column. Water, shore and altitude win over climate, because a
     * beach is a beach whatever the weather; only the leftover mid-altitude land is decided
     * by the temperature and humidity fields.
     */
    public static WorldBiome select(TerrainShaper.Sample sample) {
        int top = sample.height() - 1;
        boolean cold = sample.temperature() < COLD;
        boolean freezing = sample.temperature() < FREEZING;

        if (sample.river() > 0.35 && top < SEA_LEVEL) {
            return freezing ? FROZEN_RIVER : RIVER;
        }
        if (top < SEA_LEVEL) {
            if (freezing) return FROZEN_OCEAN;
            return top < SEA_LEVEL - 18 ? DEEP_OCEAN : OCEAN;
        }
        if (top <= SEA_LEVEL + 2) {
            if (freezing) return SNOWY_BEACH;
            // A rugged coast gets rocks instead of sand.
            return sample.erosion() > 0.72 ? STONY_SHORE : BEACH;
        }

        if (top > ALPINE) return cold ? JAGGED_PEAKS : STONY_PEAKS;
        if (top > SUBALPINE) return cold ? SNOWY_SLOPES : WINDSWEPT_HILLS;
        if (sample.mountain() > 0.35) return cold ? GROVE : MEADOW;

        double temperature = sample.temperature();
        double humidity = sample.humidity();

        if (temperature < COLD) {
            if (humidity < DRY) return SNOWY_PLAINS;
            return humidity > WET ? SNOWY_TAIGA : TAIGA;
        }
        if (temperature > WARM) {
            if (humidity < DRY) return DESERT;
            return humidity > WET ? JUNGLE : SAVANNA;
        }
        if (humidity < DRY) return temperature > 0.1 ? SUNFLOWER_PLAINS : PLAINS;
        if (humidity > WET) return temperature < 0 ? BIRCH_FOREST : DARK_FOREST;
        return FOREST;
    }
}

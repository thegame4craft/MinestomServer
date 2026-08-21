package de.thegame4craft.world;

import static de.thegame4craft.world.NoiseUtil.clamp01;
import static de.thegame4craft.world.NoiseUtil.fade;
import static de.thegame4craft.world.NoiseUtil.lerp;
import static de.thegame4craft.world.NoiseUtil.spline;

/**
 * Turns a block column into a terrain height plus the climate values the biome picker needs.
 * <p>
 * The height is built from four layers, in order of decreasing size:
 * <ol>
 *   <li><b>Continentalness</b> run through a shaping curve, deciding land vs sea and the
 *       overall elevation. The curve, not an amplitude, is what sets the slope.</li>
 *   <li><b>Erosion</b>, a region-sized value that blends between a gentle and a rugged
 *       version of that curve, so flat plains and dramatic highlands can exist side by
 *       side instead of every part of the world having the same steepness.</li>
 *   <li><b>Ridged mountain relief</b>, added only in rugged, high-continentalness regions.</li>
 *   <li><b>Hills and roughness</b> for local detail.</li>
 * </ol>
 * Rivers are carved last, after the terrain exists.
 */
public final class TerrainShaper {

    public static final int SEA_LEVEL = 63;

    // --- continent shaping ----------------------------------------------------------------

    /**
     * Continentalness mapped to a base height, as two curves the erosion value blends
     * between. The gap between neighbouring output values is the slope of that band of
     * terrain, so these tables are the main steepness control.
     * <p>
     * Both curves agree around the water line (-0.32 .. -0.05), which keeps the coastline in
     * the same place and equally gentle however eroded the region is; they only diverge
     * inland and offshore, which is where the variety is wanted.
     */
    private static final double[] SHAPE_IN = {-1.00, -0.75, -0.50, -0.32, -0.18, -0.05, 0.15, 0.40, 0.70, 1.00};
    /** Eroded regions: shallow seas, wide plains, nothing dramatic. */
    private static final double[] SHAPE_FLAT = {30, 46, 55, 60, 63, 66, 68, 71, 76, 84};
    /** Uneroded regions: deep seas and high ground. */
    private static final double[] SHAPE_STEEP = {12, 28, 44, 59, 63, 67, 74, 88, 102, 112};

    /** Continent size. Smaller value = wider landmasses and gentler slopes. */
    private static final double CONTINENT_SCALE = 0.0018;
    private static final int CONTINENT_OCTAVES = 4;

    /** Region-sized noise choosing between the two curves above. Low value = flat. */
    private static final double EROSION_SCALE = 0.0009;

    // --- mountains ------------------------------------------------------------------------

    /** Ridge frequency. Lower than the hills, so ranges are larger than the bumps on them. */
    private static final double RIDGE_SCALE = 0.0055;
    private static final int RIDGE_OCTAVES = 6;
    /** Height added on a ridge crest at full mountain strength. */
    private static final double MOUNTAIN_RELIEF = 42.0;
    /** Continentalness band over which mountains fade in. */
    private static final double MOUNTAIN_START = 0.22;
    private static final double MOUNTAIN_FULL = 0.70;

    // --- hills and roughness --------------------------------------------------------------

    private static final double HILL_SCALE = 0.008;
    private static final double HILL_MIN_AMPLITUDE = 2.0;   // in flat, eroded regions
    private static final double HILL_MAX_AMPLITUDE = 12.0;  // in rugged regions

    private static final double ROUGHNESS_SCALE = 0.03;
    private static final double ROUGHNESS_AMPLITUDE = 1.2;

    /** Hills fade out this many blocks either side of the water line, keeping coasts walkable. */
    private static final double COAST_FADE = 8.0;

    // --- rivers ---------------------------------------------------------------------------

    /**
     * Rivers follow the zero crossing of a low frequency noise field, which naturally gives
     * long meandering channels that branch and rejoin.
     */
    private static final double RIVER_SCALE = 0.0011;
    private static final int RIVER_OCTAVES = 2;
    /** Half width of the channel in noise units. Larger = wider rivers. */
    private static final double RIVER_WIDTH = 0.055;
    /** River beds are cut to here, just deep enough for the water fill to reach them. */
    private static final double RIVER_BED = SEA_LEVEL - 2;
    /**
     * How deep a channel may cut below the surrounding land. Rivers run inland over ground
     * that is well above sea level, and without this cap a channel crossing high country
     * would gouge a canyon the full distance down to the water line.
     */
    private static final double RIVER_MAX_DEPTH = 16.0;
    /** Rivers run from the coast up to here, then fade out and leave the ridges alone. */
    private static final double RIVER_LOWLAND = SEA_LEVEL + 6;
    private static final double RIVER_LIMIT = SEA_LEVEL + 46;

    // --- climate --------------------------------------------------------------------------

    private static final double TEMPERATURE_SCALE = 0.0012;
    private static final double HUMIDITY_SCALE = 0.0015;
    /** Blocks of elevation that cost one full unit of temperature. Puts snow on high ground. */
    private static final double LAPSE_RATE = 55.0;

    // --------------------------------------------------------------------------------------

    private final NoiseUtil.Layer continent;
    private final NoiseUtil.Layer erosion;
    private final NoiseUtil.Layer ridge;
    private final NoiseUtil.Layer hills;
    private final NoiseUtil.Layer roughness;
    private final NoiseUtil.Layer river;
    private final NoiseUtil.Layer temperature;
    private final NoiseUtil.Layer humidity;

    public TerrainShaper(long seed) {
        this.continent = new NoiseUtil.Layer(seed, 0, CONTINENT_SCALE, CONTINENT_OCTAVES);
        this.erosion = new NoiseUtil.Layer(seed, 1, EROSION_SCALE, 2);
        this.ridge = new NoiseUtil.Layer(seed, 2, RIDGE_SCALE, RIDGE_OCTAVES);
        this.hills = new NoiseUtil.Layer(seed, 3, HILL_SCALE, 4);
        this.roughness = new NoiseUtil.Layer(seed, 4, ROUGHNESS_SCALE, 2);
        this.river = new NoiseUtil.Layer(seed, 5, RIVER_SCALE, RIVER_OCTAVES);
        this.temperature = new NoiseUtil.Layer(seed, 6, TEMPERATURE_SCALE, 2);
        this.humidity = new NoiseUtil.Layer(seed, 7, HUMIDITY_SCALE, 2);
    }

    /**
     * Everything the generator needs to know about one block column.
     *
     * @param height      first Y that is not solid, so the top solid block is at height - 1
     * @param continent   -1 deep ocean .. 1 continental interior
     * @param erosion     0 flat and eroded .. 1 rugged
     * @param river       0 no river .. 1 middle of the channel
     * @param temperature -1 freezing .. 1 hot, already adjusted for altitude
     * @param humidity    -1 dry .. 1 wet
     * @param mountain    0 lowland .. 1 full mountain relief
     */
    public record Sample(int height, double continent, double erosion, double river,
                         double temperature, double humidity, double mountain) {
    }

    public Sample sample(int x, int z) {
        double continentValue = continent.fbm(x, z);
        double erosionValue = erosion.fbm01(x, z);

        // Blend the gentle and the rugged shaping curve. This is what stops every part of the
        // world from having the same slope.
        double base = lerp(spline(SHAPE_IN, SHAPE_FLAT, continentValue),
                spline(SHAPE_IN, SHAPE_STEEP, continentValue),
                erosionValue);

        // 0 well below the water line, 1 well above it.
        double coast = fade(SEA_LEVEL - COAST_FADE, SEA_LEVEL + COAST_FADE, base);

        // Mountains need both continental interior and a rugged region, so some continents
        // are alpine and others stay rolling farmland.
        double mountain = fade(MOUNTAIN_START, MOUNTAIN_FULL, continentValue)
                * NoiseUtil.smoothstep(erosionValue);
        double relief = ridge.ridged(x, z) * MOUNTAIN_RELIEF * mountain;

        double riverValue = riverStrength(x, z, base) * coast;
        // Above the lowlands there is not enough height to reach the water table, so the
        // channel becomes a dry valley instead of gouging its way down to sea level.
        double riverBed = Math.max(RIVER_BED, base - RIVER_MAX_DEPTH);

        double hillAmplitude = lerp(HILL_MIN_AMPLITUDE, HILL_MAX_AMPLITUDE, erosionValue)
                * coast * (1 - riverValue);
        double detail = hills.fbm(x, z) * hillAmplitude
                + roughness.fbm(x, z) * ROUGHNESS_AMPLITUDE * (1 - 0.8 * riverValue);

        double height = base + relief + detail;
        // Carve the channel last so it cuts cleanly through whatever the layers above built.
        height = lerp(height, riverBed, riverValue);

        double temperatureValue = temperature.fbm(x, z) - (height - SEA_LEVEL) / LAPSE_RATE;

        return new Sample((int) Math.round(height), continentValue, erosionValue, riverValue,
                temperatureValue, humidity.fbm(x, z), mountain);
    }

    /** 1 in the middle of a river channel, falling to 0 at its banks and on high ground. */
    private double riverStrength(int x, int z, double base) {
        double distance = Math.abs(river.fbm(x, z));
        if (distance >= RIVER_WIDTH) return 0;
        double channel = NoiseUtil.smoothstep(1 - distance / RIVER_WIDTH);
        double lowland = 1 - fade(RIVER_LOWLAND, RIVER_LIMIT, base);
        return clamp01(channel * lowland);
    }
}

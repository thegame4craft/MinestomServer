package de.thegame4craft.world;

import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator;
import de.articdive.jnoise.pipeline.JNoise;

/**
 * Noise helpers shared by the world generator.
 * <p>
 * Every {@link Layer} keeps one JNoise pipeline per thread instead of guarding a single
 * shared pipeline with a lock, so chunks on different worker threads no longer queue up
 * behind each other.
 */
public final class NoiseUtil {

    /**
     * Stacked perlin octaves only ever reach about a fifth of their nominal -1..1 range, so
     * an octave stack is amplified by this and then softly squashed back with tanh. Raise it
     * for a more extreme world, lower it for a tamer one.
     */
    public static final double GAIN = 3.0;

    private NoiseUtil() {
    }

    /** One noise layer: a seeded, scaled perlin pipeline plus the octave count to sample it at. */
    public static final class Layer {
        private final ThreadLocal<JNoise> noise;
        private final int octaves;

        public Layer(long seed, int variant, double scale, int octaves) {
            // Splitmix-style mixing so neighbouring variants produce unrelated noise fields.
            long mixed = seed * 6364136223846793005L + variant * 1442695040888963407L;
            this.noise = ThreadLocal.withInitial(() -> perlin(mixed, scale));
            this.octaves = octaves;
        }

        /** Smooth, rounded shapes in -1..1. */
        public double fbm(double x, double z) {
            return NoiseUtil.fbm(noise.get(), x, z, octaves, GAIN);
        }

        /** Smooth shapes in 0..1. */
        public double fbm01(double x, double z) {
            return (fbm(x, z) + 1) * 0.5;
        }

        /** Sharp ridgelines in 0..1, 1 on the crest. */
        public double ridged(double x, double z) {
            return NoiseUtil.ridged(noise.get(), x, z, octaves);
        }
    }

    public static JNoise perlin(long seed, double scale) {
        PerlinNoiseGenerator.PerlinNoiseBuilder builder = PerlinNoiseGenerator.newBuilder();
        builder.setSeed(seed);
        return JNoise.newBuilder().perlin(builder.build()).scale(scale).build();
    }

    /**
     * Fractal brownian motion: octaves of the same noise at doubling frequency and halving
     * amplitude. Result normalised to roughly -1..1.
     */
    public static double fbm(JNoise noise, double x, double z, int octaves, double gain) {
        double sum = 0, amplitude = 1, frequency = 1, total = 0;
        for (int o = 0; o < octaves; o++) {
            // Offset each octave, otherwise they all cross zero on the same lattice points.
            double offset = o * 991.0;
            sum += noise.evaluateNoise(x * frequency + offset, z * frequency - offset) * amplitude;
            total += amplitude;
            amplitude *= 0.5;
            frequency *= 2;
        }
        // tanh rather than a hard clamp: squashes the rare extremes without leaving a crease.
        return Math.tanh(sum / total * gain);
    }

    /**
     * Ridged multifractal in 0..1, 1 on the crest.
     * <p>
     * Folding the noise at zero ({@code 1 - |n|}) turns perlin's rounded hilltops into sharp
     * ridgelines with V-shaped valleys between them - that fold is what makes mountains read
     * as connected ranges rather than scattered lumps. Each octave is weighted by the
     * previous one so fine detail only accumulates near the ridges and the valleys stay
     * smooth.
     */
    public static double ridged(JNoise noise, double x, double z, int octaves) {
        double sum = 0, amplitude = 1, frequency = 1, total = 0, weight = 1;
        for (int o = 0; o < octaves; o++) {
            double offset = o * 733.0;
            double n = Math.tanh(noise.evaluateNoise(x * frequency + offset, z * frequency - offset) * 2.4);
            double ridge = 1 - Math.abs(n);
            ridge *= ridge;              // sharpen the crest
            ridge *= weight;             // detail only where the previous octave was already high
            weight = clamp01(ridge * 1.6);
            sum += ridge * amplitude;
            total += amplitude;
            amplitude *= 0.5;
            frequency *= 2;
        }
        return clamp01(sum / total * RIDGE_NORMALISE);
    }

    /** Ridged output clusters well below 1; this stretches it back across the full range. */
    private static final double RIDGE_NORMALISE = 2.6;

    /**
     * Piecewise curve through the given control points, smoothstepped between them so the
     * terrain has no hard crease where one segment hands over to the next.
     */
    public static double spline(double[] in, double[] out, double t) {
        if (t <= in[0]) return out[0];
        for (int i = 1; i < in.length; i++) {
            if (t <= in[i]) {
                double f = smoothstep((t - in[i - 1]) / (in[i] - in[i - 1]));
                return out[i - 1] + (out[i] - out[i - 1]) * f;
            }
        }
        return out[out.length - 1];
    }

    public static double smoothstep(double t) {
        return t * t * (3 - 2 * t);
    }

    /** Smoothstep of {@code (value - edge0) / (edge1 - edge0)}, clamped to 0..1. */
    public static double fade(double edge0, double edge1, double value) {
        return smoothstep(clamp01((value - edge0) / (edge1 - edge0)));
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static double clamp01(double value) {
        return clamp(0, value, 1);
    }

    public static double clamp(double lower, double value, double upper) {
        if (value <= lower) return lower;
        return Math.min(value, upper);
    }

    /** Deterministic 0..1 value for a block column. Same input always gives the same result. */
    public static double random(long seed, int x, int z, int salt) {
        long h = seed
                + x * 0x9E3779B97F4A7C15L
                + z * 0xC2B2AE3D27D4EB4FL
                + salt * 0x165667B19E3779F9L;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return (h >>> 11) * 0x1.0p-53;
    }

    /** Deterministic integer in {@code [0, bound)} for a block column. */
    public static int randomInt(long seed, int x, int z, int salt, int bound) {
        return (int) (random(seed, x, z, salt) * bound);
    }
}

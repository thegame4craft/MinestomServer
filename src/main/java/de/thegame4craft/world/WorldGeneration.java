package de.thegame4craft.world;

import de.articdive.jnoise.pipeline.JNoise;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.UnitModifier;

import java.util.ArrayList;
import java.util.List;

import static de.thegame4craft.world.NoiseUtil.random;
import static de.thegame4craft.world.TerrainShaper.SEA_LEVEL;

/**
 * Wires the terrain, biome and vegetation layers into a Minestom generator.
 *
 * @see TerrainShaper for the height field
 * @see WorldBiome for the biome table
 * @see Decorator for trees and plants
 */
public class WorldGeneration {

    /** Fixed seed, so the same world comes back after a restart while tuning. */
    private static final long SEED = 20260821L;

    private static final boolean GENERATE_WATER = true;
    private static final boolean GENERATE_VEGETATION = true;

    /** Blocks of biome filler between the surface and the stone below it. */
    private static final int SOIL_DEPTH = 4;

    /** Biomes are stored per 4x4x4 cell, so only every fourth column needs one written. */
    private static final int BIOME_STEP = 4;

    /** Head room a tree needs, checked so a canopy can never run past the top of the world. */
    private static final int TREE_CLEARANCE = 16;

    private static final TerrainShaper SHAPER = new TerrainShaper(SEED);
    private static final Decorator.Tree[] TREES = Decorator.Tree.values();
    private static final JNoise bedrockNoise = NoiseUtil.perlin(SEED, 0.5);

    public static void generateWorld(Instance instance) {
        instance.setGenerator(unit -> {
            Point start = unit.absoluteStart();

            UnitModifier modifier = unit.modifier();

            int minY = start.blockY();
            if(minY == -64) minY = -55;
            int maxY = unit.absoluteEnd().blockY(); // exclusive
            int sizeX = (int) unit.size().x();
            int sizeZ = (int) unit.size().z();

            // Trunk positions, collected here and planted in one fork below.
            List<int[]> trees = new ArrayList<>();

            for (int lx = 0; lx < sizeX; lx++) {
                for (int lz = 0; lz < sizeZ; lz++) {
                    int x = start.blockX() + lx;
                    int z = start.blockZ() + lz;


                    Point column = start.add(lx, 0, lz);
                    Point columnEnd = column.add(1, 0, 1);

                    // y = -64 with static bedrock, then some perlin generated noise
                    modifier.fill(column.withY(-64), columnEnd.withY(-63), Block.BEDROCK);
                    synchronized (bedrockNoise) {
                        int bedrockHeight = (int)(bedrockNoise.evaluateNoise(x, z) * 2.5+2.5);
                        modifier.fill(column.withY(-63), columnEnd.withY(-63+bedrockHeight), Block.BEDROCK);
                        modifier.fill(column.withY(-63+bedrockHeight), columnEnd.withY(-55), Block.STONE);
                    }

                    TerrainShaper.Sample sample = SHAPER.sample(x, z);
                    WorldBiome biome = WorldBiome.select(sample);

                    int surface = sample.height();      // first Y that is not solid
                    int top = surface - 1;              // top solid block

                    int stoneTop = Math.min(surface - SOIL_DEPTH, maxY);
                    if (stoneTop > minY) {
                        modifier.fill(column.withY(minY), columnEnd.withY(stoneTop), Block.STONE);
                    }

                    int fillerBottom = Math.max(stoneTop, minY);
                    int fillerTop = Math.min(surface, maxY);
                    if (fillerTop >= fillerBottom) {
                        modifier.fill(column.withY(fillerBottom), columnEnd.withY(fillerTop), biome.filler());
                    }

                    if (top >= minY && top < maxY) {
                        modifier.setBlock(column.withY(top), biome.surface());
                    }

                    if (surface < SEA_LEVEL) {
                        if (GENERATE_WATER) {
                            fillWater(modifier, column, columnEnd, biome, surface, minY, maxY);
                        }
                    } else if (GENERATE_VEGETATION && surface < maxY) {
                        decorate(modifier, column, trees, biome, x, surface, z, maxY);
                    }

                    // Biome storage is 4x4x4, so writing every column would be wasted work.
                    if ((lx % BIOME_STEP) == 0 && (lz % BIOME_STEP) == 0) {
                        for (int y = minY; y < maxY; y += BIOME_STEP) {
                            modifier.setBiome(x, y, z, biome.key());
                        }
                    }
                }
            }

            if (!trees.isEmpty()) {
                // Canopies spill across chunk borders, which only a fork can write to.
                unit.fork(setter -> {
                    for (int[] tree : trees) {
                        Decorator.placeTree(setter, SEED, tree[0], tree[1], tree[2], TREES[tree[3]]);
                    }
                });
            }
        });
    }

    private static void fillWater(UnitModifier modifier, Point column, Point columnEnd,
                                  WorldBiome biome, int surface, int minY, int maxY) {
        int waterBottom = Math.max(surface, minY);
        int waterTop = Math.min(SEA_LEVEL, maxY);
        if (waterTop <= waterBottom) return;

        modifier.fill(column.withY(waterBottom), columnEnd.withY(waterTop), Block.WATER);
        if (biome.frozen() && waterTop == SEA_LEVEL) {
            modifier.setBlock(column.withY(waterTop - 1), Block.ICE);
        }
    }

    private static void decorate(UnitModifier modifier, Point column, List<int[]> trees,
                                 WorldBiome biome, int x, int surface, int z, int maxY) {
        // Snow goes down first; a trunk placed later cuts back through it.
        if (biome.snowy()) {
            modifier.setBlock(column.withY(surface), Block.SNOW);
        } else if (random(SEED, x, z, 21) < biome.cactusChance()) {
            Decorator.placeCactus(modifier, SEED, x, surface, z);
            return;
        } else {
            Block cover = Decorator.groundCover(biome, SEED, x, z);
            if (cover != null) {
                modifier.setBlock(column.withY(surface), cover);
            }
        }

        if (biome.tree() != null
                && surface + TREE_CLEARANCE < maxY
                && random(SEED, x, z, 20) < biome.treeChance()) {
            trees.add(new int[]{x, surface, z, biome.tree().ordinal()});
        }
    }
}

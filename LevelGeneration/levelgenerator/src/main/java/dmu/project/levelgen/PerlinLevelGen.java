package dmu.project.levelgen;

import java.util.Random;

import dmu.project.levelgen.exceptions.LevelGenerationException;
import dmu.project.noise.OpenSimplexNoise;
import dmu.project.utils.Node;
import dmu.project.utils.Vector2D;

/**
 * Class wrapping OpenSimplexNoise.
 * <p>
 * Created by Dom on 18/11/2016.
 */

public class PerlinLevelGen implements LevelGenerator {

    /**
     * OpenSimplexNoise library.
     */
    private final OpenSimplexNoise simplexNoise;

    private int noiseX, noiseY, noiseWidth, noiseHeight, octaves;
    private double persistence;

    /**
     * Constructor
     *
     * @param noiseWidth  The width of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param noiseHeight The height of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param octaves     The number of noise octaves to layer.
     * @param persistence The amount the amplitude increases for each octave.
     */
    public PerlinLevelGen(int noiseWidth, int noiseHeight, int octaves, double persistence) {
        this(new Random().nextLong(), 0, 0, noiseWidth, noiseHeight, octaves, persistence);
    }

    /**
     * Constructor
     *
     * @param seed        The random number generator seed to use.
     * @param noiseWidth  The width of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param noiseHeight The height of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param octaves     The number of noise octaves to layer.
     * @param persistence The amount the amplitude increases for each octave.
     */
    public PerlinLevelGen(long seed, int noiseWidth, int noiseHeight, int octaves, double persistence) {
        this(seed, 0, 0, noiseWidth, noiseHeight, octaves, persistence);
    }

    /**
     * Constructor
     *
     * @param noiseOriginX The x coordinate of the bottom-left corner of the area to sample from.
     * @param noiseOriginY The y coordinate of the bottom-left corner of the area to sample from.
     * @param noiseWidth   the width of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param noiseHeight  the height of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param octaves      The number of noise octaves to layer.
     * @param persistence  The amount the amplitude increases for each octave.
     */
    public PerlinLevelGen(int noiseOriginX, int noiseOriginY, int noiseWidth, int noiseHeight, int octaves, double persistence) {
        this(new Random().nextLong(), noiseOriginX, noiseOriginY, noiseWidth, noiseHeight, octaves, persistence);
    }

    /**
     * Constructor
     *
     * @param seed         The random number generator seed to use.
     * @param noiseOriginX The x coordinate of the bottom-left corner of the area to sample from.
     * @param noiseOriginY The y coordinate of the bottom-left corner of the area to sample from.
     * @param noiseWidth   the width of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param noiseHeight  the height of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param octaves      The number of noise octaves to layer.
     * @param persistence  The amount the amplitude increases for each octave.
     */
    public PerlinLevelGen(long seed, int noiseOriginX, int noiseOriginY, int noiseWidth, int noiseHeight, int octaves, double persistence) {
        this.simplexNoise = new OpenSimplexNoise(seed);
        this.noiseHeight = noiseHeight;
        this.noiseWidth = noiseWidth;
        this.octaves = octaves;
        this.persistence = persistence;
        this.noiseX = noiseOriginX;
        this.noiseY = noiseOriginY;
    }

    /**
     * Generate a level's heightmap
     *
     * @param width      the width of the level.
     * @param height     the height of the level.
     * @param waterLevel a value between 0..1 that represents the water level for the generated map.
     * @return A 2D width by height array of height values representing the terrain.
     * @throws LevelGenerationException
     */
    @Override
    public HeightMap generateLevel(int width, int height, double waterLevel) throws LevelGenerationException {
        if (width <= 0 || height <= 0 || waterLevel < 0)
            throw new LevelGenerationException("Invalid parameters for heightmap generation");
        if (noiseHeight == 0 || noiseWidth == 0)
            throw new LevelGenerationException("Invalid noise width and height for heightmap generation");
        double[][] noise = new double[width][height];
        HeightMap heightMap = new HeightMap(width, height, true, waterLevel);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                double nx = noiseX + (((x / (double) width)) * noiseWidth), ny = noiseY + (((y / (double) height)) * noiseHeight);
                double e = 0;
                double amplitude = 1.0;
                double frequency = 1.0;
                double maxVal = 0;
                double lacunarity = 2.0;
                for (int currOctave = 0; currOctave < octaves; currOctave++) {
                    double noiseVal = (amplitude * noise(frequency * nx, frequency * ny));
                    e += noiseVal;
                    maxVal += amplitude;
                    amplitude *= persistence;
                    frequency *= lacunarity;
                }
                e /= maxVal;
                e = Math.pow(e, 1.5);
                noise[x][y] = e;
                if (e > waterLevel) {
                    heightMap.aboveWaterValues++;
                    heightMap.grid.addNode(x, y, createNode(x, y, true));
                } else {
                    heightMap.grid.addNode(x, y, createNode(x, y, false));
                }
            }
        }
        heightMap.elevation = noise;
        return heightMap;
    }

    /////////////////////////////
    //Private Utility Methods //
    ////////////////////////////

    /**
     * Method to call to the noise library.
     * OpenSimplexNoise returns values between -1..1 and are scaled to 0..1
     *
     * @param nx Noise X Coordinate to evaluate with.
     * @param ny Noise Y Coordinate to evaluate with.
     * @return Noise value scaled between 0..1
     */
    private double noise(double nx, double ny) {
        return (simplexNoise.eval(nx, ny) / 2.0) + 0.5;
    }

    /**
     * Creates a new Node.
     *
     * @param x          The X coordinate of the node.
     * @param y          The Y coordinate of the node.
     * @param aboveWater True if the node is above the water level.
     * @return The created node.
     */
    private Node createNode(int x, int y, boolean aboveWater) {
        return new Node(new Vector2D(x, y), -1, aboveWater);
    }
}

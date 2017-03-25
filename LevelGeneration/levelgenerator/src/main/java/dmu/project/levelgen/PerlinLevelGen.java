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
    private final OpenSimplexNoise mSimplexNoise;

    private int mNoiseX, mNoiseY, mNoiseWidth, mNoiseHeight, mOctaves;
    private double mPersistence;

    /**
     * Constructor
     *
     * @param mNoiseWidth  The width of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param mNoiseHeight The height of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param mOctaves     The number of noise mOctaves to layer.
     * @param mPersistence  The amount the amplitude increases for each octave.
     */
    public PerlinLevelGen(int mNoiseWidth, int mNoiseHeight, int mOctaves, double mPersistence) {
        this(new Random().nextLong(), 0, 0, mNoiseWidth, mNoiseHeight, mOctaves, mPersistence);
    }

    /**
     * Constructor
     *
     * @param seed         The random number generator seed to use.
     * @param mNoiseWidth  The width of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param mNoiseHeight The height of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param mOctaves     The number of noise mOctaves to layer.
     * @param mPersistence  The amount the amplitude increases for each octave.
     */
    public PerlinLevelGen(long seed, int mNoiseWidth, int mNoiseHeight, int mOctaves, double mPersistence) {
        this(seed, 0, 0, mNoiseWidth, mNoiseHeight, mOctaves, mPersistence);
    }

    /**
     * Constructor
     *
     * @param noiseOriginX The x coordinate of the bottom-left corner of the area to sample from.
     * @param noiseOriginY The y coordinate of the bottom-left corner of the area to sample from.
     * @param mNoiseWidth  the width of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param mNoiseHeight the height of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param mOctaves     The number of noise mOctaves to layer.
     * @param mPersistence  The amount the amplitude increases for each octave.
     */
    public PerlinLevelGen(int noiseOriginX, int noiseOriginY, int mNoiseWidth, int mNoiseHeight, int mOctaves, double mPersistence) {
        this(new Random().nextLong(), noiseOriginX, noiseOriginY, mNoiseWidth, mNoiseHeight, mOctaves, mPersistence);
    }

    /**
     * Constructor
     *
     * @param seed         The random number generator seed to use.
     * @param noiseOriginX The x coordinate of the bottom-left corner of the area to sample from.
     * @param noiseOriginY The y coordinate of the bottom-left corner of the area to sample from.
     * @param mNoiseWidth  the width of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param mNoiseHeight the height of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param mOctaves     The number of noise mOctaves to layer.
     * @param mPersistence  The amount the amplitude increases for each octave.
     */
    public PerlinLevelGen(long seed, int noiseOriginX, int noiseOriginY, int mNoiseWidth, int mNoiseHeight, int mOctaves, double mPersistence) {
        this.mSimplexNoise = new OpenSimplexNoise(seed);
        this.mNoiseHeight = mNoiseHeight;
        this.mNoiseWidth = mNoiseWidth;
        this.mOctaves = mOctaves;
        this.mPersistence = mPersistence;
        this.mNoiseX = noiseOriginX;
        this.mNoiseY = noiseOriginY;
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
        if (mNoiseHeight == 0 || mNoiseWidth == 0)
            throw new LevelGenerationException("Invalid noise width and height for heightmap generation");
        double[][] noise = new double[width][height];
        HeightMap heightMap = new HeightMap(width, height, true, waterLevel);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                double nx = mNoiseX + (((x / (double) width)) * mNoiseWidth), ny = mNoiseY + (((y / (double) height)) * mNoiseHeight);
                double e = 0;
                double amplitude = 1.0;
                double frequency = 1.0;
                double maxVal = 0;
                double lacunarity = 2.0;
                for (int currOctave = 0; currOctave < mOctaves; currOctave++) {
                    double noiseVal = (amplitude * noise(frequency * nx, frequency * ny));
                    e += noiseVal;
                    maxVal += amplitude;
                    amplitude *= mPersistence;
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
        return (mSimplexNoise.eval(nx, ny) / 2.0) + 0.5;
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

package dmu.project.levelgen;

import java.util.Random;

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
     * Default constructor
     *
     * @param noiseWidth  the width of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param noiseHeight the height of the area to sample noise from. The larger the area the more varied/zoomed out the terrain.
     * @param octaves     The number of noise octaves to layer.
     * @param persistence The amount the amplitude increases for each octave.
     */
    public PerlinLevelGen(int noiseWidth, int noiseHeight, int octaves, double persistence) {
        this(new Random().nextLong(), 0, 0, noiseWidth, noiseHeight, octaves, persistence);
    }

    public PerlinLevelGen(long seed, int noiseWidth, int noiseHeight, int octaves, double persistence) {
        this(seed, 0, 0, noiseWidth, noiseHeight, octaves, persistence);
    }

    public PerlinLevelGen(int noiseOriginX, int noiseOriginY, int noiseWidth, int noiseHeight, int octaves, double persistence) {
        this(new Random().nextLong(), noiseOriginX, noiseOriginY, noiseWidth, noiseHeight, octaves, persistence);
    }

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
     */
    @Override
    public HeightMap generateLevel(int width, int height, float waterLevel) {
        double[][] noise = new double[width][height];
        //double[][] elevation = new double[width][height];
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
                    heightMap.grid.addNode(x,y,createNode(x,y,true));
                } else {
                    heightMap.grid.addNode(x,y,createNode(x,y,false));
                }
            }
        }
//        double maxElevation = 0, minElevation = 0;
//        for (int y = 0; y < height; ++y) {
//            for (int x = 0; x < width; ++x) {
//                double noiseVal = noise[(x*noiseWidth/width)][(y*noiseHeight/height)];
//                if(noiseVal > maxElevation) maxElevation = noiseVal;
//                if(noiseVal < minElevation) minElevation = noiseVal;
//                elevation[x][y] = noiseVal;
//            }
//        }

        heightMap.elevation = noise;
        return heightMap;
    }

    /**
     * Method to call to the noise library.
     * OpenSimplexNoise returns values between -1..1 and are scaled to 0..1
     *
     * @param nx Noise X Coordinate to evaluate with.
     * @param ny Noise Y Coordinate to evaluate with.
     * @return Noise value scaled between 0..1
     */
    private double noise(double nx, double ny) {
        //return (simplexNoise.eval(nx,ny, 0.5) + 1.0)/2.0;
        return (simplexNoise.eval(nx, ny) / 2.0) + 0.5;
    }

    private Node createNode(int x, int y, boolean aboveWater){
        return new Node(new Vector2D(x,y),-1, aboveWater);
    }
}

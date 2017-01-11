package dmu.project.levelgen;

import java.util.Random;

import dmu.project.noise.OpenSimplexNoise;

/**
 * Class wrapping OpenSimplexNoise.
 *
 * Created by Dom on 18/11/2016.
 */

public class PerlinLevelGen implements LevelGenerator {

    /**
     * OpenSimplexNoise library.
     */
    private final OpenSimplexNoise simplexNoise;

    /**
     * Default constructor
     */
    public PerlinLevelGen(){
        simplexNoise = new OpenSimplexNoise(new Random().nextLong());
    }

    /**
     * Generate a heightmap of the specified size using layered Perlin noise.
     *
     * @param noiseWidth Width of the noise map.
     * @param noiseHeight Height of the noise map.
     * @param width Width of the heightmap.
     * @param height Height of the heightmap.
     * @return A width by height 2D array of doubles between 0..1
     */
    @Override
    public double[][] generateLevel(int noiseWidth, int noiseHeight, int width, int height, int octaves, double persistence) {
        double[][] noise = new double[width][width];
        //double[][] elevation = new double[width][height];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                double nx = ((x/(double)width))*noiseWidth, ny = ((y/(double)height))*noiseHeight;
                double e = 0;
                double amplitude = 1.0;
                double frequency = 1.0;
                double maxVal = 0;
                double lacunarity = 2.0;
                for(int currOctave = 0; currOctave < octaves; currOctave++){
                    double noiseVal = (amplitude * noise(frequency * nx, frequency * ny));
                    e += noiseVal ;
                    maxVal += amplitude;
                    amplitude *= persistence;
                    frequency *= lacunarity;
                }
                e /= maxVal;
                e = Math.pow(e,1.5);
                noise[x][y] = e;
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
        return  noise;
    }

    /**
     * Method to call to the noise library.
     * OpenSimplexNoise returns values between -1..1 and are scaled to 0..1
     *
     * @param nx Noise X Coordinate to evaluate with.
     * @param ny Noise Y Coordinate to evaluate with.
     * @return Noise value scaled between 0..1
     */
    private double noise(double nx, double ny){
        //return (simplexNoise.eval(nx,ny, 0.5) + 1.0)/2.0;
        return (simplexNoise.eval(nx,ny)/2.0)+0.5;
    }

}

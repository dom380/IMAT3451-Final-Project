package dmu.project.levelgen;

import java.util.Random;

import dmu.project.noise.OpenSimplexNoise;

/**
 * Created by Dom on 18/11/2016.
 */

public class PerlinLevelGen implements LevelGenerator {
    private final OpenSimplexNoise simplexNoise;

    public PerlinLevelGen(){
        simplexNoise = new OpenSimplexNoise(new Random().nextLong());
    }

    @Override
    public double[][] generateLevel(int noiseWidth, int noiseHeight, int width, int height) {
        double[][] noise = new double[noiseWidth][noiseHeight];
        double[][] elevation = new double[width][height];
        for (int y = 0; y < noiseHeight; ++y) {
            for (int x = 0; x < noiseWidth; ++x) {
                double nx = (x/(double)noiseWidth)-0.5, ny = (y/(double)noiseHeight)-0.5;
                double e = (1.00 * noise( 1 * nx,  1 * ny)
                        + 0.50 * noise( 2 * nx,  2 * ny)
                        + 0.25 * noise( 4 * nx,  4 * ny)
                        + 0.13 * noise( 8 * nx,  8 * ny)
                        + 0.06 * noise(16 * nx, 16 * ny)
                        + 0.03 * noise(32 * nx, 32 * ny));
                e /= (1.00+0.50+0.25+0.13+0.06+0.03);
                e = Math.pow(e,1.5);
                noise[x][y] = e;
            }
        }
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                elevation[x][y] = noise[(x*noiseWidth/width)][(y*noiseHeight/height)];
            }
        }
        return  elevation;
    }

    public double noise(double nx, double ny){
        return  simplexNoise.eval(nx,ny)/2 +0.5;
    }

}

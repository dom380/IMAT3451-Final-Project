package dmu.project.levelgen;

/**
 * Created by Dom on 18/11/2016.
 */

public interface LevelGenerator {

    double[][] generateLevel(int noiseWidth, int noiseHeight, int width, int height);

}


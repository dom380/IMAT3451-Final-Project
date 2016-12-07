package dmu.project.levelgen;

/**
 * Interface for the base terrain generator.
 * Classes that implement this interface are responsible for generating the level heightmap.
 *
 * Created by Dom on 18/11/2016.
 */

public interface LevelGenerator {

    double[][] generateLevel(int noiseWidth, int noiseHeight, int width, int height);

}


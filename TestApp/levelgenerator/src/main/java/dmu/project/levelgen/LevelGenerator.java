package dmu.project.levelgen;

/**
 * Interface for the base terrain generator.
 * Classes that implement this interface are responsible for generating the level heightmap.
 *
 * Created by Dom on 18/11/2016.
 */

public interface LevelGenerator {

    /**
     * Generate a level's heightmap
     *
     * @param noiseWidth
     * @param noiseHeight
     * @param width
     * @param height
     * @param octaves
     * @param persistence
     * @return
     */
    double[][] generateLevel(int noiseWidth, int noiseHeight, int width, int height, int octaves, double persistence);

}


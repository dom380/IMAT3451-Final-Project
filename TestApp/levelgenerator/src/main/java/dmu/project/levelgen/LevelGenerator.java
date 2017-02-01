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
     * @param width the width of the level.
     * @param height the height of the level.
     * @param waterLevel a value between 0..1 that represents the water level for the generated map.
     * @return A 2D width by height array of height values representing the terrain.
     */
    HeightMap generateLevel(int width, int height, float waterLevel);

}


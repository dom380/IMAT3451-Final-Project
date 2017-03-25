package dmu.project.levelgen;

import java.util.List;

/**
 * Class representing a potential game level within the Genetic Algorithm.
 *
 * Created by Dom on 30/11/2016.
 */

public class MapCandidate {
    /**
     * Default Constructor.
     * @param tiles List of Tiles representing the level.
     */
    public MapCandidate(List<Tile> tiles){
        tileSet = tiles;
        fitness = 0.f;
    }

    /**
     * List of Tiles representing the level.
     */
    public List<Tile> tileSet;

    /**
     * Fitness value of the level.
     */
    public Float fitness;

}

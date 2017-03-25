package dmu.project.levelgen;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dmu.project.utils.Vector2D;

/**
 * Created by Dom on 10/02/2017.
 * Utility class handle creating MapCandidates for Genetic algorithm.
 */

public class CandidateFactory {

    private static Random sRng = new Random();
    private HeightMap mHeightMap;
    private int mWidth;
    private int mHeight;
    private boolean mObjectivesEnabled;

    /**
     * Constructor
     *
     * @param mHeightMap         The heightmap.
     * @param mWidth             The mWidth of the map.
     * @param mHeight            The mHeight of the map.
     * @param mObjectivesEnabled True if objects should be added.
     */
    CandidateFactory(HeightMap mHeightMap, int mWidth, int mHeight, boolean mObjectivesEnabled) {
        this.mHeightMap = mHeightMap;
        this.mWidth = mWidth;
        this.mHeight = mHeight;
        this.mObjectivesEnabled = mObjectivesEnabled;
    }

    /**
     * Constructor
     *
     * @param mHeightMap         The heightmap.
     * @param mWidth             The mWidth of the map.
     * @param mHeight            The mHeight of the map.
     * @param mObjectivesEnabled True if objects should be added.
     * @param seed               The Random number seed to use.
     */
    CandidateFactory(HeightMap mHeightMap, int mWidth, int mHeight, boolean mObjectivesEnabled, long seed) {
        this.mHeightMap = mHeightMap;
        this.mWidth = mWidth;
        this.mHeight = mHeight;
        this.mObjectivesEnabled = mObjectivesEnabled;
        sRng = new Random(seed);
    }

    /**
     * Creates the MapCandidate at the specified difficulty.
     * <p>
     * On Higher Difficulty (7-10):
     * - Objectives, Enemies, Obstacles, Items
     * - Bias more objectives?
     * - Bias more enemies?
     * - Limit items
     * Mid difficulty (4-6):
     * - Objectives, Obstacles, Enemies, Items
     * - Bias less objectives
     * Low difficulty (1-3):
     * - Objectives, Obstacles, Enemies, Items
     * - bias less enemies
     * - slightly more items?
     * - hard limit objectives to 1-3.
     *
     * @param difficulty The difficulty of the map.
     * @return The populated MapCandidate.
     */
    public MapCandidate createCandidate(int difficulty) {
        List<Tile> tileSet = new ArrayList<>();
        Set<Vector2D> usedTiles = new LinkedHashSet<>();
        addTiles(tileSet, TileState.START, 1, 1, usedTiles);
        int freeTiles = Math.max(sRng.nextInt((mHeightMap.aboveWaterValues / 5) - 1), 200); //This probably should be tweaked as well
        freeTiles -= addObjectives(tileSet, difficulty, freeTiles, usedTiles);
        freeTiles -= addEnemies(tileSet, difficulty, freeTiles, usedTiles);
        freeTiles -= addObstacles(tileSet, difficulty, freeTiles, usedTiles);
        addItems(tileSet, difficulty, freeTiles, usedTiles);
        return new MapCandidate(tileSet);
    }

    /////////////////////////////
    //Private Utility Methods //
    ////////////////////////////

    /**
     * Add the objects to the map in a random position.
     *
     * @param tileSet    The current list of objects.
     * @param difficulty The difficulty.
     * @param freeTiles  The number of free tiles.
     * @param usedTiles  The set of used tiles.
     * @return The number of tiles used.
     */
    private int addObjectives(List<Tile> tileSet, int difficulty, int freeTiles, Set<Vector2D> usedTiles) {
        if (mObjectivesEnabled) {
            int numOfObjectives;
            if (difficulty == 1) {
                numOfObjectives = 1;
            } else {
                int minObjectives = difficulty / 2;
                numOfObjectives = Math.max(sRng.nextInt(difficulty - 1) + 1, minObjectives); //Add objectives
            }
            addTiles(tileSet, TileState.OBJECTIVE, numOfObjectives, freeTiles, usedTiles);
            return numOfObjectives;
        } else
            return 0;
    }

    /**
     * Add the enemies to the map.
     *
     * @param tileSet    The current list of objects.
     * @param difficulty The difficulty.
     * @param freeTiles  The number of free tiles.
     * @param usedTiles  The set of used tiles.
     * @return The number of tiles used.
     */
    private int addEnemies(List<Tile> tileSet, int difficulty, int freeTiles, Set<Vector2D> usedTiles) {
        int numOfEntity;
        numOfEntity = difficulty * 10;
        addTiles(tileSet, TileState.ENEMY, numOfEntity, freeTiles, usedTiles);
        return numOfEntity;
    }

    /**
     * Add the items to the map.
     *
     * @param tileSet    The current list of objects.
     * @param difficulty The difficulty.
     * @param freeTiles  The number of free tiles.
     * @param usedTiles  The set of used tiles.
     * @return The number of tiles used.
     */
    private int addItems(List<Tile> tileSet, int difficulty, int freeTiles, Set<Vector2D> usedTiles) {
        int numOfEntity;
        if (difficulty < 4) {
            numOfEntity = sRng.nextInt(freeTiles / 6);
        } else {
            numOfEntity = sRng.nextInt(freeTiles / 8); //Higher difficulty = less items
        }
        addTiles(tileSet, TileState.ITEM, numOfEntity, freeTiles, usedTiles);
        return numOfEntity;
    }

    /**
     * Add the obstacles to the map.
     *
     * @param tileSet    The current list of objects.
     * @param difficulty The difficulty.
     * @param freeTiles  The number of free tiles.
     * @param usedTiles  The set of used tiles.
     * @return The number of tiles used.
     */
    private int addObstacles(List<Tile> tileSet, int difficulty, int freeTiles, Set<Vector2D> usedTiles) {
        int x, y;
        int numOfEntity = sRng.nextInt(freeTiles / 2);
        addTiles(tileSet, TileState.OBSTACLE, numOfEntity, freeTiles, usedTiles);
        return numOfEntity;
    }


    /**
     * Utility method to actually add the object to the map.
     * Randomly selects a free tile and places the
     *
     * @param tileSet     The current list of objects.
     * @param tileState   The type of object to add.
     * @param numOfEntity The number to add.
     * @param freeTiles   The number of free tiles.
     * @param usedTiles   The set of used tiles.
     */
    public void addTiles(List<Tile> tileSet, TileState tileState, int numOfEntity, int freeTiles, Set<Vector2D> usedTiles) {
        int x, y;
        for (int i = 0; i < numOfEntity; ++i) {
            Vector2D position;
            do {
                x = sRng.nextInt(mWidth - 2) + 2; //Avoid the edges
                y = sRng.nextInt(mHeight - 2) + 2;
                position = new Vector2D(x, y);
            }
            while (mHeightMap.elevation[x][y] < mHeightMap.waterLevel || usedTiles.contains(position)); //keep looking for a value above water level
            tileSet.add(new Tile(tileState, x, y));
            usedTiles.add(position);
            freeTiles -= 1;
        }
    }

}

package dmu.project.levelgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Dom on 10/02/2017.
 */

public class CandidateFactory {
    /* Will require some tweaking
            Things that impact difficulty:
                - Order of entities being added
                - Number of tiles available
             On Higher Difficulty (say 7-10):
                - Objectives, Enemies, Obstacles, Items
                - Bias more objectives?
                - Bias more enemies?
                - Limit items
             Mid difficulty (4-6):
                - Objectives, Obstacles, Enemies, Items
                - Bias less objectives
             Low difficulty (1-3):
                - Objectives, Obstacles, Enemies, Items
                - bias less enemies
                - slightly more items?
                - hard limit objectives to 1-3.
         */
    public static MapCandidate createCandidate(HeightMap heightMap, int width, int height, int difficulty, boolean objectivesEnabled) {
        List<Tile> tileSet = new ArrayList<>();
        //Add Start tile
        int x, y;
        do {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
        } while (heightMap.elevation[x][y] < heightMap.waterLevel);
        tileSet.add(new Tile(TileState.START, x, y));
        Integer freeTiles = rng.nextInt((heightMap.aboveWaterValues / 10) - 2) + 100; //This probably should be tweaked as well
        if (difficulty < 7) { //Order affects difficulty slightly
            addObjectives(heightMap, tileSet, width, height, difficulty, objectivesEnabled, freeTiles);
            addObstacles(heightMap, tileSet, width, height, difficulty, freeTiles);
            addEnemies(heightMap, tileSet, width, height, difficulty, freeTiles);
            addItems(heightMap, tileSet, width, height, difficulty, freeTiles);
        } else {
            addObjectives(heightMap, tileSet, width, height, difficulty, objectivesEnabled, freeTiles);
            addEnemies(heightMap, tileSet, width, height, difficulty, freeTiles);
            addObstacles(heightMap, tileSet, width, height, difficulty, freeTiles);
            addItems(heightMap, tileSet, width, height, difficulty, freeTiles);
        }
        return new MapCandidate(tileSet);
    }

    private static void addObjectives(HeightMap heightMap, List<Tile> tileSet, int width, int height, int difficulty, boolean objectivesEnabled, Integer freeTiles) {
        if (objectivesEnabled) {
            int minObjectives = difficulty / 2 == 0 ? 1 : difficulty / 2;
            int numOfObjectives = Math.max(rng.nextInt(difficulty - 1) + 1, minObjectives); //Add objectives
            addTiles(heightMap, tileSet, TileState.OBJECTIVE, numOfObjectives, width, height, freeTiles);
        }
    }

    private static void addEnemies(HeightMap heightMap, List<Tile> tileSet, int width, int height, int difficulty, Integer freeTiles) {
        int numOfEntity;
        if (difficulty < 4) {
            numOfEntity = rng.nextInt(freeTiles / 4); //Add enemies
        } else {
            numOfEntity = rng.nextInt(freeTiles / 2);
        }
        addTiles(heightMap, tileSet, TileState.ENEMY, numOfEntity, width, height, freeTiles);
    }

    private static void addItems(HeightMap heightMap, List<Tile> tileSet, int width, int height, int difficulty, Integer freeTiles) {
        int numOfEntity;
        if (difficulty < 4) {
            numOfEntity = rng.nextInt(freeTiles / 4);
        } else {
            numOfEntity = rng.nextInt(freeTiles / 6); //Higher difficulty = less items
        }
        addTiles(heightMap, tileSet, TileState.ITEM, numOfEntity, width, height, freeTiles);
    }

    private static void addObstacles(HeightMap heightMap, List<Tile> tileSet, int width, int height, int difficulty, Integer freeTiles) {
        int x, y;
        int numOfEntity = rng.nextInt(freeTiles / 2);
        for (int i = 0; i < numOfEntity; ++i) { //Add obstacles
            do {
                x = rng.nextInt(width);
                y = rng.nextInt(height);
            }
            while (heightMap.elevation[x][y] < heightMap.waterLevel || heightMap.elevation[x][y] >= 0.65); //keep looking for a value above water level
            tileSet.add(new Tile(TileState.OBSTACLE, x, y));
            freeTiles--;
        }
    }

    private static void addTiles(HeightMap heightMap, List<Tile> tileSet, TileState tileState, int numOfEntity, int width, int height, Integer freeTiles) {
        int x, y;
        for (int i = 0; i < numOfEntity; ++i) {
            do {
                x = rng.nextInt(width);
                y = rng.nextInt(height);
            }
            while (heightMap.elevation[x][y] < heightMap.waterLevel); //keep looking for a value above water level
            tileSet.add(new Tile(tileState, x, y));
            freeTiles--;
        }
    }

    private final static Random rng = new Random();

}

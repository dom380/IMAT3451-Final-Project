package dmu.project.levelgen;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dmu.project.utils.Vector2D;

/**
 * Created by Dom on 10/02/2017.
 */

public class CandidateFactory {

    private static Random rng = new Random();
    private HeightMap heightMap;
    private int width;
    private int height;
    private boolean objectivesEnabled;

    CandidateFactory(HeightMap heightMap, int width, int height, boolean objectivesEnabled) {
        this.heightMap = heightMap;
        this.width = width;
        this.height = height;
        this.objectivesEnabled = objectivesEnabled;
    }

    CandidateFactory(HeightMap heightMap, int width, int height, boolean objectivesEnabled, long seed) {
        this.heightMap = heightMap;
        this.width = width;
        this.height = height;
        this.objectivesEnabled = objectivesEnabled;
        rng = new Random(seed);
    }

    /* TODO Requires tweaking
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
    public MapCandidate createCandidate(int difficulty) {
        List<Tile> tileSet = new ArrayList<>();
        Set<Vector2D> usedTiles = new LinkedHashSet<>();
        addTiles(tileSet, TileState.START, 1, 1, usedTiles);
        int freeTiles = Math.max(rng.nextInt((heightMap.aboveWaterValues / 5) - 1), 200); //This probably should be tweaked as well
//        if (difficulty < 7) { //Order affects difficulty slightly
//            freeTiles -= addObjectives(tileSet, difficulty, freeTiles, usedTiles);
//            freeTiles -= addObstacles(tileSet, difficulty, freeTiles, usedTiles);
//            freeTiles -= addEnemies(tileSet, difficulty, freeTiles, usedTiles);
//            addItems(tileSet, difficulty, freeTiles, usedTiles);
//        } else {
            freeTiles -= addObjectives(tileSet, difficulty, freeTiles, usedTiles);
            freeTiles -= addEnemies(tileSet, difficulty, freeTiles, usedTiles);
            freeTiles -= addObstacles(tileSet, difficulty, freeTiles, usedTiles);
            addItems(tileSet, difficulty, freeTiles, usedTiles);
//        }
        return new MapCandidate(tileSet);
    }

    private int addObjectives(List<Tile> tileSet, int difficulty, int freeTiles, Set<Vector2D> usedTiles) {
        if (objectivesEnabled) {
            int numOfObjectives;
            if (difficulty == 1) {
                numOfObjectives = 1;
            } else {
                int minObjectives = difficulty / 2;
                numOfObjectives = Math.max(rng.nextInt(difficulty - 1) + 1, minObjectives); //Add objectives
            }
            addTiles(tileSet, TileState.OBJECTIVE, numOfObjectives, freeTiles, usedTiles);
            return numOfObjectives;
        } else
            return 0;
    }

    private int addEnemies(List<Tile> tileSet, int difficulty, int freeTiles, Set<Vector2D> usedTiles) {
        int numOfEntity;
//        if (difficulty < 4) {
//            numOfEntity = rng.nextInt((freeTiles / 4) - (freeTiles / 6)) + (freeTiles / 6); //Add enemies
//        } else {
//            numOfEntity = rng.nextInt((freeTiles / 2) - (freeTiles / 4)) + (freeTiles / 4);
//        }
        numOfEntity = difficulty * 10;
        addTiles(tileSet, TileState.ENEMY, numOfEntity, freeTiles, usedTiles);
        return numOfEntity;
    }

    private int addItems(List<Tile> tileSet, int difficulty, int freeTiles, Set<Vector2D> usedTiles) {
        int numOfEntity;
        if (difficulty < 4) {
            numOfEntity = rng.nextInt(freeTiles / 6);
        } else {
            numOfEntity = rng.nextInt(freeTiles / 8); //Higher difficulty = less items
        }
        addTiles(tileSet, TileState.ITEM, numOfEntity, freeTiles, usedTiles);
        return numOfEntity;
    }

    private int addObstacles(List<Tile> tileSet, int difficulty, int freeTiles, Set<Vector2D> usedTiles) {
        int x, y;
        int numOfEntity = rng.nextInt(freeTiles / 2);
        addTiles(tileSet, TileState.OBSTACLE, numOfEntity, freeTiles, usedTiles);
        return numOfEntity;
    }

    public void addTiles(List<Tile> tileSet, TileState tileState, int numOfEntity, int freeTiles, Set<Vector2D> usedTiles) {
        int x, y;
        for (int i = 0; i < numOfEntity; ++i) {
            Vector2D position;
            do {
                x = rng.nextInt(width - 2) + 2; //Avoid the edges
                y = rng.nextInt(height - 2) + 2;
                position = new Vector2D(x, y);
            }
            while (heightMap.elevation[x][y] < heightMap.waterLevel || usedTiles.contains(position)); //keep looking for a value above water level
            tileSet.add(new Tile(tileState, x, y));
            usedTiles.add(position);
            freeTiles -= 1;
        }
    }

}

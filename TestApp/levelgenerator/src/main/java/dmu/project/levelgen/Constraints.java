package dmu.project.levelgen;

/**
 * Created by Dom on 30/11/2016.
 */

public class Constraints {
    int mapWidth;
    int mapHeight;
    float length;
    int populationSize;
    /**
     * Value from 0.0 to 1.0 that represents the percentage of tiles that can be populated.
     */
    float tilePercentage;

    int maxGenerations;

    //maybe change these to difficulty instead of constraints?
    int numOfObjectives;
    int enemyLimit;
    int itemLimit;

    public int getMapWidth() {
        return mapWidth;
    }

    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void setMapHeight(int mapHeight) {
        this.mapHeight = mapHeight;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public int getNumOfObjectives() {
        return numOfObjectives;
    }

    public void setNumOfObjectives(int numOfObjectives) {
        this.numOfObjectives = numOfObjectives;
    }

    public int getEnemyLimit() {
        return enemyLimit;
    }

    public void setEnemyLimit(int enemyLimit) {
        this.enemyLimit = enemyLimit;
    }

    public int getItemLimit() {
        return itemLimit;
    }

    public void setItemLimit(int itemLimit) {
        this.itemLimit = itemLimit;
    }

    public int getMaxGenerations() {
        return maxGenerations;
    }

    public void setMaxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public float getTilePercentage() {
        return tilePercentage;
    }

    public void setTilePercentage(float tilePercentage) {
        this.tilePercentage = tilePercentage;
    }
}

package dmu.project.levelgen;

/**
 * Created by Dom on 30/11/2016.
 */

public class Constraints {
    int mapWidth;
    int mapHeight;
    float length;

    int maxGenerations;

    //map change these to difficulty instead of constraints???
    int numOfObjectives;
    float enemyDensity;
    float itemDensity;

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

    public float getEnemyDensity() {
        return enemyDensity;
    }

    public void setEnemyDensity(float enemyDensity) {
        this.enemyDensity = enemyDensity;
    }

    public float getItemDensity() {
        return itemDensity;
    }

    public void setItemDensity(float itemDensity) {
        this.itemDensity = itemDensity;
    }

    public int getMaxGenerations() {
        return maxGenerations;
    }

    public void setMaxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }
}

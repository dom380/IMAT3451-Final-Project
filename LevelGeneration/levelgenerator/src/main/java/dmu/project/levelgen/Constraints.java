package dmu.project.levelgen;

/**
 * Class representing the rules and constraints of the level generation.
 *
 * Created by Dom on 30/11/2016.
 */

public class Constraints {
    /**
     * Width of map
     */
    int mapWidth = 50;
    /**
     * height of the map
     */
    int mapHeight = 80;
    /**
     * Target length of the path between start and end points of the level.
     */
    float length = 0.0f;
    /**
     * Number of candidates within each generation
     */
    int populationSize = 100;
    /**
     * Maximum number of generations to be evaluated before returning.
     */
    int maxGenerations = 50;

    boolean objectivesEnabled = true;
    /**
     *  A value between 1-10 representing how difficult the generated level should be.
     */
    int difficulty = 5;

    int noiseWidth = 4;

    int noiseHeight = 4;

    long seed = -1;

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

    public int getNoiseWidth() {
        return noiseWidth;
    }

    public void setNoiseWidth(int noiseWidth) {
        this.noiseWidth = noiseWidth;
    }

    public int getNoiseHeight() {
        return noiseHeight;
    }

    public void setNoiseHeight(int noiseHeight) {
        this.noiseHeight = noiseHeight;
    }

    public boolean isObjectivesEnabled() {
        return objectivesEnabled;
    }

    public void setObjectivesEnabled(boolean objectivesEnabled) {
        this.objectivesEnabled = objectivesEnabled;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        if(difficulty > 10)
            this.difficulty = 10;
        else if(difficulty < 1)
            this.difficulty = 1;
        else
            this.difficulty = difficulty;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }
}

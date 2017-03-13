package dmu.project.levelgen;

import dmu.project.utils.Grid;

/**
 * Created by Dom on 19/01/2017.
 * Class representing the level's height map.
 */

public class HeightMap {
    public HeightMap(int xMax, int yMax, boolean moveDiagonal, double waterLevel){
        aboveWaterValues = 0;
        this.waterLevel = waterLevel;
        grid = new Grid(xMax, yMax, moveDiagonal);
    }
    public int aboveWaterValues;
    public double[][] elevation;
    public Grid grid;
    public double waterLevel;
}

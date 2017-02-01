package dmu.project.levelgen;

import dmu.project.utils.Grid;

/**
 * Created by Dom on 19/01/2017.
 */

public class HeightMap {
    public HeightMap(int xMax, int yMax, boolean moveDiagonal){
        aboveWaterValues = 0;
        grid = new Grid(xMax, yMax, moveDiagonal);
    }
    public int aboveWaterValues;
    public double[][] elevation;
    public Grid grid;
}

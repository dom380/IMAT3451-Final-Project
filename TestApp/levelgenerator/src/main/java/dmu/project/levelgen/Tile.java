package dmu.project.levelgen;

/**
 * Created by Dom on 30/11/2016.
 */

public class Tile {
    public Tile(TileState state, double elevation){
        this.tileState = state;
        this.elevation = elevation;
    }
    public TileState tileState;
    public double elevation;
}

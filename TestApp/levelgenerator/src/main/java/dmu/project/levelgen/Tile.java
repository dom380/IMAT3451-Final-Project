package dmu.project.levelgen;

/**
 * Created by Dom on 30/11/2016.
 */

public class Tile {

    /**
     * Copy constructor
     * @param tile
     */
    public Tile(Tile tile){
        this.position = tile.position;
        this.tileState = tile.tileState;
    }

    public Tile(TileState state, int[] position){
        this.tileState = state;
        this.position = position;
    }
    public Tile(TileState state, int x, int y){
        this.tileState = state;
        this.position[0] = x;
        this.position[1] = y;
    }
    public TileState tileState;
    public int[] position = new int[2];
}

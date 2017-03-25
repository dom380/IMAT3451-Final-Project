package dmu.project.levelgen;

/**
 * Class representing a tile within the map.
 * <p>
 * Created by Dom on 30/11/2016.
 */

public class Tile {

    public TileState tileState;
    public int[] position = new int[2];
    public boolean active = true;

    /**
     * Copy constructor.
     *
     * @param tile
     */
    public Tile(Tile tile) {
        this.position = tile.position;
        this.tileState = tile.tileState;
    }

    /**
     * Constructor
     *
     * @param state    TileState enum of what this tile represents.
     * @param position Two value integer array representing the x,y position of the tile.
     */
    public Tile(TileState state, int[] position) {
        this(state, position, true);
    }

    /**
     * Constructor
     *
     * @param state    TileState enum of what this tile represents.
     * @param position Two value integer array representing the x,y position of the tile.
     * @param active   Boolean stating whether the tile can be interacted with.
     */
    public Tile(TileState state, int[] position, boolean active) {
        this.tileState = state;
        this.position = position;
        this.active = active;
    }

    /**
     * Constructor
     *
     * @param state TileState enum of what this tile represents.
     * @param x     X coordinate of the tile.
     * @param y     Y coordinate of the tile.
     */
    public Tile(TileState state, int x, int y) {
        this.tileState = state;
        this.position[0] = x;
        this.position[1] = y;
    }

}

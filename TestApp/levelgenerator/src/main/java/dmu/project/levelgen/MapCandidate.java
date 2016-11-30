package dmu.project.levelgen;

/**
 * Created by Dom on 30/11/2016.
 */

public class MapCandidate {
    public MapCandidate(Tile[][] tiles){
        tileSet = tiles;
        fitness = 0.f;
    }
    public Tile[][] tileSet;
    public Float fitness;
}

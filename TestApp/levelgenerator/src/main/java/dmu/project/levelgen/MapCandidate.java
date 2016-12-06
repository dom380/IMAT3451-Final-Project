package dmu.project.levelgen;

import java.util.List;

/**
 * Created by Dom on 30/11/2016.
 */

public class MapCandidate {
    public MapCandidate(List<Tile> tiles){
        tileSet = tiles;
        fitness = 0.f;
    }
    public List<Tile> tileSet;
    public Float fitness;
}

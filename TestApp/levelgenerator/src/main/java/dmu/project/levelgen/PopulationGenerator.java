package dmu.project.levelgen;

import java.util.List;

/**
 * Created by Dom on 30/11/2016.
 */

public interface PopulationGenerator {
    List<Tile> populate(Progress progress);
    void readConstraints(String filePath);
    void setConstraints(Constraints constraints);
}

package dmu.project.levelgen;

/**
 * Created by Dom on 30/11/2016.
 */

public interface PopulationGenerator {
    Tile[][] populate();
    void readConstraints(String filePath);
    void setConstraints(Constraints constraints);
}

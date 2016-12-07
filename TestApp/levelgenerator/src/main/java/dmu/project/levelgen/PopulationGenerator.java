package dmu.project.levelgen;

import java.util.List;

/**
 * Interface for the PopulationGenerator. Classes that implement this interface are responsible
 * placement of game objects within the level via the Tile class.
 *
 * Created by Dom on 30/11/2016.
 */

public interface PopulationGenerator {

    List<Tile> populate();

    /**
     * Read in level generation constraints from specified xml file.
     *
     * @param filePath File path to the constraints file.
     */
    void readConstraints(String filePath);

    /**
     * Setter for the Constraints object.
     *
     * @param constraints
     */
    void setConstraints(Constraints constraints);
}

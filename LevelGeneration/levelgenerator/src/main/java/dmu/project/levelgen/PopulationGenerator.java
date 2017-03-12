package dmu.project.levelgen;

import java.io.File;
import java.util.List;

import dmu.project.levelgen.exceptions.LevelConstraintsException;
import dmu.project.levelgen.exceptions.LevelGenerationException;

/**
 * Interface for the PopulationGenerator. Classes that implement this interface are responsible
 * placement of game objects within the level via the Tile class.
 *
 * Created by Dom on 30/11/2016.
 */

public interface PopulationGenerator {

    List<MapCandidate> populate() throws LevelGenerationException;

    /**
     * Read in level generation constraints from specified xml file.
     *
     * @param file Java File object representing the constraints xml file.
     */
    void readConstraints(File file) throws LevelConstraintsException;

    /**
     * Setter for the Constraints object.
     *
     * @param constraints
     */
    void setConstraints(Constraints constraints);
}

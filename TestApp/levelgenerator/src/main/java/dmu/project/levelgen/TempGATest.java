package dmu.project.levelgen;

import com.google.common.base.Stopwatch;

import java.util.List;
import java.util.concurrent.TimeUnit;

import dmu.project.levelgen.exceptions.LevelGenerationException;

/**
 * Temporary test class. To be removed.
 *
 * Created by Dom on 30/11/2016.
 */

public class TempGATest {
    public static void main(String[] args) throws LevelGenerationException {
        Constraints constraints = new Constraints();
        constraints.length = 500;
        constraints.maxGenerations = 1000;
        constraints.mapHeight = 256;
        constraints.mapWidth = 256;
        constraints.populationSize = 100;
        Stopwatch stopwatch = Stopwatch.createStarted();
        GAPopulationGen gen = new GAPopulationGen(constraints);
        List<MapCandidate> mapCandidates = gen.populate();
        stopwatch.stop();
        long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
        elapsed = 0;
    }
}

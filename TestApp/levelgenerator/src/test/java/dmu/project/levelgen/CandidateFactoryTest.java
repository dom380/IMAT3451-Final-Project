package dmu.project.levelgen;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dmu.project.levelgen.exceptions.LevelGenerationException;

/**
 * Created by Dom on 12/03/2017.
 */

@RunWith(JUnit4.class)
public class CandidateFactoryTest {
    private long seed = -2656433763347937011L;
    private LevelGenerator levelGenerator = new PerlinLevelGen(seed, 4, 4, 8, 0.5);
    private Constraints defaultConstraints = new Constraints();
    private HeightMap heightMap;

    @Before
    public void setup() throws LevelGenerationException {
        heightMap = levelGenerator.generateLevel(defaultConstraints.mapWidth, defaultConstraints.mapHeight, 0.25f);
    }


    @Test
    public void createCandidateTest() {
        CandidateFactory candidateFactory = new CandidateFactory(heightMap, defaultConstraints.mapWidth, defaultConstraints.mapHeight, true, seed);
        MapCandidate candidate = candidateFactory.createCandidate(defaultConstraints.difficulty);
        Assert.assertNotNull(candidate);
        Assert.assertTrue("Fitness should be initialised to 0", candidate.fitness == 0.0f);
        Assert.assertTrue("Tile set should be less than the maximum value", candidate.tileSet.size() > 0 && candidate.tileSet.size() < heightMap.aboveWaterValues/5);
    }

}

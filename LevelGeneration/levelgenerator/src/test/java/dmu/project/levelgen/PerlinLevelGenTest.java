package dmu.project.levelgen;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dmu.project.levelgen.exceptions.LevelGenerationException;

/**
 * Created by Dom on 07/03/2017.
 */
@RunWith(JUnit4.class)
public class PerlinLevelGenTest {

    private final static long seed = -2656433763347937011L;
    private final static double delta = 0.001;

    @Test
    public void perlinNoiseTest() throws LevelGenerationException {
        int originX = 0, originY = 0, noiseWidth = 1, noiseHeight = 1, octaves = 8, width = 512, height = 512;
        float persistance = 0.5f, waterLevel = 0.25f;
        PerlinLevelGen levelGen = new PerlinLevelGen(seed, originX, originY, noiseWidth, noiseHeight, octaves, persistance);
        HeightMap heightMap = levelGen.generateLevel(width, height, waterLevel);
        Assert.assertEquals("Returned heightmap width should match", width, heightMap.elevation.length);
        Assert.assertEquals("Returned heightmap height should match", height, heightMap.elevation[0].length);
        testElevationRange(width, height, heightMap.elevation);
    }

    @Test
    public void perlinNoiseTest_repeatSameSeed() throws LevelGenerationException {
        int originX = 0, originY = 0, noiseWidth = 1, noiseHeight = 1, octaves = 8, width = 256, height = 256;
        float persistance = 0.5f, waterLevel = 0.25f;
        PerlinLevelGen levelGen = new PerlinLevelGen(seed, originX, originY, noiseWidth, noiseHeight, octaves, persistance);
        //Generate Heightmap 1
        HeightMap heightMap = levelGen.generateLevel(width, height, waterLevel);
        Assert.assertEquals("Returned heightmap width should match", width, heightMap.elevation.length);
        Assert.assertEquals("Returned heightmap height should match", height, heightMap.elevation[0].length);
        testElevationRange(width, height, heightMap.elevation);
        //Generate Heightmap 2
        HeightMap heightMap2 = levelGen.generateLevel(width, height, waterLevel);
        Assert.assertEquals("Returned heightmap width should match", width, heightMap2.elevation.length);
        Assert.assertEquals("Returned heightmap height should match", height, heightMap2.elevation[0].length);
        testElevationRange(width, height, heightMap2.elevation);
        //Test both maps match
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double elevation1 = heightMap.elevation[i][j];
                double elevation2 = heightMap2.elevation[i][j];
                Assert.assertEquals("Elevation should match.", elevation1, elevation2, delta);
            }
        }
    }

    @Test
    public void perlinNoiseTest_repeatSameSeedLoads() throws LevelGenerationException {
        for (int i = 0; i < 100; i++)
            perlinNoiseTest_repeatSameSeed();
    }

    @Test
    public void perlinNoiseTest_tiled() throws LevelGenerationException {
        int originX = 0, originY = 0, noiseWidth = 2, noiseHeight = 2, octaves = 8, width = 256, height = 256;
        float persistance = 0.5f, waterLevel = 0.25f;
        PerlinLevelGen levelGen = new PerlinLevelGen(seed, originX, originY, noiseWidth, noiseHeight, octaves, persistance);
        //Generate Heightmap 1
        HeightMap heightMap = levelGen.generateLevel(width, height, waterLevel);
        Assert.assertEquals("Returned heightmap width should match", width, heightMap.elevation.length);
        Assert.assertEquals("Returned heightmap height should match", height, heightMap.elevation[0].length);
        testElevationRange(width, height, heightMap.elevation);
        //Generate heightmap 2 at half the width/height and half the noise width/height. This should be the bottom left corner of heightmap1
        width = 128;
        height = 128;
        noiseHeight = 1;
        noiseWidth = 1;
        levelGen = new PerlinLevelGen(seed, originX, originY, noiseWidth, noiseHeight, octaves, persistance);
        HeightMap heightMap2 = levelGen.generateLevel(width, height, waterLevel);
        Assert.assertEquals("Returned heightmap width should match", width, heightMap2.elevation.length);
        Assert.assertEquals("Returned heightmap height should match", height, heightMap2.elevation[0].length);
        testElevationRange(width, height, heightMap2.elevation);
        //Assert that heightmap2 equals first quarter of heightmap 1
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double elevation1 = heightMap.elevation[i][j];
                double elevation2 = heightMap2.elevation[i][j];
                Assert.assertEquals("Elevation should match. At index" + i + "," + j, elevation1, elevation2, delta);
            }
        }
        //Generate heightmap3 at half the width/height and half the noise width/height, offset by 1 in x. This should be the bottom right corner of heightmap1.
        originX = 1;
        levelGen = new PerlinLevelGen(seed, originX, originY, noiseWidth, noiseHeight, octaves, persistance);
        HeightMap heightMap3 = levelGen.generateLevel(width, height, waterLevel);
        Assert.assertEquals("Returned heightmap width should match", width, heightMap2.elevation.length);
        Assert.assertEquals("Returned heightmap height should match", height, heightMap2.elevation[0].length);
        testElevationRange(width, height, heightMap3.elevation);
        int offset = 128;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double elevation1 = heightMap.elevation[i + offset][j];
                double elevation2 = heightMap3.elevation[i][j];
                Assert.assertEquals("Elevation should match. At index" + i + "," + j, elevation1, elevation2, delta);
            }
        }
        //Generate heightmap4 at half the width/height and half the noise width/height, offset by 1 in x and y. This should be the top right corner of heightmap1.
        originX = 1;
        originY = 1;
        levelGen = new PerlinLevelGen(seed, originX, originY, noiseWidth, noiseHeight, octaves, persistance);
        HeightMap heightMap4 = levelGen.generateLevel(width, height, waterLevel);
        Assert.assertEquals("Returned heightmap width should match", width, heightMap2.elevation.length);
        Assert.assertEquals("Returned heightmap height should match", height, heightMap2.elevation[0].length);
        testElevationRange(width, height, heightMap4.elevation);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double elevation1 = heightMap.elevation[i + offset][j + offset];
                double elevation2 = heightMap4.elevation[i][j];
                Assert.assertEquals("Elevation should match. At index" + i + "," + j, elevation1, elevation2, delta);
            }
        }
        //Generate heightmap5 at half the width/height and half the noise width/height, offset by 1 in y. This should be the top left corner of heightmap1.
        originX = 0;
        originY = 1;
        levelGen = new PerlinLevelGen(seed, originX, originY, noiseWidth, noiseHeight, octaves, persistance);
        HeightMap heightMap5 = levelGen.generateLevel(width, height, waterLevel);
        Assert.assertEquals("Returned heightmap width should match", width, heightMap2.elevation.length);
        Assert.assertEquals("Returned heightmap height should match", height, heightMap2.elevation[0].length);
        testElevationRange(width, height, heightMap5.elevation);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double elevation1 = heightMap.elevation[i][j + offset];
                double elevation2 = heightMap5.elevation[i][j];
                Assert.assertEquals("Elevation should match. At index" + i + "," + j, elevation1, elevation2, delta);
            }
        }
    }

    @Test
    public void testCorrectException() {
        int originX = 0, originY = 0, noiseWidth = -1, noiseHeight = -1, octaves = 8, width = -1, height = -1;
        float persistance = 0.5f, waterLevel = 0.25f;
        PerlinLevelGen levelGen = new PerlinLevelGen(seed, originX, originY, noiseWidth, noiseHeight, octaves, persistance);
        boolean exceptionThrown = false;
        try {
            levelGen.generateLevel(width, height, waterLevel);
        } catch (LevelGenerationException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("LevelGenerationException should have been thrown.", exceptionThrown);
        width = 0;
        height = 1;
        exceptionThrown = false;
        try {
            levelGen.generateLevel(width, height, waterLevel);
        } catch (LevelGenerationException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("LevelGenerationException should have been thrown.", exceptionThrown);
        width = 1;
        height = 0;
        exceptionThrown = false;
        try {
            levelGen.generateLevel(width, height, waterLevel);
        } catch (LevelGenerationException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("LevelGenerationException should have been thrown.", exceptionThrown);
        width = 1;
        height = 1;
        waterLevel = -1.0f;
        exceptionThrown = false;
        try {
            levelGen.generateLevel(width, height, waterLevel);
        } catch (LevelGenerationException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("LevelGenerationException should have been thrown.", exceptionThrown);
    }


    private void testElevationRange(int width, int height, double[][] elevation) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double elevationValue = elevation[i][j];
                Assert.assertTrue("Elevation must be less than or equal to 1", elevationValue <= 1.0);
                Assert.assertTrue("Elevation must be greater than or equal to 0.0", elevationValue >= 0.0);
            }
        }
    }
}

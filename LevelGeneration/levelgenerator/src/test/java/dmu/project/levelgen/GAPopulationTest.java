package dmu.project.levelgen;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dmu.project.levelgen.exceptions.LevelConstraintsException;
import dmu.project.levelgen.exceptions.LevelGenerationException;

/**
 * Created by Dom on 11/03/2017.
 */
@RunWith(JUnit4.class)
public class GAPopulationTest {

    private HeightMap heightMap;
    private long seed = -2656433763347937011L;
    private LevelGenerator levelGenerator = new PerlinLevelGen(seed, 4, 4, 8, 0.5);
    private Constraints defaultConstraints = new Constraints();
    private CandidateFactory candidateFactory;
    private static final float expectedFitness = 0.84999996f;

    public GAPopulationTest() throws LevelGenerationException {
        heightMap = levelGenerator.generateLevel(defaultConstraints.mapWidth, defaultConstraints.mapHeight, 0.25f);
        candidateFactory = new CandidateFactory(heightMap, defaultConstraints.mapWidth - 2, defaultConstraints.mapHeight - 2, true, seed);
    }

    @Before
    public void setup() throws LevelGenerationException {
        heightMap = levelGenerator.generateLevel(defaultConstraints.mapWidth, defaultConstraints.mapHeight, 0.25f);
        candidateFactory = new CandidateFactory(heightMap, defaultConstraints.mapWidth - 2, defaultConstraints.mapHeight - 2, true, seed);
    }

    @Test
    public void initPopulationTest() throws LevelGenerationException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Constraints constraints = new Constraints();
        constraints.seed = seed;
        GAPopulationGen populationGen = new GAPopulationGen(constraints);
        populationGen.setCandidateFactory(candidateFactory);
        Method initPopulation = populationGen.getClass().getDeclaredMethod("initPopulation", int.class, HeightMap.class);
        initPopulation.setAccessible(true);
        List<MapCandidate> result = (List<MapCandidate>) initPopulation.invoke(populationGen, 20, heightMap);
        Assert.assertNotNull(result);
        Assert.assertEquals("Should return 20 maps", 20, result.size());
        for (MapCandidate candidate : result) {
            validateMapCandidate(candidate, constraints);
        }
    }

    @Test
    public void initPopulation_InvalidSize() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Constraints constraints = new Constraints();
        constraints.seed = seed;
        GAPopulationGen populationGen = new GAPopulationGen(constraints);
        populationGen.setCandidateFactory(candidateFactory);
        Method initPopulation = populationGen.getClass().getDeclaredMethod("initPopulation", int.class, HeightMap.class);
        initPopulation.setAccessible(true);
        List<MapCandidate> result = (List<MapCandidate>) initPopulation.invoke(populationGen, -1, heightMap);
        Assert.assertNotNull("Result must not be null", result);
        Assert.assertTrue("Result should be empty", result.isEmpty());
    }

    @Test
    public void testMutate_Add() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        Constraints constraints = new Constraints();
        constraints.seed = seed;
        GAPopulationGen populationGen = new GAPopulationGen(constraints);
        populationGen.setCandidateFactory(candidateFactory);

        //Use reflection to set the heightmap field.
        Field heightMapField = populationGen.getClass().getDeclaredField("heightMap");
        heightMapField.setAccessible(true);
        heightMapField.set(populationGen, this.heightMap);

        //Use reflection to call initPopulation
        Method initPopulation = populationGen.getClass().getDeclaredMethod("initPopulation", int.class, HeightMap.class);
        initPopulation.setAccessible(true);
        List<MapCandidate> result = (List<MapCandidate>) initPopulation.invoke(populationGen, 20, this.heightMap);
        Assert.assertNotNull("Result can't be null", result);
        Assert.assertTrue("Result can't be empty", !result.isEmpty());

        //Use reflection to get mutate method.
        Method mutate = populationGen.getClass().getDeclaredMethod("mutate", MapCandidate.class);
        mutate.setAccessible(true);

        //Mock RNG.
        Random rng = Mockito.mock(Random.class);
        Mockito.when(rng.nextDouble())
                .thenReturn(0.1);

        //Replace RNG in populationGen.
        Field rngField = populationGen.getClass().getDeclaredField("rng");
        rngField.setAccessible(true);
        rngField.set(populationGen, rng);

        int previousSize = result.get(0).tileSet.size();
        MapCandidate mapCandidate = (MapCandidate) mutate.invoke(populationGen, result.get(0));
        Assert.assertNotNull("Mutated candidate can't be null", mapCandidate);
        Assert.assertEquals("New tile should have been added", previousSize + 1, mapCandidate.tileSet.size());
    }

    @Test
    public void testMutate_Change() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        Constraints constraints = new Constraints();
        constraints.seed = seed;
        GAPopulationGen populationGen = new GAPopulationGen(constraints);
        populationGen.setCandidateFactory(candidateFactory);

        //Use reflection to set the heightmap field.
        Field heightMapField = populationGen.getClass().getDeclaredField("heightMap");
        heightMapField.setAccessible(true);
        heightMapField.set(populationGen, this.heightMap);

        //Use reflection to call initPopulation
        Method initPopulation = populationGen.getClass().getDeclaredMethod("initPopulation", int.class, HeightMap.class);
        initPopulation.setAccessible(true);
        List<MapCandidate> result = (List<MapCandidate>) initPopulation.invoke(populationGen, 20, this.heightMap);
        Assert.assertNotNull("Result can't be null", result);
        Assert.assertTrue("Result can't be empty", !result.isEmpty());

        MapCandidate originalCandidate = result.get(0);
        List<Tile> originalTileSet = new ArrayList<>(); //Deep copy the original tile set.
        for (Tile tile : originalCandidate.tileSet) {
            originalTileSet.add(new Tile(tile));
        }

        //Use reflection to get mutate method.
        Method mutate = populationGen.getClass().getDeclaredMethod("mutate", MapCandidate.class);
        mutate.setAccessible(true);

        //Mock RNG.
        Random rng = Mockito.mock(Random.class);
        Mockito.when(rng.nextDouble())
                .thenReturn(0.1) //Return chance of mutation
                .thenReturn(0.6); //Then return chance of change rather than add.
        Mockito.when(rng.nextInt(result.get(0).tileSet.size()))
                .thenReturn(30);

        //Replace RNG in populationGen.
        Field rngField = populationGen.getClass().getDeclaredField("rng");
        rngField.setAccessible(true);
        rngField.set(populationGen, rng);

        int previousSize = result.get(0).tileSet.size();
        MapCandidate mapCandidate = (MapCandidate) mutate.invoke(populationGen, result.get(0));
        Assert.assertNotNull("Mutated candidate can't be null", mapCandidate);
        Assert.assertEquals("No new tiles should have been added", previousSize, mapCandidate.tileSet.size());

        int diffTileCount = 0;
        for (int i = 0; i < previousSize; i++) {
            Tile originalTile = originalTileSet.get(i);
            Tile newTile = mapCandidate.tileSet.get(i);
            Assert.assertEquals("Positions should match.", originalTile.position, newTile.position);
            if (originalTile.tileState != newTile.tileState)
                diffTileCount++;
        }
        Assert.assertEquals("Only 1 tile should have changed.", 1, diffTileCount);
    }

    @Test
    public void crossoverTest() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        int crossoverP1 = 30, crossoverP2 = 50;
        Constraints constraints = new Constraints();
        constraints.seed = seed;
        GAPopulationGen populationGen = new GAPopulationGen(constraints);
        populationGen.setCandidateFactory(candidateFactory);
        Method initPopulation = populationGen.getClass().getDeclaredMethod("initPopulation", int.class, HeightMap.class);
        initPopulation.setAccessible(true);
        List<MapCandidate> result = (List<MapCandidate>) initPopulation.invoke(populationGen, 2, heightMap);

        int arraySize = Math.min(result.get(0).tileSet.size(), result.get(1).tileSet.size());

        //Use reflection to get the crossover method
        Method crossover = populationGen.getClass().getDeclaredMethod("crossover", MapCandidate.class, MapCandidate.class);
        crossover.setAccessible(true);

        //Mock RNG.
        Random rng = Mockito.mock(Random.class);
        Mockito.when(rng.nextDouble())
                .thenReturn(0.6); //Make sure there's no mutation
        Mockito.when(rng.nextInt(arraySize))
                .thenReturn(crossoverP1)
                .thenReturn(crossoverP2);

        //Replace RNG in populationGen.
        Field rngField = populationGen.getClass().getDeclaredField("rng");
        rngField.setAccessible(true);
        rngField.set(populationGen, rng);

        List<MapCandidate> children = (List<MapCandidate>) crossover.invoke(populationGen, result.get(0), result.get(1));
        Assert.assertNotNull("Result must not be null", children);
        Assert.assertEquals("Result must include only 2 maps", 2, children.size());
        List<Tile> parent1 = result.get(0).tileSet;
        List<Tile> parent2 = result.get(1).tileSet;
        List<Tile> child1 = children.get(0).tileSet;
        List<Tile> child2 = children.get(1).tileSet;
        Assert.assertEquals("Parent and child tile size should match", parent1.size(), child1.size());
        Assert.assertEquals("Parent and child tile size should match", parent2.size(), child2.size());
        for (int i = 0; i < crossoverP1; i++) { //Should match parent up to this point
            compareTiles(parent1.get(i), child1.get(i));
            compareTiles(parent2.get(i), child2.get(i));
        }
        for (int i = crossoverP1 + 1; i < crossoverP2; i++) { //Should other parent between crossover points
            compareTiles(parent1.get(i), child2.get(i));
            compareTiles(parent2.get(i), child1.get(i));
        }
        for (int i = crossoverP2 + 1; i < arraySize; i++) { //Should match parent after 2nd crossover point
            compareTiles(parent1.get(i), child1.get(i));
            compareTiles(parent2.get(i), child2.get(i));
        }
    }

    @Test
    public void fitnessFunctionTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Constraints constraints = new Constraints();
        constraints.seed = seed;
        GAPopulationGen populationGen = new GAPopulationGen(constraints);
        populationGen.setCandidateFactory(candidateFactory);
        //Use reflection to set the heightmap field.
        Field heightMapField = populationGen.getClass().getDeclaredField("heightMap");
        heightMapField.setAccessible(true);
        heightMapField.set(populationGen, this.heightMap);

        Method initPopulation = populationGen.getClass().getDeclaredMethod("initPopulation", int.class, HeightMap.class);
        initPopulation.setAccessible(true);
        List<MapCandidate> result = (List<MapCandidate>) initPopulation.invoke(populationGen, 1, heightMap);


        Method testFitness = populationGen.getClass().getDeclaredMethod("testFitness", List.class);
        testFitness.setAccessible(true);
        float fitness = (float) testFitness.invoke(populationGen, result);
        Assert.assertTrue("Fitness must be in a range 0..1", fitness >= 0.0f && fitness <= 1.0);
        Assert.assertEquals("Returned fitness should match", expectedFitness, fitness, 0.001);
    }


    @Test
    public void readConstraintsFileTest() throws JsonProcessingException, LevelConstraintsException {
        GAPopulationGen populationGen = new GAPopulationGen();
        Constraints constraints = new Constraints();
        constraints.setSeed(12324634631L);
        constraints.setDifficulty(5);
        constraints.setObjectivesEnabled(true);
        constraints.setNoiseHeight(3);
        constraints.setNoiseWidth(3);
        constraints.setMapHeight(1);
        constraints.setMapWidth(1);
        constraints.setPopulationSize(100);
        constraints.setMaxGenerations(100);
        URL resource = this.getClass().getResource("validConstraints.xml");
        Assert.assertNotNull(resource);
        populationGen.readConstraints(new File(resource.getFile()));
        Constraints constraintsFromFile = populationGen.getConstraints();
        Assert.assertEquals("Seed should match", constraints.seed, constraintsFromFile.seed);
        Assert.assertEquals("Difficulty should match", constraints.difficulty, constraintsFromFile.difficulty);
        Assert.assertEquals("Objectives enabled should match", constraints.objectivesEnabled, constraintsFromFile.objectivesEnabled);
        Assert.assertEquals("Noise Height should match", constraints.noiseHeight, constraintsFromFile.noiseHeight);
        Assert.assertEquals("Noise width should match", constraints.noiseWidth, constraintsFromFile.noiseWidth);
        Assert.assertEquals("mapHeight should match", constraints.mapHeight, constraintsFromFile.mapHeight);
        Assert.assertEquals("mapWidth should match", constraints.mapWidth, constraintsFromFile.mapWidth);
        Assert.assertEquals("populationSize should match", constraints.populationSize, constraintsFromFile.populationSize);
        Assert.assertEquals("maxGenerations should match", constraints.maxGenerations, constraintsFromFile.maxGenerations);
    }

    @Test
    public void readInvalidConstraintsTest() {
        GAPopulationGen populationGen = new GAPopulationGen();
        URL resource = this.getClass().getResource("invalidConstraints.xml");
        Assert.assertNotNull(resource);
        boolean exceptionThrown = false;
        try {
            populationGen.readConstraints(new File(resource.getFile()));
        } catch (LevelConstraintsException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("Exception should be thrown", exceptionThrown);
    }

    @Test
    public void incorrectConstraintFilePath() {
        GAPopulationGen populationGen = new GAPopulationGen();
        boolean exceptionThrown = false;
        try {
            populationGen.readConstraints(new File("random/file/path.xml"));
        } catch (LevelConstraintsException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("Exception should have thrown", exceptionThrown);
    }

    @Test
    public void readConstraintsMissingValues() throws LevelConstraintsException {
        GAPopulationGen populationGen = new GAPopulationGen();
        Constraints constraints = new Constraints();
        URL resource = this.getClass().getResource("missingConstraints.xml");
        Assert.assertNotNull(resource);
        populationGen.readConstraints(new File(resource.getFile()));
        Constraints constraintsFromFile = populationGen.getConstraints();
        Assert.assertEquals("Seed should match", constraints.seed, constraintsFromFile.seed);
        Assert.assertEquals("Difficulty should match", constraints.difficulty, constraintsFromFile.difficulty);
        Assert.assertEquals("Objectives enabled should match", constraints.objectivesEnabled, constraintsFromFile.objectivesEnabled);
        Assert.assertEquals("Noise Height should match", constraints.noiseHeight, constraintsFromFile.noiseHeight);
        Assert.assertEquals("Noise width should match", constraints.noiseWidth, constraintsFromFile.noiseWidth);
        Assert.assertEquals("mapHeight should match", constraints.mapHeight, constraintsFromFile.mapHeight);
        Assert.assertEquals("mapWidth should match", constraints.mapWidth, constraintsFromFile.mapWidth);
        Assert.assertEquals("populationSize should match", constraints.populationSize, constraintsFromFile.populationSize);
        Assert.assertEquals("maxGenerations should match", constraints.maxGenerations, constraintsFromFile.maxGenerations);
    }


    private void validateMapCandidate(MapCandidate mapCandidate, Constraints constraints) {
        Assert.assertEquals("Initial fitness should be 0.0", Float.valueOf(0.0f), mapCandidate.fitness);
        Assert.assertTrue("Should have a tile set", mapCandidate.tileSet.size() > 0);
        boolean hasStartPos = false;
        int objectiveCount = 0, enemyCount = 0;
        for (Tile tile : mapCandidate.tileSet) {
            switch (tile.tileState) {
                case START:
                    hasStartPos = true;
                    break;
                case ENEMY:
                    enemyCount++;
                    break;
                case OBJECTIVE:
                    objectiveCount++;
                    break;
            }
        }
        Assert.assertTrue("Should have a start position", hasStartPos);
        Assert.assertTrue("Objective count should be in range", objectiveCount >= 1 && objectiveCount < constraints.difficulty);
        Assert.assertEquals("Enemy count should match", constraints.difficulty * 10, enemyCount);
    }

    private void compareTiles(Tile expected, Tile actual) {
        Assert.assertEquals("Tile X position should match", expected.position[0], actual.position[0]);
        Assert.assertEquals("Tile Y position should match", expected.position[1], actual.position[1]);
        Assert.assertEquals("Tile state should match", expected.tileState, actual.tileState);
        Assert.assertEquals("Tile active flag should match", expected.active, actual.active);
    }


}

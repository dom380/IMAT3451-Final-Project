package dmu.project.levelgen;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Stopwatch;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import dmu.project.levelgen.exceptions.LevelConstraintsException;
import dmu.project.levelgen.exceptions.LevelGenerationException;
import dmu.project.utils.Node;
import dmu.project.utils.PathFinder;
import dmu.project.weather.WeatherResponse;

/**
 * Implementation of the PopulationGenerator interface that uses a Genetic Algorithm.
 * <p>
 * Created by Dom on 30/11/2016.
 */

public class GAPopulationGen implements PopulationGenerator {

    private Constraints mConstraints;
    private LevelGenerator mLevelGen;
    private static Random sRng = new Random();
    private HeightMap mHeightMap;
    private CandidateFactory mCandidateFactory;
    private float mWaterLevel = 0.25f;

    /**
     * Default constructor.
     */
    public GAPopulationGen() {
        mConstraints = new Constraints();
        if (mConstraints.seed != -1) {
            long seed = mConstraints.seed;
            mLevelGen = new PerlinLevelGen(seed, mConstraints.noiseWidth, mConstraints.noiseHeight, 8, 0.5);
            sRng = new Random(seed);
            randomState = new RandomEnum<>(TileState.class, seed);
        } else {
            mLevelGen = new PerlinLevelGen(mConstraints.noiseWidth, mConstraints.noiseHeight, 8, 0.5);
        }
    }

    /**
     * Constructor.
     *
     * @param mConstraints Level generation mConstraints.
     */
    public GAPopulationGen(Constraints mConstraints) {
        this(mConstraints, null);
    }

    /**
     * Constructor
     *
     * @param mConstraints Level generation mConstraints.
     * @param weather     The local weather. Optional.
     */
    public GAPopulationGen(Constraints mConstraints, WeatherResponse weather) {
        if (mConstraints.seed != -1) {
            long seed = mConstraints.seed;
            mLevelGen = new PerlinLevelGen(seed, mConstraints.noiseWidth, mConstraints.noiseHeight, 8, 0.5);
            sRng = new Random(seed);
            randomState = new RandomEnum<>(TileState.class, seed);
        } else {
            mLevelGen = new PerlinLevelGen(mConstraints.noiseWidth, mConstraints.noiseHeight, 8, 0.5);
        }
        this.mConstraints = mConstraints;
        if (weather != null) {
            WeatherResponse.ConditionCode conditionCode = weather.getWeather().get(0).getId();
            if (weather.getMain().getTemp() >= 30.0f) {
                this.mWaterLevel = 0.15f;
            } else if (conditionCode == WeatherResponse.ConditionCode.EXTREME_RAIN || conditionCode == WeatherResponse.ConditionCode.MODERATE_RAIN) {
                this.mWaterLevel = 0.35f;
            }
        } else {
            this.mWaterLevel = 0.25f;
        }
    }

    /**
     * Returns the heightmap for the level being generated.
     *
     * @return heightmap.
     */
    public HeightMap getHeightMap() {
        return mHeightMap;
    }

    /**
     * Genetic Algorithm implementation of the populate method.
     *
     * @return List of Tiles representing the level.
     */
    @Override
    public List<MapCandidate> populate() throws LevelGenerationException {
        int width = mConstraints.mapWidth;
        int height = mConstraints.mapHeight;
        mHeightMap = mLevelGen.generateLevel(width, height, mWaterLevel); //Generate the base terrain.
        if (mConstraints.seed > 0) {
            mCandidateFactory = new CandidateFactory(mHeightMap, width - 2, height - 2, mConstraints.objectivesEnabled, mConstraints.seed);
        } else {
            mCandidateFactory = new CandidateFactory(mHeightMap, width - 2, height - 2, mConstraints.objectivesEnabled);
        }
        List<MapCandidate> population = initPopulation(mConstraints.populationSize, mHeightMap);
        Stopwatch timer = Stopwatch.createStarted();
        int maxGen = mConstraints.getMaxGenerations();
        int sameFitnessCount = 0;
        float avgFitness = 0, initFitness = 0, previousFitness = 0;
        for (int currentGen = 0; currentGen < maxGen; currentGen++) { //For each generation
            avgFitness = testFitness(population);
            if (currentGen == 0) {
                initFitness = avgFitness; //Get the initial population's fitness for debug purposes.
            }
            if (avgFitness > 0.9f) { //If sufficient fitness reached then exit.
                break;
            }
            if (timer.elapsed(TimeUnit.SECONDS) > 25) { //We've been running too long, give up.
                break;
            }
            if (Math.abs(avgFitness - previousFitness) < 0.001) { //Keep track of the number of generations with no meaningful fitness increase.
                sameFitnessCount++;
            }
            if (sameFitnessCount > 10)
                break; //If no fitness improvement for the last x generations exit
            previousFitness = avgFitness;
            population = getNewGen(population); //Perform crossover and mutation to get next generation.
        }
        Collections.sort(population, Collections.reverseOrder(new Comparator<MapCandidate>() {
            @Override
            public int compare(MapCandidate map1, MapCandidate map2) { //Sort population by fitness in descending order
                return map1.fitness.compareTo(map2.fitness);
            }
        }));
        timer.elapsed(TimeUnit.SECONDS);
        timer.stop();
        return population; //Return the last generation, ordered by fitness descending.
    }

    /**
     * Reads the mConstraints from the specified file.
     *
     * @param file Java File object representing the mConstraints xml file.
     * @throws LevelConstraintsException
     */
    @Override
    public void readConstraints(File file) throws LevelConstraintsException {
        Constraints constraints = null;
        try {
            XmlMapper mapper = new XmlMapper();
            constraints = mapper.readValue(IOUtils.toByteArray(new FileInputStream(file)), Constraints.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new LevelConstraintsException("Invalid mConstraints file", e);
        } catch (IOException e) {
            throw new LevelConstraintsException("Constraints file not found", e);
        } finally {
            if (constraints != null) {
                this.mConstraints = constraints;
            }
        }
    }

    /**
     * Setter for level generation mConstraints
     *
     * @param constraints mConstraints to use.
     */
    @Override
    public void setConstraints(Constraints constraints) {
        this.mConstraints = constraints;
    }

    /**
     * Fitness function to test current population's fitness.
     *
     * @param population List of MapCandidates to test.
     * @return Average fitness of population (ignoring maps that fail mConstraints)
     */
    private float testFitness(List<MapCandidate> population) {
        float highestFitness = 0.0f;
        float avgFitness = 0.0f;
        float numOfMaps = population.size();
        int[] startPos = new int[2];
        Stopwatch timer = Stopwatch.createStarted();
        for (MapCandidate map : population) { //For each MapCandidate in population.
            int enemyCount = 0, itemCount = 0, poorPlacedEnemies = 0;
            List<Tile> objectiveBucket = new ArrayList<>();
            List<Tile> obstacleBucket = new ArrayList<>();
            boolean hasStart = false;
            for (Tile tile : map.tileSet) { //Get the start position, count number of enemies and objectives.
                switch (tile.tileState) {
                    case START:
                        startPos = tile.position;
                        hasStart = true;
                        break;
                    case OBJECTIVE:
                        objectiveBucket.add(tile);
                        break;
                    case ENEMY:
                        if (Heuristics.diagonalDist(tile.position, startPos) < 10)
                            poorPlacedEnemies++;
                        enemyCount++;
                        break;
                    case ITEM:
                        itemCount++;
                        break;
                    case OBSTACLE:
                        obstacleBucket.add(tile);
                        break;
                }
            }
            if (!hasStart) { //If we don't have a start position (which shouldn't happen) get rid of this candidate.
                map.fitness = 0.0f;
                numOfMaps--;
                continue;
            }
            Float fitness = 1.0f;
            if (enemyCount > 0) { //If there are no enemies it's a pretty poor map so set fitness to 0 and move on.
                fitness -= 0.05f * poorPlacedEnemies;
            } else {
                map.fitness = 0.0f;
                numOfMaps--;
                continue;
            }
            if (itemCount == 0) { //Mark down maps with no items as that's less interesting.
                fitness -= 0.1f;
            }
            if (obstacleBucket.isEmpty()) { //Mark down maps with no obstacles as that's also less interesting.
                fitness -= 0.1f;
            }
            //Test if there's a path between the start and each objective
            PathFitness pathFitness = testPathFitness(startPos, objectiveBucket, obstacleBucket);
            if (!pathFitness.goodPaths) { //If we can't reach every objective set fitness to 0
                map.fitness = 0.0f;
                numOfMaps--;
                continue;
            }
            fitness += pathFitness.fitness;
            map.fitness = fitness;
            if (map.fitness > highestFitness) {
                highestFitness = map.fitness;
            }
            avgFitness += fitness;
        }
        avgFitness /= numOfMaps;
        timer.elapsed(TimeUnit.SECONDS);
        timer.stop();
        return avgFitness;
    }

    /**
     * Create the next generation in the GA by performing crossover and mutation.
     * Tournament selection is used to pick which candidates crossover.
     *
     * @param currentPop The current population of MapCandidates
     * @return The next generation of MapCandidates.
     */
    private List<MapCandidate> getNewGen(List<MapCandidate> currentPop) {
        List<MapCandidate> newGeneration = new ArrayList<>();
        int tournamentSize = 4;
        int populationSize = currentPop.size();
        MapCandidate currentFittest = null, secondFitness = null;
        while (newGeneration.size() < populationSize) { //While not enough candidates in next gen
            for (int i = 0; i < tournamentSize; ++i) { //Select random candidates for tournament
                MapCandidate mapCandidate = currentPop.get(sRng.nextInt(populationSize));
                if ((currentFittest == null) || (mapCandidate.fitness > currentFittest.fitness)) {
                    secondFitness = currentFittest;
                    currentFittest = mapCandidate;
                } else if (secondFitness == null || mapCandidate.fitness > secondFitness.fitness) {
                    secondFitness = mapCandidate;
                }
            }
            newGeneration.addAll(crossover(currentFittest, secondFitness)); //Crossover the two fittest candidates from tournament.
            currentFittest = null;
            secondFitness = null;
        }
        return newGeneration;
    }

    /**
     * Perform 2 Point crossover.
     *
     * @param parent1 MapCandidate to crossover with.
     * @param parent2 MapCandidate to crossover with.
     * @return Two value List of the child MapCandidates.
     */
    private List<MapCandidate> crossover(MapCandidate parent1, MapCandidate parent2) {
        List<MapCandidate> children = new ArrayList<>();
        int arraySize1 = parent1.tileSet.size(), arraySize2 = parent2.tileSet.size();
        int arraySize = Math.min(arraySize1, arraySize2);
        int crossoverPoint1 = sRng.nextInt(arraySize);
        int crossoverPoint2 = sRng.nextInt(arraySize);
        if (crossoverPoint1 > crossoverPoint2) { //Ensure crossover point 1 is smaller than 2
            int temp = crossoverPoint1;
            crossoverPoint1 = crossoverPoint2;
            crossoverPoint2 = temp;
        }
        List<Tile> child1Tiles = new ArrayList<>();
        List<Tile> child2Tiles = new ArrayList<>();
        for (int i = 0; i < arraySize; ++i) {
            if (i > crossoverPoint1 && i < crossoverPoint2) {
                child1Tiles.add(new Tile(parent2.tileSet.get(i)));
                child2Tiles.add(new Tile(parent1.tileSet.get(i)));
            } else {
                child1Tiles.add(new Tile(parent1.tileSet.get(i)));
                child2Tiles.add(new Tile(parent2.tileSet.get(i)));
            }
        }
        for (int i = arraySize; i < arraySize1; ++i) { //Ensure we don't discard any tiles from the crossover
            child1Tiles.add(parent1.tileSet.get(i));
        }
        for (int i = arraySize; i < arraySize2; ++i) { //Ensure we don't discard any tiles from the crossover
            child2Tiles.add(parent2.tileSet.get(i));
        }
        children.add(mutate(new MapCandidate(child1Tiles)));
        children.add(mutate(new MapCandidate(child2Tiles)));
        return children;
    }

    /**
     * Function that potentially mutates the MapCandidate.
     *
     * @param map MapCandidate to mutate
     * @return Possibly mutated MapCandidate.
     */
    private MapCandidate mutate(MapCandidate map) {
        double chance = sRng.nextDouble();
        if (chance <= 0.33) { // 1 in 3 chance to mutate
            chance = sRng.nextDouble();
            if (chance <= 0.5) { // 50/50 chance to add, or change tile
                int x, y;
                do {
                    x = sRng.nextInt(mConstraints.mapWidth - 2) + 2; //Avoid the edges
                    y = sRng.nextInt(mConstraints.mapHeight - 2) + 2;
                }
                while (mHeightMap.elevation[x][y] < mHeightMap.waterLevel);
                map.tileSet.add(new Tile(randomState.random(), x, y));
            } else {
                TileState randomTileState;
                int randomIndex = sRng.nextInt(map.tileSet.size());
                do {
                    randomTileState = randomState.random();
                }
                while (map.tileSet.get(randomIndex).tileState == randomTileState);
                map.tileSet.get(randomIndex).tileState = randomTileState;
            }

        }
        return map;
    }
    //////////////////////////////
    //Private utility methods
    /////////////////////////////

    /**
     * Utility method to create the specified number of MapCandidates.
     *
     * @param popSize   Number of MapCandidates to create.
     * @param elevation Level Heightmap.
     * @return List of MapCandidates.
     */
    private List<MapCandidate> initPopulation(int popSize, HeightMap elevation) {
        List<MapCandidate> population = new ArrayList<MapCandidate>();
        for (int i = 0; i < popSize; ++i) {
            population.add(mCandidateFactory.createCandidate(mConstraints.getDifficulty()));
        }
        return population;
    }

    private class PathFitness {
        boolean goodPaths;
        float fitness;

        PathFitness(boolean goodPaths, float fitness) {
            this.goodPaths = goodPaths;
            this.fitness = fitness;
        }
    }

    private PathFitness testPathFitness(int[] startPos, List<Tile> objectiveBucket, List<Tile> obstacleBucket) {
        List<Node> obstacles = new ArrayList<>();
        Node startNode = mHeightMap.grid.getNode(startPos[0], startPos[1]);
        float fitness = 0.0f;
        boolean goodPaths = true;
        for (Tile tile : obstacleBucket) { //mark each obstacle tile as not walkable on grid.
            Node node = mHeightMap.grid.getNode(tile.position[0], tile.position[1]);
            node.walkable = false;
            obstacles.add(node);
            if (node.position.equals(startNode)) {
                fitness -= 1.0f;
            }
        }
        //Test start position is reasonable
        int startTiles = mHeightMap.grid.getNeighbours(startNode).size();
        if (startTiles < 8) { //If not all nodes immediately surround the start are free, mark down map.
            fitness -= 0.05f * (8 - startTiles);
        }
        int listSize = objectiveBucket.size();
        for (int i = 0; i < listSize; ++i) { //Test for path to each objective.
            Tile objective = objectiveBucket.get(i);
            if (Heuristics.diagonalDist(objective.position, startPos) < 20 || Heuristics.diagonalDist(objective.position, startPos) > mConstraints.length) {
                fitness -= 0.075f;
            }
            if (!PathFinder.checkPathExists(startPos, objective.position, mHeightMap.grid)) {
                goodPaths = false;
                break;
            }
            for (int j = i + 1; j < listSize; ++j) { //Now check how close the objects are to each other.
                if (Heuristics.diagonalDist(objective.position, objectiveBucket.get(j).position) < 15) {
                    fitness -= 0.05f;
                }
            }

        }
        PathFinder.clearNodes(obstacles);
        return new PathFitness(goodPaths, fitness);
    }

    public Constraints getConstraints() {
        return mConstraints;
    }

    //Package access method for testing.
    void setCandidateFactory(CandidateFactory candidateFactory) {
        this.mCandidateFactory = candidateFactory;
    }

    /**
     * Utility class to randomly select an Enum value.
     *
     * @param <E> Enum to randomly select for.
     */
    private static class RandomEnum<E extends Enum> {

        private static Random RNG = new Random();
        private final E[] values;

        public RandomEnum(Class<E> token) {
            values = token.getEnumConstants();
        }

        public RandomEnum(Class<E> token, long seed) {
            RNG = new Random(seed);
            values = token.getEnumConstants();
        }

        public E random() { //We don't want random start and end points so remove them from the random range
            return values[RNG.nextInt(values.length - 2) + 2];
        }
    }

    private static RandomEnum<TileState> randomState = new RandomEnum<>(TileState.class);
}

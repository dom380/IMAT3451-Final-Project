package dmu.project.levelgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Implementation of the PopulationGenerator interface that uses a Genetic Algorithm.
 *
 * Created by Dom on 30/11/2016.
 */

public class GAPopulationGen implements PopulationGenerator {

    private Constraints constraints;
    private LevelGenerator levelGen;
    private final static Random rng = new Random();
    private int numTiles;
    private double[][] elevation;

    /**
     * Default constructor.
     */
    public GAPopulationGen() {
        levelGen = new PerlinLevelGen();
        constraints = new Constraints(); //todo set up default values
    }

    /**
     * Constructor.
     *
     * @param constraints Level generation constraints.
     */
    public GAPopulationGen(Constraints constraints) {
        levelGen = new PerlinLevelGen();
        this.constraints = constraints;
    }

    /**
     * Returns the heightmap for the level being generated.
     *
     * @return heightmap.
     */
    public double[][] getElevation() {
        return elevation;
    }

    /**
     * Genetic Algorithm implementation of the populate method.
     * @return List of Tiles representing the level.
     */
    @Override
    public List<Tile> populate() {
        int width = constraints.mapWidth;
        int height = constraints.mapHeight;
        int maxTiles = (int) (width * height * constraints.tilePercentage);
        numTiles = rng.nextInt(maxTiles); //Set a random amount of the available tiles to populate.
        elevation = levelGen.generateLevel(512, 512, width, height); //Generate the base terrain.
        List<MapCandidate> population = initPopulation(constraints.populationSize, elevation);
        int maxGen = constraints.getMaxGenerations();
        int sameFitnessCount = 0;
        float highestFitness = 0, initFitness = 0, previousFitness = 0;
        for (int currentGen = 0; currentGen < maxGen; currentGen++) { //For each generation
            highestFitness = testFitness(population);
            if (currentGen == 0) initFitness = highestFitness; //Get the initial population's fitness for debug purposes.
            if (highestFitness > 0.9f) break; //If sufficient fitness reached then exit.
            if (highestFitness == previousFitness) { //Keep track of the number of generations with no fitness increase.
                sameFitnessCount++;
            }
            if (sameFitnessCount > 300) //// TODO: 07/12/2016 Move to constraint or config.
                break; //If no fitness improvement for the last 300 generations exit
            previousFitness = highestFitness;
            population = getNewGen(population); //Perform crossover and mutation to get next generation.
        }
        highestFitness = testFitness(population); //get fitness of final generation.

        Collections.sort(population, Collections.reverseOrder(new Comparator<MapCandidate>() {
            @Override
            public int compare(MapCandidate map1, MapCandidate map2) { //Sort population by fitness in descending order
                return map1.fitness.compareTo(map2.fitness);
            }
        }));

        return population.get(0).tileSet; //Return the most fit map
    }

    @Override
    public void readConstraints(String filePath) {
        // TODO: 30/11/2016 use Jackson deserialiser for this
    }

    /**
     * Setter for level generation constraints
     *
     * @param constraints constraints to use.
     */
    @Override
    public void setConstraints(Constraints constraints) {
        this.constraints = constraints;
    }

    /**
     * Fitness function to test current population's fitness.
     *
     * @param population List of MapCandidates to test.
     * @return Highest fitness found.
     */
    private float testFitness(List<MapCandidate> population) {
        int wellPlacedEnemies;
        float highestFitness = 0;
        int[] startPos = new int[2];
        int[] endPos = new int[2];
        for (MapCandidate map : population) { //For each MapCandidate in population.
            int enemyCount = 0, itemCount = 0, objectiveCount = 0;
            wellPlacedEnemies = 0;
            List<Tile> enemyBucket = new ArrayList<>();
            List<Tile> objectiveBucket = new ArrayList<>();
            for (Tile tile : map.tileSet) { //Get the start and end tile positions, count number of enemies and objectives.
                switch (tile.tileState) {
                    case START:
                        startPos = tile.position;
                        break;
                    case END:
                        endPos = tile.position;
                        break;
                    case OBJECTIVE:
                        objectiveBucket.add(tile);
                        objectiveCount++;
                        break;
                    case ITEM:
                        objectiveBucket.add(tile);
                        itemCount++;
                        break;
                    case ENEMY:
                        enemyBucket.add(tile);
                        enemyCount++;
                        break;
                }
            }
            for (Tile enemy : enemyBucket) { //Count the number of enemies close to an item or objective tile.
                for (Tile objective : objectiveBucket) {
                    if (manhattanDist(enemy.position, objective.position) < 3) {
                        wellPlacedEnemies++;
                        break;
                    }
                }
            }
            //todo get if there is a path to exit/objectives etc.
            float pathLength = (float) Math.sqrt(((endPos[0] - startPos[0]) * (endPos[0] - startPos[0])) + ((endPos[1] - startPos[1]) * (endPos[1] - startPos[1])));
            float pathLengthFitness = Math.max(1 - Math.abs((pathLength - constraints.length) / constraints.length), 0.01f); //0..1 value representing how close to the requested path length.
            float enemyFitness = wellPlacedEnemies / (float) enemyCount; //Percentage of enemies that are placed near objectives.
            float itemFitness = 1 - Math.abs((itemCount - constraints.itemLimit) / (float) constraints.itemLimit); //0..1 value representing how close to the requested item number
            float objectiveFitness = 1 - Math.abs((objectiveCount - constraints.numOfObjectives)
                    / (float) constraints.numOfObjectives); //0..1 value representing how close to the requested objective number.
            map.fitness = (enemyFitness + pathLengthFitness + objectiveFitness + itemFitness) / 4.0f; //Final fitness average of individual checks.
//            float enemyFitness = Math.max(1 - Math.abs((constraints.enemyLimit - enemyCount)/(float)constraints.enemyLimit), 0.01f);
//            float itemFitness = Math.max(1 - Math.abs((constraints.itemLimit - itemCount)/(float)constraints.itemLimit), 0.01f);
//            float ObjectiveFitness = Math.max(1 - Math.abs((constraints.numOfObjectives - objectiveCount) / constraints.numOfObjectives), 0.01f);
//            map.fitness = (enemyFitness + itemFitness + pathLengthFitness + ObjectiveFitness) / 4.0f;
            if (map.fitness > highestFitness) highestFitness = map.fitness;
        }
        return highestFitness;
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
        int tournamentSize = 3;
        int populationSize = currentPop.size();
        MapCandidate fittest = null, secondFitness = null;
        while (newGeneration.size() < populationSize) { //While not enough candidates in next gen
            for (int i = 0; i < tournamentSize; ++i) { //Select random candidates for tournament
                MapCandidate mapCandidate = currentPop.get(rng.nextInt(populationSize));
                if ((fittest == null) || (mapCandidate.fitness > fittest.fitness)) {
                    secondFitness = fittest;
                    fittest = mapCandidate;
                } else if (secondFitness == null || mapCandidate.fitness > secondFitness.fitness) {
                    secondFitness = mapCandidate;
                }
            }
            newGeneration.addAll(crossover(fittest, secondFitness)); //Crossover the two fittest candidates from tournament.
            fittest = null;
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
        int arraySize = Math.min(parent1.tileSet.size(), parent2.tileSet.size());
        int crossoverPoint1 = rng.nextInt(arraySize);
        int crossoverPoint2 = rng.nextInt(arraySize);
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
        //Move out of this function? Should we be mutating in the crossover function?
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
        double chance = rng.nextDouble();
        if (chance <= 0.33) { // 1 in 3 chance to mutate
            chance = rng.nextDouble();
            if (chance <= 0.5) // 50/50 chance to add, or change tile
                map.tileSet.add(new Tile(randomState.random(), rng.nextInt(constraints.mapWidth), rng.nextInt(constraints.mapHeight)));
            else
                map.tileSet.get(rng.nextInt(map.tileSet.size())).tileState = randomState.random();

        }
        return map;
    }
    //////////////////////////////
    //Private utility methods
    /////////////////////////////

    /**
     * Utility method to create the specified number of MapCandidates.
     *
     * @param popSize Number of MapCandidates to create.
     * @param elevation Level Heightmap.
     * @return List of MapCandidates.
     */
    private List<MapCandidate> initPopulation(int popSize, double[][] elevation) {
        List<MapCandidate> population = new ArrayList<MapCandidate>();
        int width = constraints.mapWidth;
        int height = constraints.mapHeight;
        for (int i = 0; i < popSize; ++i) {
            population.add(createCandidate(elevation, width, height));
        }
        return population;
    }

    /**
     * Utility method to create a MapCandidate.
     *
     * @param elevation Heightmap.
     * @param width Width of the level
     * @param height Height of the level.
     * @return MapCandidate.
     */
    private MapCandidate createCandidate(double[][] elevation, int width, int height) {
        List<Tile> tileSet = new ArrayList<>();
        //Add Start and End tile
        tileSet.add(new Tile(TileState.START, rng.nextInt(width), rng.nextInt(height)));
        tileSet.add(new Tile(TileState.END, rng.nextInt(width), rng.nextInt(height)));
        int x, y;
        //Randomly place Objective tiles
        for (int i = 0; i < constraints.numOfObjectives; ++i) {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
            if (elevation[x][y] > 0.25)
                tileSet.add(new Tile(TileState.OBJECTIVE, x, y));
        }
        //Randomly place Enemy tiles
        for (int i = 0; i < constraints.enemyLimit; ++i) {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
            if (elevation[x][y] > 0.25)
                tileSet.add(new Tile(TileState.ENEMY, x, y));
        }
        //Randomly place Item tiles
        for (int i = 0; i < constraints.itemLimit; ++i) {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
            if (elevation[x][y] > 0.25)
                tileSet.add(new Tile(TileState.ITEM, x, y));
        }
        //Get the amount of unused available tiles.
        int numOfObstacle = numTiles - (constraints.itemLimit + constraints.numOfObjectives + constraints.enemyLimit);
        //Randomly place Obstacle tiles.
        for (int i = 0; i < numOfObstacle; ++i) {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
            if (elevation[x][y] > 0.25)
                tileSet.add(new Tile(TileState.OBSTACLE, x, y));
        }
//        for (int i = 0; i < numTiles - 2; ++i) {
//            //// TODO: 02/12/2016 Change random state to use normal instead of uniform distribution
//            tileSet.add(new Tile(randomState.random(), rng.nextInt(width), rng.nextInt(height)));
//        }
        return new MapCandidate(tileSet);
    }

    /**
     * Calculate the Manhattan Distance between two positions.
     *
     * @param pos1 Two value array representing (x,y) coordinates.
     * @param pos2 Two value array representing (x,y) coordinates.
     * @return The Manhattan Distance.
     */
    private int manhattanDist(int[] pos1, int[] pos2) {
        int dX = pos2[0] - pos1[0];
        int dY = pos2[1] - pos1[1];
        return Math.abs(dX) + Math.abs(dY);
    }

    /**
     * Utility class to randomly select an Enum value.
     *
     * @param <E> Enum to randomly select for.
     */
    private static class RandomEnum<E extends Enum> {

        private static final Random RND = new Random();
        private final E[] values;

        public RandomEnum(Class<E> token) {
            values = token.getEnumConstants();
        }

        public E random() { //We don't want random start and end points so remove them from the random range
            return values[RND.nextInt(values.length - 2) + 2];
        }
    }

    private static RandomEnum<TileState> randomState = new RandomEnum<TileState>(TileState.class);
}

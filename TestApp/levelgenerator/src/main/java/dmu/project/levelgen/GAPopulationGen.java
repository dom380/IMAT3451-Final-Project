package dmu.project.levelgen;

import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import dmu.project.utils.Grid;
import dmu.project.utils.LIFOEntry;
import dmu.project.utils.Node;
import dmu.project.utils.Vector2D;

/**
 * Implementation of the PopulationGenerator interface that uses a Genetic Algorithm.
 * <p>
 * Created by Dom on 30/11/2016.
 */

public class GAPopulationGen implements PopulationGenerator {

    private Constraints constraints;
    private LevelGenerator levelGen;
    private final static Random rng = new Random();
    private int numTiles;
    private HeightMap heightMap;

    private static double rootTwo = Math.sqrt(2);

    /**
     * Default constructor.
     */
    public GAPopulationGen() {
        levelGen = new PerlinLevelGen(5, 5, 8, 0.5);
        constraints = new Constraints(); //todo set up default values
    }

    /**
     * Constructor.
     *
     * @param constraints Level generation constraints.
     */
    public GAPopulationGen(Constraints constraints) {
        levelGen = new PerlinLevelGen(constraints.noiseWidth, constraints.noiseHeight, 8, 0.5);
        this.constraints = constraints;
    }

    /**
     * Returns the heightmap for the level being generated.
     *
     * @return heightmap.
     */
    public HeightMap getHeightMap() {
        return heightMap;
    }

    /**
     * Genetic Algorithm implementation of the populate method.
     *
     * @return List of Tiles representing the level.
     */
    @Override
    public List<Tile> populate() {
        int width = constraints.mapWidth;
        int height = constraints.mapHeight;
        heightMap = levelGen.generateLevel(width, height, 0.25f); //Generate the base terrain.
        List<MapCandidate> population = initPopulation(constraints.populationSize, heightMap);
        int maxGen = constraints.getMaxGenerations();
        int sameFitnessCount = 0;
        float highestFitness = 0, initFitness = 0, previousFitness = 0;
        for (int currentGen = 0; currentGen < maxGen; currentGen++) { //For each generation
            highestFitness = testFitness(population);
            if (currentGen == 0)
                initFitness = highestFitness; //Get the initial population's fitness for debug purposes.
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
        //int wellPlacedEnemies;
        int difficulty = constraints.difficulty;
        float highestFitness = 0;
        int[] startPos = new int[2];
        int[] endPos = new int[2];
        Stopwatch timer = Stopwatch.createStarted();
        for (MapCandidate map : population) { //For each MapCandidate in population.
            int enemyCount = 0, itemCount = 0, objectiveCount = 0;
            // wellPlacedEnemies = 0;
            //List<Tile> enemyBucket = new ArrayList<>();
            List<Tile> objectiveBucket = new ArrayList<>();
            List<Tile> obstacleBucket = new ArrayList<>();
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
                        itemCount++;
                        break;
                    case ENEMY:
                      //  enemyBucket.add(tile);
                        enemyCount++;
                        break;
                    case OBSTACLE:
                        obstacleBucket.add(tile);
                        break;
                }
            }

            boolean goodPaths = true;
            List<Node> cleaningList = new ArrayList<>();
            for (Tile objective : objectiveBucket) {
                if (!checkPath(startPos, objective.position, heightMap.grid, obstacleBucket, cleaningList)) {
                    goodPaths = false;
                    break;
                }
            }
            for (Node node : cleaningList) { //Reset visited nodes for next search
                node.fScore = -1;
                node.gScore = -1;
                node.hScore = -1;
            }
            if (!goodPaths) {
                map.fitness = 0.0f;
                continue;
            }
            float usableTiles = map.tileSet.size();
            float mapDifficulty = (float) enemyCount / usableTiles;
            mapDifficulty -= (itemCount / usableTiles) * 5.0f;
            if (constraints.isObjectivesEnabled())
                mapDifficulty += (float) objectiveCount / 10;
            mapDifficulty *= 10;
            map.fitness = 1.0f - Math.abs(difficulty - mapDifficulty) / difficulty;
            if (map.fitness > highestFitness) highestFitness = map.fitness;
        }
        timer.elapsed(TimeUnit.SECONDS);
        timer.stop();
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
     * @param popSize   Number of MapCandidates to create.
     * @param elevation Level Heightmap.
     * @return List of MapCandidates.
     */
    private List<MapCandidate> initPopulation(int popSize, HeightMap elevation) {
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
     * @param heightMap Heightmap.
     * @param width     Width of the level
     * @param height    Height of the level.
     * @return MapCandidate.
     */
    private MapCandidate createCandidate(HeightMap heightMap, int width, int height) {
        List<Tile> tileSet = new ArrayList<>();
        //Add Start and End tile
        int x, y;
        do {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
        } while (heightMap.elevation[x][y] < 0.25);
        tileSet.add(new Tile(TileState.START, x, y));
        do {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
        } while (heightMap.elevation[x][y] < 0.25);
        tileSet.add(new Tile(TileState.END, x, y));
        int freeTiles = rng.nextInt(heightMap.aboveWaterValues / 10 - 2) + 100;
        if (constraints.isObjectivesEnabled()) {
            int numOfObjectives = rng.nextInt(9) + 1;
            for (int i = 0; i < numOfObjectives; ++i) {
                do {
                    x = rng.nextInt(width);
                    y = rng.nextInt(height);
                }
                while (heightMap.elevation[x][y] < 0.25); //keep looking for a value above water level
                tileSet.add(new Tile(TileState.OBJECTIVE, x, y));
                freeTiles--;
            }
        }
        int numOfEntity = rng.nextInt(freeTiles / 2);
        for (int i = 0; i < numOfEntity; ++i) {
            do {
                x = rng.nextInt(width);
                y = rng.nextInt(height);
            }
            while (heightMap.elevation[x][y] < 0.25 || heightMap.elevation[x][y] > 0.65); //keep looking for a value above water level
            tileSet.add(new Tile(TileState.OBSTACLE, x, y));
            //heightMap.grid.getNode(x,y).walkable = false;
            freeTiles--;
        }
        numOfEntity = rng.nextInt(freeTiles / 2);
        for (int i = 0; i < numOfEntity; ++i) {
            do {
                x = rng.nextInt(width);
                y = rng.nextInt(height);
            } while (heightMap.elevation[x][y] < 0.25); //keep looking for a value above water level
            tileSet.add(new Tile(TileState.ENEMY, x, y));
            freeTiles--;
        }
        numOfEntity = rng.nextInt(freeTiles / 2);
        for (int i = 0; i < numOfEntity; ++i) {
            do {
                x = rng.nextInt(width);
                y = rng.nextInt(height);
            } while (heightMap.elevation[x][y] < 0.25); //keep looking for a value above water level
            tileSet.add(new Tile(TileState.ITEM, x, y));
            freeTiles--;
        }
//        int objCount = 0;
//        int maxObj = rng.nextInt(9) + 1;
//        for (int i = 0; i < freeTiles; ++i) {
//            // TODO: 02/12/2016 Change random state to use normal instead of uniform distribution
//            TileState state = randomState.random();
//            if(state == TileState.OBJECTIVE){
//                if(!constraints.isObjectivesEnabled() || objCount >= maxObj)
//                {
//                    state = TileState.OBSTACLE;
//                }
//                else
//                    objCount++;
//            }
//
//            do {
//                    x = rng.nextInt(width);
//                    y = rng.nextInt(height);
//                } while (heightMap.elevation[x][y] < 0.25);
//            tileSet.add(new Tile(state, x, y));
//        }
        return new MapCandidate(tileSet);
    }

    /**
     * Calculate the Diagonal  Distance between two positions.
     *
     * @param pos1 Two value array representing (x,y) coordinates.
     * @param pos2 Two value array representing (x,y) coordinates.
     * @return The Diagonal Distance.
     */
    private double diagonalDist(int[] pos1, int[] pos2) {
        int dX = Math.abs(pos2[0] - pos1[0]);
        int dY = Math.abs(pos2[1] - pos1[1]);
        return (dX) + (rootTwo - 2) * Math.min(dX, dY);

    }

    private double diagonalDist(Vector2D pos1, Vector2D pos2) {
        double dX = Math.abs(pos2.getX() - pos1.getX());
        double dY = Math.abs(pos2.getY() - pos1.getY());
        return (dX) + (rootTwo - 2) * Math.min(dX, dY);

    }

    private double manhatDist(Vector2D pos1, Vector2D pos2) {
        double dX = Math.abs(pos1.getX() - pos2.getX());
        double dY = Math.abs(pos1.getY() - pos2.getY());
        return (dX + dY);
    }

    private double realDist(Vector2D pos1, Vector2D pos2) {
        double dX = Math.abs(pos2.getX() - pos1.getX());
        double dY = Math.abs(pos2.getY() - pos1.getY());
        return Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
    }

    private double realDist(int[] pos1, int[] pos2) {
        int dX = Math.abs(pos2[0] - pos1[0]);
        int dY = Math.abs(pos2[1] - pos1[1]);
        return Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
    }

    private boolean checkPath(int[] start, int[] goal, Grid grid, List<Tile> tileSet, List<Node> cleaningList) {
        boolean goodPath = false;
        LIFOEntry.resetCount();
//        final Map<Vector2D, Vector2D> cameFrom = new HashMap<>();
        final Queue<LIFOEntry<Node>> frontier = new PriorityQueue<>();
        Node startNode = grid.getNode(start[0], start[1]);
        Node goalNode = grid.getNode(goal[0], goal[1]);
        final Set<Node> closedSet = new HashSet<>();
        //Stopwatch stopwatch = Stopwatch.createStarted();
        //A* Search

//        frontier.add(new LIFOEntry<Node>(startNode));
//        startNode.fScore = diagonalDist(start, goal);
//        while (!frontier.isEmpty()) {
//            LIFOEntry entry = frontier.poll();
//            Node current = (Node) entry.getEntry();
//            if (current.position.equals(goalNode.position)) {
//                goodPath = true;
//                break;
//            }
//            closedSet.add(current);
//            List<Node> neighbours = grid.getNeighbours(current, tileSet);
//            for (Node neighbour : neighbours) {
//                if (closedSet.contains(neighbour)) {
//                    continue;
//                }
//                double gScore = current.gScore != -1 ? (current.gScore + realDist(current.position, neighbour.position)) : (realDist(current.position, neighbour.position));
//                if (gScore >= neighbour.gScore && neighbour.gScore != -1)
//                    continue; //This isn't a better path than one found before.
//                neighbour.updateScore(gScore, diagonalDist(neighbour.position, goalNode.position), current);
//                if (!frontier.contains(neighbour)) {
//                    frontier.add(new LIFOEntry<Node>(neighbour));
//                }
//                cameFrom.put(neighbour.position, current.position);
//
//            }
//            if (frontier.isEmpty()) {
//                goodPath = false;
//                break; //No path found
//            }
//        }
//        long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
//        stopwatch.stop();
//        goodPath = false;
//        LIFOEntry.resetCount();
//        closedSet.clear();
//        frontier.clear();
//        cameFrom.clear();
//        grid.reset();
//        stopwatch.reset();
//        stopwatch.start();
        frontier.add(new LIFOEntry<Node>(startNode));
        cleaningList.add(startNode);
        boolean moveDiag = grid.isMoveDiag();
        //JPS
        while (!frontier.isEmpty()) {
            LIFOEntry<Node> entry = frontier.poll();
            Node current = entry.getEntry();
            if (current.position.equals(goalNode.position)) {
                goodPath = true;
                break;
            }
            closedSet.add(current);
            List<Node> neighbours = grid.getNeighboursPrune(current, tileSet);
            for (Node neighbour : neighbours) {
                Node jumpNode = jump(grid, neighbour.position, current, goalNode, tileSet, moveDiag);
                if (jumpNode != null && !closedSet.contains(jumpNode)) {
                    double gScore = current.gScore != -1 ? (current.gScore + realDist(current.position, neighbour.position)) : (realDist(current.position, neighbour.position));
                    if (gScore >= neighbour.gScore && neighbour.gScore != -1)
                        continue; //This isn't a better path than on found before.
                    cleaningList.add(jumpNode);
                    if (moveDiag) {
                        jumpNode.updateScore(gScore, diagonalDist(jumpNode.position, goalNode.position), null);
                    } else {
                        jumpNode.updateScore(gScore, manhatDist(jumpNode.position, goalNode.position), current);
                    }
                    LIFOEntry<Node> jumpEntry =  new LIFOEntry<Node>(jumpNode);
                    if (!frontier.contains(jumpEntry)) {
                        frontier.add(jumpEntry);
                    }else{
                        int debugcheck =1;
                        debugcheck++;
                    }
//                    cameFrom.put(jumpNode.position, current.position);
                }
            }
        }
//        //BFS of map to check if path exists between two points.
//        while (!frontier.isEmpty()) {
//            LIFOEntry entry = frontier.poll();
//            Node current = (Node) entry.getEntry();
//            if (current.position.equals(goalNode.position)) {
//                goodPath = true;
//                break;
//            }
//
//            List<Node> neighbours = grid.getNeighbours(current, tileSet);
//            for (Node neighbour : neighbours) {
//                if (!cameFrom.containsKey(neighbour.position)) {
//                    frontier.add(new LIFOEntry<Node>(neighbour));
//                    cameFrom.put(neighbour.position, current.position);
//                }
//            }
//        }
//        if(stopwatch.isRunning()) {
//            elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
//            stopwatch.stop();
//        }
        return goodPath;
    }

    private Node jump(Grid grid, Vector2D nodePos, Node parent, Node goal, List<Tile> tileSet, boolean moveDiag) {
        int x = nodePos.getX().intValue(), y = nodePos.getY().intValue();
        if (!grid.walkable(x, y, tileSet)) { //If space isn't walkable return null
            return null;
        }
        Node node = grid.getNode(x, y);
        node.parent = parent;
        if (node.position.equals(goal.position)) { //If end point, return it. Search over.
            return node;
        }
        //get the normalized direction of travel
        int dx = (int) ((node.position.getX() - parent.position.getX()) / Math.max(Math.abs(node.position.getX() - parent.position.getX()), 1));
        int dy = (int) ((node.position.getY() - parent.position.getY()) / Math.max(Math.abs(node.position.getY() - parent.position.getY()), 1));

        if (dx != 0 && dy != 0) { //If x and y have changed we're moving diagonally. Check for forced neighbours
            if ((grid.walkable(x - dx, y + dy, tileSet) && !grid.walkable(x - dx, y, tileSet)) || //we are moving diagonally, don't check the parent, or our next diagonal step, but the other diagonals
                    (grid.walkable(x + dx, y - dy, tileSet) && !grid.walkable(x, y - dy, tileSet))) {  //if we find a forced neighbor here, we are on a jump point, and we return the current position
                return node;
            }
        } else { //Check horizontal and vertical
            if (dx != 0) { //Moving in X
                if (moveDiag) { //And we allow diagonal movement
                    if ((grid.walkable(x + dx, y + 1, tileSet) && !grid.walkable(x, y + 1, tileSet)) || //check the diagonal nodes for forced neighbours
                            (grid.walkable(x + dx, y - 1, tileSet) && !grid.walkable(x, y - 1, tileSet))) {
                        return node;
                    }
                } else { //Diagonal moves not allowed.
                    if (grid.walkable(x + 1, y, tileSet) || grid.walkable(x - 1, y, tileSet)) { // if left or right free
                        return node;                                                            // return node as we're on a jump point
                    }
                }
            } else { //Moving in Y
                if (moveDiag) { //If diagonal movement allowed.
                    if ((grid.walkable(x + 1, y + dy, tileSet) && !grid.walkable(x + 1, y, tileSet)) ||
                            (grid.walkable(x - 1, y + dy, tileSet) && !grid.walkable(x - 1, y, tileSet))) {
                        return node;
                    }
                } else {
                    if (grid.walkable(x, y + 1, tileSet) || grid.walkable(x, y - 1, tileSet)) {
                        return node;
                    }
                }
            }
        }
        if (dx != 0 && dy != 0) { //Moving diagonally so have to check for vertical and horizontal jump points
            Node jumpHoz = jump(grid, new Vector2D(x + dx, y), node, goal, tileSet, moveDiag);
            if (jumpHoz != null) {
                return node;
            }
            Node jumpVert = jump(grid, new Vector2D(x, y + dy), node, goal, tileSet, moveDiag);
            if (jumpVert != null) {
                return node;
            }

        }
        if (moveDiag) {
            if (grid.walkable(x + dx, y, tileSet) || grid.walkable(x, y + dy, tileSet)) {
                return jump(grid, new Vector2D(x + dx, y + dy), node, goal, tileSet, true); //Haven't found a forced neighbour or goal yet, jump to next diagonal in current direction.
            } else { //Blocked from going diagonally
                return null;
            }
        }
        return null;//Couldn't jump anywhere.
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

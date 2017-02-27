package dmu.project.levelgen;

import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
    private static Random rng = new Random();
    private int numTiles;
    private HeightMap heightMap;
    private CandidateFactory candidateFactory;

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
        if (constraints.seed != -1) {
            long seed = constraints.seed;
            levelGen = new PerlinLevelGen(seed, constraints.noiseWidth, constraints.noiseHeight, 8, 0.5);
            rng = new Random(seed);
            randomState = new RandomEnum<>(TileState.class, seed);
        } else {
            levelGen = new PerlinLevelGen(constraints.noiseWidth, constraints.noiseHeight, 8, 0.5);
        }
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
    public List<MapCandidate> populate() {
        int width = constraints.mapWidth;
        int height = constraints.mapHeight;
        heightMap = levelGen.generateLevel(width, height, 0.25); //Generate the base terrain.
        if (constraints.seed > 0) {
            candidateFactory = new CandidateFactory(heightMap, width - 2, height - 2, constraints.objectivesEnabled, constraints.seed);
        } else {
            candidateFactory = new CandidateFactory(heightMap, width - 2, height - 2, constraints.objectivesEnabled);
        }
        List<MapCandidate> population = initPopulation(constraints.populationSize, heightMap);
        Stopwatch timer = Stopwatch.createStarted();
        int maxGen = constraints.getMaxGenerations();
        int sameFitnessCount = 0;
        float avgFitness = 0, initFitness = 0, previousFitness = 0;
        boolean reachedMaxFit = false;
        for (int currentGen = 0; currentGen < maxGen; currentGen++) { //For each generation
            avgFitness = testFitness(population);
            if (currentGen == 0) {
                initFitness = avgFitness; //Get the initial population's fitness for debug purposes.
            }
            if (avgFitness > 0.9f) { //If sufficient fitness reached then exit.
                reachedMaxFit = true;
                break;
            }
            if(timer.elapsed(TimeUnit.SECONDS) > 30){ //We've been running too long, give up. //TODO make configurable?
                reachedMaxFit = false;
                break;
            }
            if (Math.abs(avgFitness - previousFitness) < 0.001) { //Keep track of the number of generations with no meaningful fitness increase.
                sameFitnessCount++;
            }
            if (sameFitnessCount > 10) // TODO: 07/12/2016 Move to constraint or config.
                break; //If no fitness improvement for the last x generations exit
            previousFitness = avgFitness;
            population = getNewGen(population); //Perform crossover and mutation to get next generation.
        }
        if (!reachedMaxFit)
            avgFitness = testFitness(population); //get fitness of final generation.

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
     * @return Average fitness of population (ignoring maps that fail constraints)
     */
    private float testFitness(List<MapCandidate> population) {
        //int wellPlacedEnemies;
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
            /*TODO - New fitness test! NEED TUNING
            * Check num of enemies near start pos - Too many too close lower fitness
            * Check dist to objectives - too close lower, too far low? (maybe depend of difficulty?)
            * maybe enemy placement?
            */
            if (!hasStart) { //If we don't have a start position (which shouldn't happen) get rid of this candidate.
                map.fitness = 0.0f;
                numOfMaps--;
                continue;
            }
            Float fitness = 1.0f;
            if (enemyCount > 0) { //If there are no enemies it's a pretty poor map so set fitness to 0 and move on.
                fitness -= 0.05f * poorPlacedEnemies;//((float) poorPlacedEnemies / (float) enemyCount) * 2.0f;
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
            if (map.fitness > highestFitness) highestFitness = map.fitness;
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
                MapCandidate mapCandidate = currentPop.get(rng.nextInt(populationSize));
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
        for (int i = arraySize; i < arraySize1; ++i) { //Ensure we don't discard any tiles from the crossover
            child1Tiles.add(parent1.tileSet.get(i));
        }
        for (int i = arraySize; i < arraySize2; ++i) { //Ensure we don't discard any tiles from the crossover
            child2Tiles.add(parent2.tileSet.get(i));
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
            if (chance <= 0.5) { // 50/50 chance to add, or change tile
                int x, y;
                do {
                    x = rng.nextInt(constraints.mapWidth - 2) + 2; //Avoid the edges
                    y = rng.nextInt(constraints.mapHeight - 2) + 2;
                }
                while (heightMap.elevation[x][y] < heightMap.waterLevel);
                map.tileSet.add(new Tile(randomState.random(), x, y));
            } else
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
        for (int i = 0; i < popSize; ++i) {
            population.add(candidateFactory.createCandidate(constraints.getDifficulty()));
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
        List<Node> cleaningList = new ArrayList<>();
        List<Node> obstacles = new ArrayList<>();
        Node startNode = heightMap.grid.getNode(startPos[0], startPos[1]);
        float fitness = 0.0f;
        boolean goodPaths = true;
        for (Tile tile : obstacleBucket) { //mark each obstacle tile as not walkable on grid.
            Node node = heightMap.grid.getNode(tile.position[0], tile.position[1]);
            node.walkable = false;
            obstacles.add(node);
            if (node.position.equals(startNode)) {
                fitness -= 1.0f;
            }
        }
        //Test start position is reasonable
        int startTiles = heightMap.grid.getNeighbours(startNode).size();
        if (startTiles < 8) { //If not all nodes immediately surround the start are free, mark down map.
            fitness -= 0.05f * (8 - startTiles);
        }
        int listSize = objectiveBucket.size();
        for (int i = 0; i < listSize; ++i) { //Test for path to each objective.
            Tile objective = objectiveBucket.get(i);
            if (Heuristics.diagonalDist(objective.position, startPos) < 20 || Heuristics.diagonalDist(objective.position, startPos) > 300) { //TODO tune this because it's probs garbage
                fitness -= 0.075f;
            }
            if (!checkPath(startPos, objective.position, heightMap.grid, cleaningList)) {
                goodPaths = false;
                break;
            }
            clearNodes(cleaningList); //Reset node visited in search and empty the list.
            for (int j = i + 1; j < listSize; ++j) { //Now check how close the objects are to each other.
                if (Heuristics.diagonalDist(objective.position, objectiveBucket.get(j).position) < 15) {
                    fitness -= 0.05f;
                }
            }

        }
        clearNodes(cleaningList);
        clearNodes(obstacles);
        return new PathFitness(goodPaths, fitness);
    }

    private boolean checkPath(int[] start, int[] goal, Grid grid, List<Node> cleaningList) {
        boolean goodPath = false;
        LIFOEntry.resetCount();
//        final Map<Vector2D, Vector2D> cameFrom = new HashMap<>();
        final Queue<LIFOEntry<Node>> frontier = new PriorityQueue<>();
        Node startNode = grid.getNode(start[0], start[1]);
        Node goalNode = grid.getNode(goal[0], goal[1]);
        final Set<Node> closedSet = new HashSet<>();
        Stopwatch stopwatch = Stopwatch.createStarted();
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
//            List<Vector2D> neighbours = grid.getNeighbours(current, tileSet);
//            for (Vector2D neighbourPos : neighbours) {
//                Node neighbour = grid.getNode(neighbourPos.getX().intValue(),neighbourPos.getY().intValue());
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
//                //cameFrom.put(neighbour.position, current.position);
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
//        grid.reset();
//        stopwatch.reset();
        //stopwatch.start();
        frontier.add(new LIFOEntry<Node>(startNode));
        cleaningList.add(startNode);
        boolean moveDiag = grid.isMoveDiag();
        //JPS
        while (!frontier.isEmpty()) {
            LIFOEntry<Node> entry = frontier.poll();
            Node current = entry.getEntry();
            if (current.position.equals(goalNode.position)) {
                stopwatch.stop();
                goodPath = true;
                break;
            }
            closedSet.add(current);
            List<Vector2D> neighbours = grid.getNeighboursPrune(current);
            for (Vector2D neighbourPos : neighbours) {
                Node jumpNode = jump(grid, neighbourPos, current, goalNode, moveDiag);
                if (jumpNode != null && !closedSet.contains(jumpNode)) {
                    Node neighbour = grid.getNode(neighbourPos.getX().intValue(), neighbourPos.getY().intValue());
                    double gScore = current.gScore != -1 ? (current.gScore + Heuristics.realDist(current.position, neighbour.position)) : (Heuristics.realDist(current.position, neighbour.position));
                    if (gScore >= neighbour.gScore && neighbour.gScore != -1)
                        continue; //This isn't a better path than on found before.
                    cleaningList.add(jumpNode);
                    if (moveDiag) {
                        jumpNode.updateScore(gScore, Heuristics.diagonalDist(jumpNode.position, goalNode.position), null);
                    } else {
                        jumpNode.updateScore(gScore, Heuristics.manhatDist(jumpNode.position, goalNode.position), current);
                    }
                    LIFOEntry<Node> jumpEntry = new LIFOEntry<Node>(jumpNode);
                    if (!frontier.contains(jumpEntry))
                        frontier.add(jumpEntry);
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
        if (stopwatch.isRunning()) {
            long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            stopwatch.stop();
        }
        return goodPath;
    }

    private Node jump(Grid grid, Vector2D nodePos, Node parent, Node goal, boolean moveDiag) {
        int x = nodePos.getX().intValue(), y = nodePos.getY().intValue();
        if (!grid.walkable(x, y)) { //If space isn't walkable return null
            return null;
        }
        Node node = grid.getNode(x, y);
        node.parent = parent;
        if (node.position.equals(goal.position)) { //If end point, return it. Search over.
            return node;
        }
        //get the normalized direction of travel
        int px = parent.position.getX().intValue(), py = parent.position.getY().intValue();
        int dx = (x - px) / Math.max(Math.abs(x - px), 1);
        int dy = (y - py) / Math.max(Math.abs(y - py), 1);

        if (dx != 0 && dy != 0) { //If x and y have changed we're moving diagonally. Check for forced neighbours
            if ((grid.walkable(x - dx, y + dy) && !grid.walkable(x - dx, y)) || //we are moving diagonally, don't check the parent, or our next diagonal step, but the other diagonals
                    (grid.walkable(x + dx, y - dy) && !grid.walkable(x, y - dy))) {  //if we find a forced neighbor here, we are on a jump point, and we return the current position
                return node;
            }
            //Moving diagonally so have to check for vertical and horizontal jump points
            if (jump(grid, new Vector2D(x + dx, y), node, goal, moveDiag) != null || jump(grid, new Vector2D(x, y + dy), node, goal, moveDiag) != null) {
                return node;
            }
        } else { //Check horizontal and vertical
            if (dx != 0) { //Moving in X
                if (moveDiag) { //And we allow diagonal movement
                    if ((grid.walkable(x + dx, y + 1) && !grid.walkable(x, y + 1)) || //check the diagonal nodes for forced neighbours
                            (grid.walkable(x + dx, y - 1) && !grid.walkable(x, y - 1))) {
                        return node;
                    }
                } else { //Diagonal moves not allowed.
                    if (grid.walkable(x + 1, y) || grid.walkable(x - 1, y)) { // if left or right free
                        return node;                                                            // return node as we're on a jump point
                    }
                }
            } else { //Moving in Y
                if (moveDiag) { //If diagonal movement allowed.
                    if ((grid.walkable(x + 1, y + dy) && !grid.walkable(x + 1, y)) ||
                            (grid.walkable(x - 1, y + dy) && !grid.walkable(x - 1, y))) {
                        return node;
                    }
                } else {
                    if (grid.walkable(x, y + 1) || grid.walkable(x, y - 1)) {
                        return node;
                    }
                }
            }
        }
        if (moveDiag) {
            if (grid.walkable(x + dx, y) || grid.walkable(x, y + dy)) {
                return jump(grid, new Vector2D(x + dx, y + dy), node, goal, true); //Haven't found a forced neighbour or goal yet, jump to next diagonal in current direction.
            } else { //Blocked from going diagonally
                return null;
            }
        }
        return null;//Couldn't jump anywhere.
    }

    void clearNodes(List<Node> nodes) {
        if (nodes.isEmpty()) return;
        for (Node node : nodes) {
            node.fScore = -1;
            node.gScore = -1;
            node.hScore = -1;
            node.parent = null;
            node.walkable = true;
        }
        nodes.clear();
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

package dmu.project.levelgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Dom on 30/11/2016.
 */

public class GAPopulationGen implements PopulationGenerator {

    private Constraints constraints;
    private LevelGenerator levelGen;
    private final static Random rng = new Random();

    public GAPopulationGen() {
        levelGen = new PerlinLevelGen();
        constraints = new Constraints(); //todo set up default values
    }

    public GAPopulationGen(Constraints constraints) {
        levelGen = new PerlinLevelGen();
        this.constraints = constraints;
    }

    @Override
    public Tile[][] populate() {
        int width = constraints.mapWidth;
        int height = constraints.mapHeight;
        double[][] elevation = levelGen.generateLevel(512, 512, width, height);
        List<MapCandidate> population = initPopulation(20, elevation);
        int maxGen = constraints.getMaxGenerations();
        float avgFittness = 0, initFittness = 0;
        for (int currentGen = 0; currentGen < maxGen; currentGen++) {
            avgFittness = testFitness(population);
            if(currentGen == 0) initFittness = avgFittness;
            population = getNewGen(population);
        }
        //get fittness of final gen
        avgFittness = testFitness(population);
        //Sort population by fitness in descending order
        Collections.sort(population, Collections.reverseOrder((p1, p2) -> p1.fitness.compareTo(p2.fitness)));
        //Return fittest map
        return population.get(0).tileSet;
    }

    @Override
    public void readConstraints(String filePath) {
        // TODO: 30/11/2016 use Jackson deserialiser for this
    }

    @Override
    public void setConstraints(Constraints constraints) {
        this.constraints = constraints;
    }

    private float testFitness(List<MapCandidate> population) {
        int width = constraints.mapWidth, height = constraints.mapHeight;
        float sumFittness = 0;
        float mapSize = (width * height);
        int[] startPos = new int[2];
        int[] endPos = new int[2];
        for (MapCandidate map : population) {
            int enemyCount = 0, objectiveCount = 0, itemCount = 0;
            boolean hasStart = false, hasEnd = false;
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    Tile currentTile = map.tileSet[x][y];
                    switch (currentTile.tileState) {
                        case START:
                            startPos[0] = x;
                            startPos[1] = y;
                            hasStart = true;
                            break;
                        case END:
                            endPos[0] = x;
                            endPos[1] = y;
                            hasEnd = true;
                            break;
                        case OBJECTIVE:
                            objectiveCount++;
                            break;
                        case ITEM:
                            itemCount++;
                            break;
                        case ENEMY:
                            enemyCount++;
                            break;
                    }
                }
            }//end for
            if (!hasEnd || !hasStart) { //should probably change the init code to ensure this constraint
                map.fitness = 0.f;
                break;
            }
            //todo get if there is a path to exit/objectives etc.
            float enemyDensity = enemyCount / mapSize;
            float itemDensity = itemCount / mapSize;
            float pathLength = (float) Math.sqrt(((endPos[0] - startPos[0]) * (endPos[0] - startPos[0])) + ((endPos[1] - startPos[1]) * (endPos[1] - startPos[1])));
            float enemyFitness = Math.max(1 - Math.abs(constraints.enemyDensity - enemyDensity), 0.01f);
            float itemFitness = Math.max(1 - Math.abs(constraints.itemDensity - itemDensity), 0.01f);
            float pathLengthFitness = Math.max(1 - Math.abs((pathLength - constraints.length) / constraints.length), 0.01f);
            float ObjectiveFitness = Math.max(1 - Math.abs((constraints.numOfObjectives - objectiveCount) / constraints.numOfObjectives), 0.01f);
            map.fitness = (enemyFitness + itemFitness + pathLengthFitness + ObjectiveFitness) / 4.0f;
            sumFittness += map.fitness;
        }
        return sumFittness/(float)population.size();
    }

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
            newGeneration.addAll(crossover(fittest, secondFitness));
            fittest = null;
            secondFitness = null;
        }
        return newGeneration;
    }

    // TODO: 30/11/2016 Optimise the crap out of this because it looks super slow
    private List<MapCandidate> crossover(MapCandidate parent1, MapCandidate parent2) {
        int width = constraints.mapWidth, height = constraints.mapHeight;
        List<MapCandidate> children = new ArrayList<>();
        int x = rng.nextInt(constraints.mapWidth - 1) + 1;
        int y = rng.nextInt(constraints.mapHeight - 1) + 1;
        Tile[][] child1Tiles = new Tile[width][height];
        Tile[][] child2Tiles = new Tile[width][height];
        //Copy first 'half' of parents to children
        for (int i = 0; i < x; ++i) { // Copy half from parent 1 to child 1
            if (i != x - 1) { //If not last row
                System.arraycopy(parent1.tileSet[i], 0, child1Tiles[i], 0, parent1.tileSet[i].length);
            } else {
                System.arraycopy(parent1.tileSet[i], 0, child1Tiles[i], 0, y);
            }
        }
        for (int i = 0; i < x; ++i) { // Copy half from parent 2 to child 2
            if (i != x - 1) { //If not last row
                System.arraycopy(parent2.tileSet[i], 0, child2Tiles[i], 0, parent2.tileSet[i].length);
            } else {
                System.arraycopy(parent2.tileSet[i], 0, child2Tiles[i], 0, y);
            }
        }
        //Copy second 'half' of parents to children
        for (int i = x; i < width; ++i) { // Copy half from parent 2 to child 1
            if (i == x) { //If first row of this half, fill out missing elements of the previous row
                System.arraycopy(parent2.tileSet[i - 1], y, child1Tiles[i - 1], y, parent2.tileSet[i - 1].length - y);
            }
            System.arraycopy(parent2.tileSet[i], 0, child1Tiles[i], 0, parent2.tileSet[i].length);

        }
        for (int i = x; i < width; ++i) { // Copy half from parent 1 to child 2
            if (i == x) { //If first row of this half, copy from crossover point
                System.arraycopy(parent1.tileSet[i - 1], y, child2Tiles[i - 1], y, parent1.tileSet[i - 1].length - y);
            }
            System.arraycopy(parent1.tileSet[i], 0, child2Tiles[i], 0, parent1.tileSet[i].length);

        }
        children.add(new MapCandidate(child1Tiles));
        children.add(new MapCandidate(child2Tiles));
        return children;
    }

    //Private utility methods
    private List<MapCandidate> initPopulation(int popSize, double[][] elevation) {
        List<MapCandidate> population = new ArrayList<MapCandidate>();
        int width = constraints.mapWidth;
        int height = constraints.mapHeight;
        for (int i = 0; i < popSize; ++i) {
            population.add(createCandidate(elevation, width, height));
        }
        return population;
    }

    private MapCandidate createCandidate(double[][] elevation, int width, int height) {
        Tile[][] tileSet = new Tile[width][height];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                tileSet[x][y] = new Tile(randomState.random(), elevation[x][y]);
            }
        }
        return new MapCandidate(tileSet);
    }

    private static class RandomEnum<E extends Enum> {

        private static final Random RND = new Random();
        private final E[] values;

        public RandomEnum(Class<E> token) {
            values = token.getEnumConstants();
        }

        public E random() {
            return values[RND.nextInt(values.length)];
        }
    }

    private static RandomEnum<TileState> randomState = new RandomEnum<TileState>(TileState.class);
}

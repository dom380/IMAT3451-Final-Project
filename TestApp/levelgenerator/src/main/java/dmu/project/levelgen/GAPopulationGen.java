package dmu.project.levelgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Created by Dom on 30/11/2016.
 */

public class GAPopulationGen implements PopulationGenerator {

    private Constraints constraints;
    private LevelGenerator levelGen;
    private final static Random rng = new Random();
    private int numTiles;
    private double[][] elevation;

    public GAPopulationGen() {
        levelGen = new PerlinLevelGen();
        constraints = new Constraints(); //todo set up default values
    }

    public GAPopulationGen(Constraints constraints) {
        levelGen = new PerlinLevelGen();
        this.constraints = constraints;
    }

    public double[][] getElevation() {
        return elevation;
    }

    @Override
    public List<Tile> populate(Progress progress) {
        int width = constraints.mapWidth;
        int height = constraints.mapHeight;
        int maxTiles = (int) (width * height * constraints.tilePercentage);
        numTiles = rng.nextInt(maxTiles);
//        numTiles = (int) (width*height*constraints.tilePercentage);
        elevation = levelGen.generateLevel(512, 512, width, height);
        List<MapCandidate> population = initPopulation(constraints.populationSize, elevation);
        int maxGen = constraints.getMaxGenerations();
        int sameFitnessCount = 0;
        float highestFitness = 0, initFitness = 0, previousFitness = 0;
        for (int currentGen = 0; currentGen < maxGen; currentGen++) {
            highestFitness = testFitness(population);
            if (currentGen == 0) initFitness = highestFitness;
            if (highestFitness > 0.9f) break;
            if (highestFitness == previousFitness) {
                sameFitnessCount++;
            }
            if (sameFitnessCount > 300)
                break; //If no fitness improvement for the last 100 generations exit
            previousFitness = highestFitness;
            population = getNewGen(population);
            progress.progress = 100 * (currentGen/(float)maxGen);
        }
        //get fitness of final gen
        highestFitness = testFitness(population);
        //Sort population by fitness in descending order
        Collections.sort(population, Collections.reverseOrder(new Comparator<MapCandidate>() {
            @Override
            public int compare(MapCandidate map1, MapCandidate map2) {
                return map1.fitness.compareTo(map2.fitness);
            }
        }));
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
        /*
            DOING FITNESS ALL WRONG!!!
            CHANGE TO USE CONSTRAINTS AS ACTUAL CONSTRAINTS!!
            FITNESS BASED ON GROUPING OF ELEMENTS / GAMEPLAY
            ALSO MAYBE CHANGE TO USE DIFFICULTY AS A TARGET AND WORKOUT FITNESS BASED ON THAT

            for each enemy
                for each item or objective
                    if enemy within range of obj
                        wellPlacedEnemies++
                        break
            enemy fitness  = wellPlacedEnemies/enemyCount (percentage of enemies near obj
            do the same with OBSTACLE near OBSTACLE?
            start/end fitness stay the same
            average them all together at end. Should reward maps with enemies near items and objectives
         */
        int width = constraints.mapWidth, height = constraints.mapHeight, wellPlacedEnemies = 0;
        float highestFitness = 0;
        int[] startPos = new int[2];
        int[] endPos = new int[2];
        for (MapCandidate map : population) {
            int enemyCount = 0, itemCount = 0, objectiveCount = 0;
            wellPlacedEnemies = 0;
            List<Tile> enemyBucket = new ArrayList<>();
            List<Tile> objectiveBucket = new ArrayList<>();
            for (Tile tile : map.tileSet) {
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
            for (Tile enemy : enemyBucket) {
                for (Tile objective : objectiveBucket) {
                    if (manhattanDist(enemy.position, objective.position) < 3) {
                        wellPlacedEnemies++;
                        break;
                    }
                }
            }
            //todo get if there is a path to exit/objectives etc.
            float pathLength = (float) Math.sqrt(((endPos[0] - startPos[0]) * (endPos[0] - startPos[0])) + ((endPos[1] - startPos[1]) * (endPos[1] - startPos[1])));
            float pathLengthFitness = Math.max(1 - Math.abs((pathLength - constraints.length) / constraints.length), 0.01f);
            float enemyFitness = wellPlacedEnemies / (float) enemyCount;
            float itemFitness = 1 - Math.abs((itemCount - constraints.itemLimit) / (float) constraints.itemLimit);
            float objectiveFitness = 1 - Math.abs((objectiveCount - constraints.numOfObjectives) / (float) constraints.numOfObjectives); // objectiveCount / (float) constraints.numOfObjectives;
            map.fitness = (enemyFitness + pathLengthFitness + objectiveFitness + itemFitness) / 4.0f;
//            float enemyFitness = Math.max(1 - Math.abs((constraints.enemyLimit - enemyCount)/(float)constraints.enemyLimit), 0.01f);
//            float itemFitness = Math.max(1 - Math.abs((constraints.itemLimit - itemCount)/(float)constraints.itemLimit), 0.01f);
//            float ObjectiveFitness = Math.max(1 - Math.abs((constraints.numOfObjectives - objectiveCount) / constraints.numOfObjectives), 0.01f);
//            map.fitness = (enemyFitness + itemFitness + pathLengthFitness + ObjectiveFitness) / 4.0f;
            if (map.fitness > highestFitness) highestFitness = map.fitness;
        }
        return highestFitness;
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

    private List<MapCandidate> crossover(MapCandidate parent1, MapCandidate parent2) {
        List<MapCandidate> children = new ArrayList<>();
        int arraySize = Math.min(parent1.tileSet.size(), parent2.tileSet.size());
        int crossoverPoint1 = rng.nextInt(arraySize);
        int crossoverPoint2 = rng.nextInt(arraySize);
        if (crossoverPoint1 < crossoverPoint2) {
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
        children.add(mutate(new MapCandidate(child1Tiles)));
        children.add(mutate(new MapCandidate(child2Tiles)));
        return children;
    }

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
        List<Tile> tileSet = new ArrayList<>();
        tileSet.add(new Tile(TileState.START, rng.nextInt(width), rng.nextInt(height)));
        tileSet.add(new Tile(TileState.END, rng.nextInt(width), rng.nextInt(height)));
        int x, y;
        for (int i = 0; i < constraints.numOfObjectives; ++i) {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
            if (elevation[x][y] > 0.25)
                tileSet.add(new Tile(TileState.OBJECTIVE, x, y));
        }
        for (int i = 0; i < constraints.enemyLimit; ++i) {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
            if (elevation[x][y] > 0.25)
                tileSet.add(new Tile(TileState.ENEMY, x, y));
        }
        for (int i = 0; i < constraints.itemLimit; ++i) {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
            if (elevation[x][y] > 0.25)
                tileSet.add(new Tile(TileState.ITEM, x, y));
        }
        int numOfObstacle = numTiles - (constraints.itemLimit + constraints.numOfObjectives + constraints.enemyLimit);
        for (int i = 0; i < numOfObstacle; ++i) {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
            if (elevation[x][y] > 0.25)
                tileSet.add(new Tile(TileState.OBSTACLE, x, y));
        }
//        for (int i = 0; i < numTiles - 2; ++i) {
//            //// TODO: 02/12/2016 Change random start to use normal instead of uniform distribution
//            tileSet.add(new Tile(randomState.random(), rng.nextInt(width), rng.nextInt(height)));
//        }
        return new MapCandidate(tileSet);
    }

    private int manhattanDist(int[] pos1, int[] pos2) {
        int dX = pos2[0] - pos1[0];
        int dY = pos2[1] - pos1[1];
        return Math.abs(dX) + Math.abs(dY);
    }

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

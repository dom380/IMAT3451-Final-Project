package dmu.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dmu.mygdx.game.WeatherAPI.WeatherClient;
import dmu.mygdx.game.WeatherAPI.WeatherResponse;
import dmu.project.levelgen.Constraints;
import dmu.project.levelgen.GAPopulationGen;
import dmu.project.levelgen.HeightMap;
import dmu.project.levelgen.MapCandidate;
import dmu.project.levelgen.Tile;
import dmu.project.levelgen.exceptions.LevelGenerationException;

/**
 * Implementation of the LibGDX Screen class.
 * Responsible for rendering the generated level for demonstration of prototype.
 * <p>
 * Created by Dom on 18/11/2016.
 */

public class LevelGenScreen implements Screen {
    private static int width = 80, height = 50;
    private final MyGdxGame game;
    private OrthographicCamera camera;
    private MapBuilder.Map map;
    private OrthogonalTiledMapRenderer renderer = null;
    private int noiseWidth, noiseHeight, difficulty, tileWidth = 16, tileHeight = 16;
    private final static long debugSeed = -2656433763347937011L;
    private boolean debugEnabled;
    private LevelSelectUI ui;
    private List<MapCandidate> mapCandidates;
    private HeightMap heightMap;
    private WeatherResponse weather = null;
    private InputMultiplexer inputMultiplexer = new InputMultiplexer();
    private Player player;
    private Controller controller;
    private GestureDetector mapCameraController;
    private float scaleX, scaleY;
    private List<Enemy> enemies = new ArrayList<>();
    private GameState state = GameState.LEVEL_SELECT;
    private Stopwatch timer;
    private int mapIndex = 0, lastMapIndex = 0;
    GameUI gameUI;

    /**
     * Constructor
     *
     * @param game The main game object.
     * @param noiseWidth The width of the noise sample grid for the Perlin level generator.
     * @param noiseHeight The height of the noise sample grid for the Perlin level generator.
     * @param difficulty The difficulty of the levels to generate.
     * @param debugEnabled Whether to use the debug seed or not.
     */
    public LevelGenScreen(MyGdxGame game, int noiseWidth, int noiseHeight, int difficulty, boolean debugEnabled) {
        this.game = game;
        this.noiseWidth = noiseWidth;
        this.noiseHeight = noiseHeight;
        this.difficulty = difficulty;
        this.debugEnabled = debugEnabled;
        width = game.properties.get("constraints.mapWidth") != null ? Integer.parseInt(game.properties.get("constraints.mapWidth")) : 80;
        height = game.properties.get("constraints.mapHeight") != null ? Integer.parseInt(game.properties.get("constraints.mapHeight")) : 50;
        this.camera = new OrthographicCamera();
        resetCamera();
        init();
    }


    /**
     * Switch the map with the specified index.
     * @param index The index of the map to switch to.
     */
    void switchMap(int index) {
        if (index > 9 || index >= mapCandidates.size())
            index = Math.min(9, mapCandidates.size() - 1);
        List<Tile> tileList = getMapCandidates().get(index).tileSet;
        lastMapIndex = mapIndex;
        mapIndex = index;
        map = MapBuilder.buildMap(width, height, tileWidth, tileHeight, heightMap, tileList, weather);
        if (renderer != null)
            renderer.setMap(map.tiledMap);
        else
            renderer = new OrthogonalTiledMapRenderer(this.map.tiledMap, game.batch);
        if (player != null)
            player.setTileList(tileList);
        renderer.setView(camera);
    }

    /**
     * Switch the next map.
     */
    void switchNextMap() {
        int minIndex = Math.min(9, mapCandidates.size() - 1);
        lastMapIndex = mapIndex;
        mapIndex = mapIndex != minIndex ? mapIndex + 1 : 0;
        List<Tile> tileList = getMapCandidates().get(mapIndex).tileSet;
        map = MapBuilder.buildMap(width, height, tileWidth, tileHeight, heightMap, tileList, weather);
        if (renderer != null)
            renderer.setMap(map.tiledMap);
        else
            renderer = new OrthogonalTiledMapRenderer(this.map.tiledMap, game.batch);
        if (player != null)
            player.setTileList(tileList);
        renderer.setView(camera);
    }

    /**
     * Switch the previous map.
     */
    void switchPreviousMap() {
        int minIndex = Math.min(9, mapCandidates.size() - 1);
        lastMapIndex = mapIndex;
        mapIndex = mapIndex == 0 ? minIndex : mapIndex - 1;
        List<Tile> tileList = getMapCandidates().get(mapIndex).tileSet;
        map = MapBuilder.buildMap(width, height, tileWidth, tileHeight, heightMap, tileList, weather);
        if (renderer != null)
            renderer.setMap(map.tiledMap);
        else
            renderer = new OrthogonalTiledMapRenderer(this.map.tiledMap, game.batch);
        if (player != null)
            player.setTileList(tileList);
        renderer.setView(camera);
    }

    /**
     * Method called when a level is selected. Starts the game play.
     */
    void playMap() {
        state = GameState.PLAYING;
        int objectiveCount = 0;
        TextureAtlas enemyAtlas = new TextureAtlas(Gdx.files.internal("sprites/enemy.atlas"));
        resetGrid(lastMapIndex);
        List<Tile> tileList = getMapCandidates().get(mapIndex).tileSet;
        Vector2 startPos = new Vector2();
        for (Tile tile : tileList) {
            switch (tile.tileState) {
                case START:
                    startPos = new Vector2(tile.position[0], tile.position[1]);
                    break;
                case END:
                    break;
                case ITEM:
                    heightMap.grid.getNode(tile.position[0], tile.position[1]).walkable = false;
                    break;
                case OBSTACLE:
                    heightMap.grid.getNode(tile.position[0], tile.position[1]).walkable = false;
                    break;
                case ENEMY:
                    ((TiledMapTileLayer) map.tiledMap.getLayers().get(1)).setCell(tile.position[0], tile.position[1], null);
                    enemies.add(new Enemy(game.batch, heightMap.grid, enemyAtlas, new Vector2(tile.position[0] * tileWidth, tile.position[1] * tileHeight)));
                    break;
                case OBJECTIVE:
                    objectiveCount++;
                    heightMap.grid.getNode(tile.position[0], tile.position[1]).walkable = false;
                    break;
            }
        }
        if (gameUI != null) inputMultiplexer.removeProcessor(gameUI.getStage());
        camera.zoom = 0.25f;
        player = new Player(this, game.batch, heightMap.grid, tileList, map, 10);
        player.setPosition(startPos);
        controller = new Controller(game.batch, player);
        gameUI = new GameUI(game, this, game.batch, objectiveCount, player.getHP());
        inputMultiplexer.removeProcessor(ui.getStage());
        inputMultiplexer.removeProcessor(mapCameraController);
        inputMultiplexer.addProcessor(controller.getStage());
        inputMultiplexer.addProcessor(controller);
    }


    /**
     * Called when the screen is first shown.
     * Construct the UI and Controller.
     */
    @Override
    public void show() {
        ui = new LevelSelectUI(game, this);
        inputMultiplexer.addProcessor(ui.getStage());
        switchMap(mapIndex);
        player = new Player(this, game.batch, heightMap.grid, mapCandidates.get(0).tileSet, map, 10);
        controller = new Controller(game.batch, player);
    }

    /**
     * Update any logic and render the screen.
     *
     * @param delta Time step in seconds.
     */
    @Override
    public void render(float delta) {
        //Clear the buffer
        Gdx.gl.glClearColor(0.4f, 0.4f, 0.98f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //Update the camera
        camera.update();
        //Render the map
        renderer.setView(camera);
        renderer.render();
        if (delta > 0.01667) delta = 0.0166f; //Limit the time step.
        if (map.particleEffect != null) { //If there's an active particle effect, update and render it.
            map.particleEffect.update(delta);
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            map.particleEffect.draw(game.batch);
            game.batch.end();
        }
        switch (state) {
            case PLAYING: {
                if (player.getHP() <= 0) {
                    state = GameState.DEAD;
                    inputMultiplexer.removeProcessor(controller.getStage());
                    inputMultiplexer.removeProcessor(controller);
                    gameUI.setGameOverText("You died.");
                    timer = Stopwatch.createStarted();
                }
                if (gameUI.getNumOfObjectives() <= 0) {
                    state = GameState.LEVEL_COMPLETE;
                    gameUI.setGameOverText("You win!");
                    gameUI.switchToWinUI();
                    inputMultiplexer.removeProcessor(controller.getStage());
                    inputMultiplexer.removeProcessor(controller);
                    inputMultiplexer.addProcessor(gameUI.getStage());
                }
                camera.position.set(player.position.x, player.position.y, 0);
                fixCamBounds();
                player.update(delta, enemies);
                Iterator<Enemy> enemyIterator = enemies.iterator();
                while (enemyIterator.hasNext()) {
                    Enemy next = enemyIterator.next();
                    next.update(delta, player);
                    next.render(delta, camera);
                }
                player.render(delta, camera);
                controller.draw(delta);
                gameUI.draw();
                break;
            }
            case LEVEL_SELECT:
                ui.draw();
                break;
            case DEAD: {
                if (timer.elapsed(TimeUnit.SECONDS) > 6) {
                    timer.stop();
                    timer.reset();
                    state = GameState.LEVEL_SELECT;
                    resetCamera();
                    mapCameraController = new GestureDetector(new CameraController2D(camera, Math.min(scaleX, scaleY), scaleX, scaleY));
                    inputMultiplexer.addProcessor(mapCameraController);
                    show();
                }
                for (Enemy enemy : enemies) {
                    enemy.render(delta, camera);
                }
                controller.draw(delta);
                gameUI.draw();
                break;
            }
            case LEVEL_COMPLETE:
                for (Enemy enemy : enemies) {
                    enemy.render(delta, camera);
                }
                player.render(delta, camera);
                gameUI.draw();
                break;
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        if (ui != null) ui.resize(width, height);
        if (gameUI != null) gameUI.resize(width, height);
        if (controller != null) controller.resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        if (map != null) map.dispose();
        if (ui != null) ui.dispose();
        if (player != null) player.dispose();
        if (gameUI != null) gameUI.dispose();
        if(controller != null) controller.dispose();
    }

    public List<MapCandidate> getMapCandidates() {
        return mapCandidates;
    }

    public int getMapIndex() {
        return mapIndex;
    }

    /////////////////////////////
    //Private Utility Methods //
    ////////////////////////////

    /**
     * Generates the level.
     *
     * @return true if successful.
     */
    private boolean init() {
        scaleX = (float) width / (Gdx.graphics.getWidth() / tileWidth);
        scaleY = (float) height / (Gdx.graphics.getHeight() / tileWidth);
        //Set level constraints
        Constraints constraints = readConstraints(game.properties);
        //Generate Level
        GAPopulationGen populationGen = new GAPopulationGen(constraints);
        try {
            mapCandidates = populationGen.populate();
        } catch (LevelGenerationException e) {
            Gdx.app.error("Level Creation", "Unrecoverable exception thrown.", e);
            game.returnToMenu(); //Go back to main menu.
        }
        WeatherClient weatherClient = new WeatherClient(game.apiUrl, game.apiKey);
        double[] latLong = game.getLocationService().getLatLong();
        if (latLong != null)
            weather = weatherClient.getWeather(latLong[0], latLong[1]);
        heightMap = populationGen.getHeightMap();
        map = MapBuilder.buildMap(width, height, tileWidth, tileHeight, heightMap, mapCandidates.get(0).tileSet, weather);
        CameraController2D cameraInputController = new CameraController2D(camera, Math.min(scaleX, scaleY), scaleX, scaleY);
        mapCameraController = new GestureDetector(cameraInputController);
        inputMultiplexer.addProcessor(mapCameraController);
        Gdx.input.setInputProcessor(inputMultiplexer);
        return true;
    }

    /**
     * Utility method to read the game's configuration.
     *
     * @param properties A map of name value pairs.
     * @return Returns the constraints with the values from the properties.
     */
    private Constraints readConstraints(ObjectMap<String, String> properties) {
        Constraints constraints = new Constraints();
        String value = properties.get("constraints.populationSize", "100");
        constraints.setPopulationSize(Integer.valueOf(value));
        value = properties.get("constraints.maxGenerations", "50");
        constraints.setMaxGenerations(Integer.valueOf(value));
        constraints.setMapHeight(height);
        constraints.setMapWidth(width);
        constraints.setNoiseWidth(noiseWidth);
        constraints.setNoiseHeight(noiseHeight);
        value = properties.get("constraints.objectivesEnabled", "true");
        constraints.setObjectivesEnabled(Boolean.parseBoolean(value));
        constraints.setDifficulty(difficulty);
        if (debugEnabled)
            constraints.setSeed(debugSeed);
        return constraints;
    }

    /**
     * Utility method to constrain the camera to the scene.
     */
    private void fixCamBounds() {
        float scrollLimitX = camera.viewportWidth * scaleX;
        float scrollLimitY = camera.viewportHeight * scaleY;
        //Constrain camera from scrolling outside of map
        if (camera.position.x - (camera.viewportWidth * camera.zoom) / 2 < 0)
            camera.position.x = (camera.viewportWidth * camera.zoom) / 2;
        else if (camera.position.x + (camera.viewportWidth * camera.zoom) / 2 > scrollLimitX)
            camera.position.x = scrollLimitX - (camera.viewportWidth * camera.zoom) / 2;
        if (camera.position.y - (camera.viewportHeight * camera.zoom) / 2 < 0)
            camera.position.y = (camera.viewportHeight * camera.zoom) / 2;
        else if (camera.position.y + (camera.viewportHeight * camera.zoom) / 2 > scrollLimitY)
            camera.position.y = scrollLimitY - (camera.viewportHeight * camera.zoom) / 2;
        camera.update();
    }

    /**
     * Utility method to reset the camera to default position.
     */
    private void resetCamera() {
        if (camera != null) {
            this.camera.viewportWidth = Gdx.graphics.getWidth();
            this.camera.viewportHeight = Gdx.graphics.getHeight();
            this.camera.position.set(camera.viewportWidth / 2.f, camera.viewportHeight / 2.f, 0);
            this.camera.update();
        }
    }

    /**
     * Reset the game grid.
     * @param lastMapIndex Index of the last map used.
     */
    private void resetGrid(int lastMapIndex) {
        List<Tile> tileList = getMapCandidates().get(lastMapIndex).tileSet;
        for (Tile tile : tileList) {
            heightMap.grid.getNode(tile.position[0], tile.position[1]).walkable = true;
        }
    }


    /**
     * Enum specifying the game state.
     */
    private enum GameState {
        PLAYING,
        LEVEL_SELECT,
        DEAD,
        LEVEL_COMPLETE
    }
}

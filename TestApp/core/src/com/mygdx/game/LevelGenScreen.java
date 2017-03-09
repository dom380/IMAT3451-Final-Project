package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.common.base.Stopwatch;
import com.mygdx.game.WeatherAPI.WeatherClient;
import com.mygdx.game.WeatherAPI.WeatherResponse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dmu.project.levelgen.Constraints;
import dmu.project.levelgen.GAPopulationGen;
import dmu.project.levelgen.HeightMap;
import dmu.project.levelgen.MapCandidate;
import dmu.project.levelgen.Tile;

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
    private boolean debugEnabled, playing = false;
    private LevelSelectUI ui;
    private List<MapCandidate> mapCandidates;
    private HeightMap heightMap;
    private WeatherResponse weather = null;
    private InputMultiplexer inputMultiplexer = new InputMultiplexer();
    private Player player;
    private Controller controller;
    private GestureDetector mapCameraController;
    private float scaleX, scaleY;
    private FPSLogger fpsLogger = new FPSLogger();
    private List<Enemy> enemies = new ArrayList<>();
    GameUI gameUI;

    public LevelGenScreen(MyGdxGame game, int noiseWidth, int noiseHeight, int difficulty, boolean debugEnabled) {
        this.game = game;
        this.noiseWidth = noiseWidth;
        this.noiseHeight = noiseHeight;
        this.difficulty = difficulty;
        this.debugEnabled = debugEnabled;
        width = game.properties.get("constraints.mapWidth") != null ? Integer.parseInt(game.properties.get("constraints.mapWidth")) : 80;
        height = game.properties.get("constraints.mapHeight") != null ? Integer.parseInt(game.properties.get("constraints.mapHeight")) : 50;
        this.camera = new OrthographicCamera();
        this.camera.viewportWidth = Gdx.graphics.getWidth();
        this.camera.viewportHeight = Gdx.graphics.getHeight();
        this.camera.position.set(camera.viewportWidth / 2.f, camera.viewportHeight / 2.f, 0);
        this.camera.update();
        init();
    }


    private boolean init() {
        scaleX = (float) width / (Gdx.graphics.getWidth() / tileWidth);
        scaleY = (float) height / (Gdx.graphics.getHeight() / tileWidth);
        //Set level constraints
        Constraints constraints = readConstraints(game.properties);
        //Generate Level
        GAPopulationGen populationGen = new GAPopulationGen(constraints);
        mapCandidates = populationGen.populate();
        WeatherClient weatherClient = new WeatherClient(game.apiUrl, game.apiKey);//Todo find a better way of getting this string from android resources
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

    void switchMap(List<Tile> tileList) {
        map = MapBuilder.buildMap(width, height, tileWidth, tileHeight, heightMap, tileList, weather);
        if (renderer != null)
            renderer.setMap(map.tiledMap);
        else
            renderer = new OrthogonalTiledMapRenderer(this.map.tiledMap, game.batch);
        if (player != null)
            player.setTileList(tileList);
        renderer.setView(camera);
    }

    void playMap(List<Tile> tileList) {
        playing = true;
        int objectiveCount = 0;
        TextureAtlas enemyAtlas = new TextureAtlas(Gdx.files.internal("sprites/enemy.atlas"));
        for (Tile tile : tileList) {
            switch (tile.tileState) {
                case START:
                    player.setPosition(new Vector2(tile.position[0], tile.position[1]));
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
        inputMultiplexer.removeProcessor(ui.getStage());
        inputMultiplexer.removeProcessor(mapCameraController);
        inputMultiplexer.addProcessor(controller.getStage());
        inputMultiplexer.addProcessor(controller);
        camera.zoom = 0.25f;
        player.setMap(map);
        gameUI = new GameUI(game.batch, objectiveCount, player.getHP());
    }

    @Override
    public void show() {
        ui = new LevelSelectUI(game, this);
        inputMultiplexer.addProcessor(ui.getStage());
        switchMap(mapCandidates.get(0).tileSet);
        player = new Player(this, game.batch, heightMap.grid, mapCandidates.get(0).tileSet, map, 10);
        controller = new Controller(game.batch, player);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.4f, 0.4f, 0.98f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        renderer.setView(camera);
        renderer.render();
        if (playing) {
            camera.position.set(player.position.x, player.position.y, 0);
            fixCamBounds();
            player.update(delta, enemies);
            player.render(delta, camera);
            Iterator<Enemy> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy next = enemyIterator.next();
                next.update(delta);
                next.render(delta, camera);

            }
            controller.draw(delta);
            gameUI.draw();
        } else {
            ui.draw();
        }
        if (map.particleEffect != null) {
            map.particleEffect.update(delta);
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            map.particleEffect.draw(game.batch);
            game.batch.end();
        }
        fpsLogger.log();
    }


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
        map.dispose();
        ui.dispose();
        player.dispose();
        gameUI.dispose();
    }

    public List<MapCandidate> getMapCandidates() {
        return mapCandidates;
    }
}

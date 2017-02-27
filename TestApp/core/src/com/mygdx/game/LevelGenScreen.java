package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.mygdx.game.WeatherAPI.WeatherClient;
import com.mygdx.game.WeatherAPI.WeatherResponse;

import java.util.List;

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
    private static final int width = 80, height = 50;
    private final MyGdxGame game;
    private OrthographicCamera camera;
    private MapBuilder.Map map;
    private TiledMapRenderer renderer;
    private int noiseWidth, noiseHeight, difficulty, tileWidth = 16, tileHeight = 16;
    private final static long debugSeed = -2656433763347937011L;
    private boolean debugEnabled;
    private GameUI ui;
    private List<MapCandidate> mapCandidates;
    private HeightMap heightMap;
    private WeatherResponse weather = null;

    public LevelGenScreen(MyGdxGame game) {
        this(game, 1, 1, 5, false);
    }

    public LevelGenScreen(MyGdxGame game, int noiseWidth, int noiseHeight, int difficulty, boolean debugEnabled) {
        this.game = game;
        this.noiseWidth = noiseWidth;
        this.noiseHeight = noiseHeight;
        this.difficulty = difficulty;
        this.debugEnabled = debugEnabled;
        ui = new GameUI(game, this);
        camera = new OrthographicCamera();
        camera.viewportWidth = Gdx.graphics.getWidth();
        camera.viewportHeight = Gdx.graphics.getHeight();
        camera.position.set(camera.viewportWidth / 2.f, camera.viewportHeight / 2.f, 0);
        camera.update();
        init();
    }


    private boolean init() {

        float scaleX = (float) width / (Gdx.graphics.getWidth() / tileWidth);
        float scaleY = (float) height / (Gdx.graphics.getHeight() / tileWidth);
        //Set level constraints
        Constraints constraints = new Constraints();
        constraints.setPopulationSize(100);
        constraints.setMaxGenerations(50);
        constraints.setMapHeight(height);
        constraints.setMapWidth(width);
        constraints.setNoiseWidth(noiseWidth);
        constraints.setNoiseHeight(noiseHeight);
        constraints.setObjectivesEnabled(true);
        constraints.setDifficulty(difficulty);
        if (debugEnabled)
            constraints.setSeed(debugSeed);
        //Generate Level
        GAPopulationGen populationGen = new GAPopulationGen(constraints);
        mapCandidates = populationGen.populate();
        WeatherClient weatherClient = new WeatherClient(game.apiUrl, game.apiKey);//Todo find a better way of getting this string from android resources

        double[] latLong = game.getLocationService().getLatLong();
        if (latLong != null)
            weather = weatherClient.getWeather(latLong[0], latLong[1]);
        heightMap = populationGen.getHeightMap();
        map = MapBuilder.buildMap(width, height, tileWidth, tileHeight, heightMap, mapCandidates.get(0).tileSet, weather);
        renderer = new OrthogonalTiledMapRenderer(this.map.tiledMap);
        renderer.setView(camera);
        CameraController2D cameraInputController = new CameraController2D(camera, Math.min(scaleX, scaleY), scaleX, scaleY);
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(cameraInputController));
        inputMultiplexer.addProcessor(ui.getStage());
        Gdx.input.setInputProcessor(inputMultiplexer);
        return true;
    }

    public void switchMap(List<Tile> tileList) {
        map = MapBuilder.buildMap(width, height, tileWidth, tileHeight, heightMap, tileList, weather);
        renderer = new OrthogonalTiledMapRenderer(this.map.tiledMap); //Can't find a better way of changing the map
        renderer.setView(camera);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(100f / 255f, 100f / 255f, 250f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //viewport.apply();
        camera.update();
        renderer.setView(camera);
        renderer.render();
        if (map.particleEffect != null) {
            map.particleEffect.update(delta);
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            map.particleEffect.draw(game.batch);
            game.batch.end();
        }
        ui.draw();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        ui.resize(width, height);
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
    }

    public List<MapCandidate> getMapCandidates() {
        return mapCandidates;
    }
}

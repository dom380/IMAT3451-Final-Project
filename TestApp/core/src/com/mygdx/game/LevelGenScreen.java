package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.mygdx.game.WeatherAPI.WeatherClient;
import com.mygdx.game.WeatherAPI.WeatherResponse;

import java.util.List;

import dmu.project.levelgen.Constraints;
import dmu.project.levelgen.GAPopulationGen;
import dmu.project.levelgen.Tile;

/**
 * Implementation of the LibGDX Screen class.
 * Responsible for rendering the generated level for demonstration of prototype.
 * <p>
 * Created by Dom on 18/11/2016.
 */

public class LevelGenScreen implements Screen {

    private MyGdxGame game;
    private Camera camera;
    private MapBuilder.Map map;
    private TiledMapRenderer renderer;
    private int noiseWidth, noiseHeight, difficulty, tileWidth = 16, tileHeight = 16;
    private final static long debugSeed = -2656433763347937011L;
    private boolean debugEnabled;
    private SpriteBatch batch = new SpriteBatch();

    public LevelGenScreen(MyGdxGame game) {
        this(game, 1, 1, 5, false);
    }

    public LevelGenScreen(MyGdxGame game, int noiseWidth, int noiseHeight, int difficulty, boolean debugEnabled) {
        this.game = game;
        this.noiseWidth = noiseWidth;
        this.noiseHeight = noiseHeight;
        this.difficulty = difficulty;
        this.debugEnabled = debugEnabled;
        camera = new OrthographicCamera();
        camera.viewportWidth = Gdx.graphics.getWidth();
        camera.viewportHeight = Gdx.graphics.getHeight();
        camera.position.set(camera.viewportWidth / 2.f, camera.viewportHeight / 2.f, 0);
        camera.update();
        init();
    }


    private boolean init() {
        int width = 80;
        int height = 50;
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
        List<Tile> mapObjects = populationGen.populate();
//        int count = 0;
//        for (Tile tile : mapObjects) {
//            if (tile.tileState == TileState.ENEMY)
//                count++;
//        }
        WeatherClient weatherClient = new WeatherClient(game.apiUrl, game.apiKey);//Todo find a better way of getting this string from android resources
        WeatherResponse weather = null;
        double[] latLong = game.getLocationService().getLatLong();
        if (latLong != null)
            weather = weatherClient.getWeather(latLong[0], latLong[1]);
        map = MapBuilder.buildMap(width, height, tileWidth, tileHeight, populationGen.getHeightMap(), mapObjects, weather);
        renderer = new OrthogonalTiledMapRenderer(this.map.tiledMap);
        renderer.setView((OrthographicCamera) camera);
        CameraController2D cameraInputController = new CameraController2D((OrthographicCamera) camera, Math.min(scaleX, scaleY), scaleX, scaleY);
        Gdx.input.setInputProcessor(new GestureDetector(cameraInputController));
        return true;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(100f / 255f, 100f / 255f, 250f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        renderer.setView((OrthographicCamera) camera);
        renderer.render();
        if (map.particleEffect != null) {
            map.particleEffect.update(delta);
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            map.particleEffect.draw(batch);
            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
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
    }
}

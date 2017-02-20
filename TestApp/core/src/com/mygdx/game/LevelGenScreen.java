package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;

import java.io.UnsupportedEncodingException;
import java.util.List;

import dmu.project.levelgen.Constraints;
import dmu.project.levelgen.GAPopulationGen;
import dmu.project.levelgen.HeightMap;
import dmu.project.levelgen.Tile;
import dmu.project.levelgen.TileState;
import dmu.project.utils.RestClient;

/**
 * Implementation of the LibGDX Screen class.
 * Responsible for rendering the generated level for demonstration of prototype.
 * <p>
 * Created by Dom on 18/11/2016.
 */

public class LevelGenScreen implements Screen {

    private MyGdxGame game;
    private Camera camera;
    private Texture tileTexture;
    private Texture spriteTexture;
    private TiledMap map;
    private TiledMapRenderer renderer;
    private int noiseWidth, noiseHeight, difficulty, tileWidth = 16, tileHeight = 16;
    private final static long debugSeed = -2656433763347937011L;
    private boolean debugEnabled;

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

    public LevelGenScreen(MyGdxGame game, final Camera camera) {
        this.game = game;
        this.camera = camera;
        camera.update();
        init();
    }

    private boolean init() {
        map = new TiledMap();
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

//        double[] latLong = game.getLocationService().getLatLong();
        //if (latLong != null) {
//            try {
//                RestClient restClient = new RestClient("http://api.openweathermap.org/data/2.5/weather");
//                restClient.addParam("lat", String.valueOf(latLong[0]));
//                restClient.addParam("lon", String.valueOf(latLong[1]));
//                restClient.addParam("units", "metric");
//                restClient.addParam("appid", "2c2e5d04d5f1c71108c7d2e4719a04fb");
//
//                restClient.execute(RestClient.RequestMethod.GET);
//                String response = restClient.response;
//
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
       // }
        //Load textures
        tileTexture = new Texture(Gdx.files.internal("16gradientv2.png"));
        tileTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        spriteTexture = new Texture(Gdx.files.internal("sprites.png"));
        spriteTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[][] splitTiles = TextureRegion.split(tileTexture, tileWidth, tileHeight);
        TextureRegion[][] splitSprites = TextureRegion.split(spriteTexture, 16, 16);
        //Construct TileMap.
        HeightMap heightMap = populationGen.getHeightMap();
        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
        TiledMapTileLayer spriteLayer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
        for (int x = 0; x < width; x++) { //Set each tile to the correct sprite based on elevation.
            for (int y = 0; y < height; y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                int tx, ty;
                double scalar;
                if (heightMap.elevation[x][y] < 0.25) {
                    tx = 0;
                    scalar = heightMap.elevation[x][y] / 0.25;
                } else if (heightMap.elevation[x][y] < 0.35) {
                    tx = 1;
                    scalar = (heightMap.elevation[x][y] - 0.24) / (0.35 - 0.25);
                } else if (heightMap.elevation[x][y] < 0.55) {
                    tx = 2;
                    scalar = (heightMap.elevation[x][y] - 0.34) / (0.55 - 0.35);
                } else {
                    tx = 3;
                    scalar = (heightMap.elevation[x][y] - 0.54) / (1.0 - 0.55);
                }
                ty = (int) (64 * heightMap.elevation[x][y] * scalar);
                if (ty > 63) {
                    ty = 63;
                }
                cell.setTile(new StaticTiledMapTile(splitTiles[tx][ty]));
                layer.setCell(x, y, cell);
            }
        }
        for (Tile tile : mapObjects) { //For each level object set correct sprite.
            int tx = 0, ty = 0;
            switch (tile.tileState) {
                case START:
                    tx = 4;
                    break;
                case OBJECTIVE:
                    tx = 1;
                    break;
                case ITEM:
                    tx = 0;
                    break;
                case OBSTACLE:
                    tx = 3;
                    break;
                case ENEMY:
                    tx = 2;
                    break;
            }
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(new StaticTiledMapTile(splitSprites[ty][tx]));
            spriteLayer.setCell(tile.position[0], tile.position[1], cell);
        }
        map.getLayers().add(layer);
        map.getLayers().add(spriteLayer);
        renderer = new OrthogonalTiledMapRenderer(map);
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
        tileTexture.dispose();
        map.dispose();
    }
}

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

import java.util.List;

import dmu.project.levelgen.Constraints;
import dmu.project.levelgen.GAPopulationGen;
import dmu.project.levelgen.LevelGenerator;
import dmu.project.levelgen.Tile;

/**
 * Implementation of the LibGDX Screen class.
 * Responsible for rendering the generated level for demonstration of prototype.
 *
 * Created by Dom on 18/11/2016.
 */

public class LevelGenScreen implements Screen {

    private MyGdxGame game;
    private Camera camera;
    private Texture tileTexture;
    private Texture spriteTexture;
    private TiledMap map;
    private TiledMapRenderer renderer;
    private int noiseWidth, noiseHeight;

    public LevelGenScreen(MyGdxGame game) {
        this(game, 1, 1);
    }

    public LevelGenScreen(MyGdxGame game, int noiseWidth, int noiseHeight)
    {
        this.game = game;
        this.noiseWidth = noiseWidth;
        this.noiseHeight = noiseHeight;
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
        int width = (Gdx.graphics.getWidth() / 8);
        int height = (Gdx.graphics.getHeight() / 10);
        //Set level constraints
        Constraints constraints = new Constraints();
        constraints.setEnemyLimit(300);
        constraints.setLength(500);
        constraints.setItemLimit(30);
        constraints.setPopulationSize(200);
        constraints.setMaxGenerations(100);
        constraints.setNumOfObjectives(5);
        constraints.setMapHeight(height);
        constraints.setMapWidth(width);
        constraints.setTilePercentage(0.1f);
        constraints.setNoiseWidth(noiseWidth);
        constraints.setNoiseHeight(noiseHeight);
        //Generate Level
        GAPopulationGen populationGen = new GAPopulationGen(constraints);
        List<Tile> mapObjects = populationGen.populate();
        //Load textures
        tileTexture = new Texture(Gdx.files.internal("gradientFinal.png"));
        tileTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        spriteTexture = new Texture(Gdx.files.internal("sprites.png"));
        spriteTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        TextureRegion[][] splitTiles = TextureRegion.split(tileTexture, 8, 10);
        TextureRegion[][] splitSprites = TextureRegion.split(spriteTexture, 16, 16);
        //Construct TileMap.
        double[][] elevation = populationGen.getElevation();
        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, 8, 10);
        TiledMapTileLayer spriteLayer = new TiledMapTileLayer(width, height,  8, 10);
        for (int x = 0; x < width; x++) { //Set each tile to the correct sprite based on elevation.
            for (int y = 0; y < height; y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                int tx, ty;
                double scalar;
                if (elevation[x][y] < 0.25) {
                    tx = 0;
                    scalar = elevation[x][y] / 0.25;
                } else if (elevation[x][y] < 0.35) {
                    tx = 1;
                    scalar = (elevation[x][y] - 0.24) / (0.35 - 0.25);
                } else if (elevation[x][y] < 0.55) {
                    tx = 2;
                    scalar = (elevation[x][y] - 0.34) / (0.55 - 0.35);
                } else {
                    tx = 3;
                    scalar = (elevation[x][y] - 0.54) / (1.0 - 0.55);
                }
                ty = (int) (32 * elevation[x][y] * scalar);
                if(ty > 31){
                    ty =31;
                }
                cell.setTile(new StaticTiledMapTile(splitTiles[tx][ty]));
                layer.setCell(x, y, cell);
            }
        }
        for(Tile tile:mapObjects){ //For each level object set correct sprite.
            int tx = 0, ty = 0;
            switch (tile.tileState) {
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
            spriteLayer.setCell(tile.position[0],tile.position[1],cell);
        }
        map.getLayers().add(layer);
        map.getLayers().add(spriteLayer);
        renderer = new OrthogonalTiledMapRenderer(map);
        renderer.setView((OrthographicCamera) camera);
        CameraController2D cameraInputController = new CameraController2D((OrthographicCamera) camera);
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

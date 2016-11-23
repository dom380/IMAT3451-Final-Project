package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import dmu.project.levelgen.LevelGenerator;
import dmu.project.levelgen.PerlinLevelGen;

/**
 * Created by Dom on 18/11/2016.
 */

public class LevelGenScreen implements Screen {

    private MyGdxGame game;
    private Camera camera;
    private Texture tileTexture;
    private TiledMap map;
    private TiledMapRenderer renderer;
    private Vector2 lastTouch = new Vector2();
    LevelGenerator levelGen;

    public LevelGenScreen(MyGdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.viewportWidth = Gdx.graphics.getWidth();
        camera.viewportHeight = Gdx.graphics.getHeight();
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
        init();
    }

    public LevelGenScreen(MyGdxGame game, final Camera camera) {
        this.game = game;
        this.camera = camera;
        camera.update();
        init();
    }

    private boolean init(){
        map = new TiledMap();
        levelGen = new PerlinLevelGen();
//        tileTexture = new Texture(Gdx.files.internal("terrain.png"));
        tileTexture = new Texture(Gdx.files.internal("gradient.png"));
//        TextureRegion[][] splitTiles = TextureRegion.split(tileTexture, 32, 32);
        TextureRegion[][] splitTiles = TextureRegion.split(tileTexture, 10, 10);
        int width = (Gdx.graphics.getWidth() / 10);
        int height = (Gdx.graphics.getHeight() / 10);
        double[][] elevation = levelGen.generateLevel(512, 512);
//        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, 32, 32);
        TiledMapTileLayer layer = new TiledMapTileLayer(width, height,10,10);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                int tx, ty, nx, ny;
                nx = x * (512 / width);
                ny = y * (512 / height);
                ty = (int)(24*elevation[nx][ny]);
                if (elevation[nx][ny] < 0.25) {
                    tx = 0;
                } else if (elevation[nx][ny] < 0.35) {
                    tx = 1;
                } else if (elevation[nx][ny] < 0.55){
                    tx = 2;
                } else{
                    tx = 3;
                }
//                if (elevation[nx][ny] < 0.25) {
//                    tx = 3;
//                    ty = 28;
//                } else if (elevation[nx][ny] > 0.5) {
//                    tx = 17;
//                    ty = 19;
//                } else if (elevation[nx][ny] > 0.4) {
//                    tx = 3;
//                    ty = 13;
//                } else if (elevation[nx][ny] > 0.35) {
//                    tx = 11;
//                    ty = 1;
//                } else {
//                    tx = 9;
//                    ty = 1;
//                }

                cell.setTile(new StaticTiledMapTile(splitTiles[tx][ty]));
                layer.setCell(x, y, cell);
            }
        }
        map.getLayers().add(layer);
        renderer = new OrthogonalTiledMapRenderer(map);
        CameraController2D cameraInputController = new CameraController2D((OrthographicCamera) camera);
        Gdx.input.setInputProcessor(new GestureDetector(cameraInputController));
//        Gdx.input.setInputProcessor(new InputAdapter() {
//            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//                lastTouch.set(screenX, screenY);
//                return true;
//            }
//
//            public boolean touchDragged(int screenX, int screenY, int pointer) {
//                Vector2 newTouch = new Vector2(screenX, screenY);
//                // delta will now hold the difference between the last and the current touch positions
//                // delta.x > 0 means the touch moved to the right, delta.x < 0 means a move to the left
//                Vector2 delta = newTouch.cpy().sub(lastTouch);
//                camera.translate(-delta.x, delta.y, 0);
//                lastTouch = newTouch;
//                return true;
//            }
//
//        });
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

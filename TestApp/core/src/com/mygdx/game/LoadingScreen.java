package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.badlogic.gdx.utils.async.AsyncTask;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Dom on 01/03/2017.
 */

public class LoadingScreen implements Screen {

    private MyGdxGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private float deltaTime = 0.0f;
    private Animation anim;
    private TextureAtlas textureAtlas;
    AsyncExecutor asyncExecutor = new AsyncExecutor(10);

    AsyncResult<LevelGenScreen> task;

    private LevelGenScreen asyncMethod(MyGdxGame game, int noiseWidth, int noiseHeight, int difficulty, boolean debugEnabled) {
        return new LevelGenScreen(game, noiseWidth, noiseHeight, difficulty, debugEnabled);
    }

    public LoadingScreen(final MyGdxGame game, final int noiseWidth, final int noiseHeight, final int difficulty, final boolean debugEnabled) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new StretchViewport(800, 480, camera);
        viewport.apply();
        batch = game.batch;
        textureAtlas = new TextureAtlas(Gdx.files.internal("sprites/loadingRing.atlas"));
        anim = new Animation(0.06f, textureAtlas.getRegions());
        anim.setPlayMode(PlayMode.LOOP);
        task = asyncExecutor.submit(new AsyncTask<LevelGenScreen>() {
            public LevelGenScreen call() {
                return asyncMethod(game, noiseWidth, noiseHeight, difficulty, debugEnabled);
            }
        });
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.1f, .12f, .16f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (!task.isDone()) {
            deltaTime += delta;
            TextureRegion keyFrame = anim.getKeyFrame(deltaTime);
            batch.begin();
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.draw(keyFrame, -keyFrame.getRegionWidth()/2, -keyFrame.getRegionHeight()/2);
            batch.end();
        } else {
            game.switchScreen(task.get());
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
        textureAtlas.dispose();
    }
}

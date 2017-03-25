package dmu.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.badlogic.gdx.utils.async.AsyncTask;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Dom on 01/03/2017.
 * Implementation of the LibGDX Screen interface.
 * Creates a background thread to generate the level while rendering a loading animation.
 */

public class LoadingScreen implements Screen {

    private MyGdxGame mGame;
    private OrthographicCamera mCamera;
    private Viewport mViewport;
    private SpriteBatch mBatch;
    private Animation mAnimation;
    private TextureAtlas mTextureAtlas;
    private TextureAtlas mUiAtlas;
    private Skin mSkin;
    private Stage mStage;
    AsyncExecutor mAsyncExecutor = new AsyncExecutor(10);
    AsyncResult<LevelGenScreen> mTask;


    /**
     * Constructor.
     * <p>
     * Initialises an asynchronous mTask to generate the level pool.
     *
     * @param mGame        The main mGame object.
     * @param noiseWidth   The width of noise to sample from.
     * @param noiseHeight  The height of noise to sample from.
     * @param difficulty   The difficulty of the levels to generate.
     * @param debugEnabled If true use the debug seed.
     */
    public LoadingScreen(final MyGdxGame mGame, final int noiseWidth, final int noiseHeight, final int difficulty, final boolean debugEnabled) {
        this.mGame = mGame;
        mBatch = mGame.mBatch;
        mCamera = new OrthographicCamera();
        mViewport = new StretchViewport(800, 480, mCamera);
        mViewport.apply();
        mTextureAtlas = new TextureAtlas(Gdx.files.internal("sprites/loadingRing.atlas"));
        mUiAtlas = new TextureAtlas(Gdx.files.internal("sprites/uiskin.atlas"));
        mSkin = new Skin(Gdx.files.internal("sprites/uiskin.json"), mUiAtlas);

        mAnimation = new Animation(0.06f, mTextureAtlas.getRegions());
        mAnimation.setPlayMode(PlayMode.LOOP);

        Label label = new Label("Generating level...", mSkin);
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.padBottom(50.0f);
        table.add(label).padBottom(50.0f);
        table.row();
        table.add(new dmu.mygdx.game.AnimActor(mAnimation, true)).padTop(50.0f);
        mStage = new Stage(mViewport, mBatch);
        mStage.addActor(table);
        //Start up the background thread to generate the levels.
        mTask = mAsyncExecutor.submit(new AsyncTask<LevelGenScreen>() {
            public LevelGenScreen call() {
                return asyncMethod(mGame, noiseWidth, noiseHeight, difficulty, debugEnabled);
            }
        });
    }

    @Override
    public void show() {

    }

    /**
     * Render the loading animation.
     *
     * @param delta The time step.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.1f, .12f, .16f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (!mTask.isDone()) {
            mStage.act(delta);
            mStage.draw();
        } else {
            mGame.switchScreen(mTask.get());
        }
    }

    @Override
    public void resize(int width, int height) {
        mViewport.update(width, height);
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

    /**
     * Dispose of assets.
     */
    @Override
    public void dispose() {
        mTextureAtlas.dispose();
        mUiAtlas.dispose();
        mStage.dispose();
    }

    /////////////////////////////
    //Private Utility Methods //
    ////////////////////////////

    /**
     * Creates a LevelGenScreen object that will call the Procedural content generation system to generate levels.
     * To be run on a background thread as this is a long running process.
     *
     * @param game         The main mGame object.
     * @param noiseWidth   The width of noise to sample from.
     * @param noiseHeight  The height of noise to sample from.
     * @param difficulty   The difficulty of the levels to generate.
     * @param debugEnabled If true use the debug seed.
     * @return The LevelGenScreen with the generated levels.
     */
    private LevelGenScreen asyncMethod(MyGdxGame game, int noiseWidth, int noiseHeight, int difficulty, boolean debugEnabled) {
        return new LevelGenScreen(game, noiseWidth, noiseHeight, difficulty, debugEnabled);
    }
}

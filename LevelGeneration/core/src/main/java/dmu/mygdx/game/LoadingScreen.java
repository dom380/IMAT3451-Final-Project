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

    private MyGdxGame game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private Animation anim;
    private TextureAtlas textureAtlas;
    private TextureAtlas uiAtlas;
    private Skin skin;
    private Stage stage;

    AsyncExecutor asyncExecutor = new AsyncExecutor(10);

    AsyncResult<LevelGenScreen> task;


    /**
     * Constructor.
     * <p>
     * Initialises an asynchronous task to generate the level pool.
     *
     * @param game         The main game object.
     * @param noiseWidth   The width of noise to sample from.
     * @param noiseHeight  The height of noise to sample from.
     * @param difficulty   The difficulty of the levels to generate.
     * @param debugEnabled If true use the debug seed.
     */
    public LoadingScreen(final MyGdxGame game, final int noiseWidth, final int noiseHeight, final int difficulty, final boolean debugEnabled) {
        this.game = game;
        batch = game.batch;
        camera = new OrthographicCamera();
        viewport = new StretchViewport(800, 480, camera);
        viewport.apply();
        textureAtlas = new TextureAtlas(Gdx.files.internal("sprites/loadingRing.atlas"));
        uiAtlas = new TextureAtlas(Gdx.files.internal("sprites/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("sprites/uiskin.json"), uiAtlas);

        anim = new Animation(0.06f, textureAtlas.getRegions());
        anim.setPlayMode(PlayMode.LOOP);

        Label label = new Label("Generating level...", skin);
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.padBottom(50.0f);
        table.add(label).padBottom(50.0f);
        table.row();
        table.add(new dmu.mygdx.game.AnimActor(anim, true)).padTop(50.0f);
        stage = new Stage(viewport, batch);
        stage.addActor(table);
        //Start up the background thread to generate the levels.
        task = asyncExecutor.submit(new AsyncTask<LevelGenScreen>() {
            public LevelGenScreen call() {
                return asyncMethod(game, noiseWidth, noiseHeight, difficulty, debugEnabled);
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
        if (!task.isDone()) {
            stage.act(delta);
            stage.draw();
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

    /**
     * Dispose of assets.
     */
    @Override
    public void dispose() {
        textureAtlas.dispose();
        uiAtlas.dispose();
        stage.dispose();
    }

    /////////////////////////////
    //Private Utility Methods //
    ////////////////////////////

    /**
     * Creates a LevelGenScreen object that will call the Procedural content generation system to generate levels.
     * To be run on a background thread as this is a long running process.
     *
     * @param game         The main game object.
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

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

import dmu.project.levelgen.Constraints;
import dmu.project.levelgen.GAPopulationGen;
import dmu.project.levelgen.HeightMap;
import dmu.project.levelgen.MapCandidate;
import dmu.project.levelgen.Tile;
import dmu.project.levelgen.exceptions.LevelGenerationException;
import dmu.project.weather.WeatherClient;
import dmu.project.weather.WeatherResponse;

/**
 * Implementation of the LibGDX Screen class.
 * Responsible for rendering the generated level for demonstration of prototype.
 * <p>
 * Created by Dom on 18/11/2016.
 */

public class LevelGenScreen implements Screen {
    private static int sWidth = 80, sHeight = 50;
    private final MyGdxGame mGame;
    private OrthographicCamera mCamera;
    private MapBuilder.Map mMap;
    private OrthogonalTiledMapRenderer mRenderer = null;
    private int mNoiseWidth, mNoiseHeight, mDifficulty, mTileWidth = 16, mTileHeight = 16;
    private final static long DEBUG_SEED = -2656433763347937011L;
    private boolean mDebugEnabled;
    private LevelSelectUI mUi;
    private List<MapCandidate> mMapCandidates;
    private HeightMap mHeightMap;
    private WeatherResponse mWeather = null;
    private InputMultiplexer mInputMultiplexer = new InputMultiplexer();
    private Player mPlayer;
    private Controller mController;
    private GestureDetector mMapCameraController;
    private float mScaleX, mScaleY;
    private List<Enemy> mEnemies = new ArrayList<>();
    private GameState mState = GameState.LEVEL_SELECT;
    private Stopwatch mTimer;
    private int mMapIndex = 0, mLastMapIndex = 0;
    GameUI mGameUI;

    /**
     * Constructor
     *
     * @param mGame         The main mGame object.
     * @param mNoiseWidth   The sWidth of the noise sample grid for the Perlin level generator.
     * @param mNoiseHeight  The sHeight of the noise sample grid for the Perlin level generator.
     * @param mDifficulty   The mDifficulty of the levels to generate.
     * @param mDebugEnabled Whether to use the debug seed or not.
     */
    public LevelGenScreen(MyGdxGame mGame, int mNoiseWidth, int mNoiseHeight, int mDifficulty, boolean mDebugEnabled) {
        this.mGame = mGame;
        this.mNoiseWidth = mNoiseWidth;
        this.mNoiseHeight = mNoiseHeight;
        this.mDifficulty = mDifficulty;
        this.mDebugEnabled = mDebugEnabled;
        sWidth = mGame.mProperties.get("constraints.mapWidth") != null ? Integer.parseInt(mGame.mProperties.get("constraints.mapWidth")) : 80;
        sHeight = mGame.mProperties.get("constraints.mapHeight") != null ? Integer.parseInt(mGame.mProperties.get("constraints.mapHeight")) : 50;
        this.mCamera = new OrthographicCamera();
        resetCamera();
        init();
    }


    /**
     * Switch the mMap with the specified index.
     *
     * @param index The index of the mMap to switch to.
     */
    void switchMap(int index) {
        if (index > 9 || index >= mMapCandidates.size())
            index = Math.min(9, mMapCandidates.size() - 1);
        List<Tile> tileList = getMapCandidates().get(index).tileSet;
        mLastMapIndex = mMapIndex;
        mMapIndex = index;
        mMap = MapBuilder.buildMap(sWidth, sHeight, mTileWidth, mTileHeight, mHeightMap, tileList, mWeather);
        if (mRenderer != null)
            mRenderer.setMap(mMap.tiledMap);
        else
            mRenderer = new OrthogonalTiledMapRenderer(this.mMap.tiledMap, mGame.mBatch);
        if (mPlayer != null)
            mPlayer.setTileList(tileList);
        mRenderer.setView(mCamera);
    }

    /**
     * Switch the next mMap.
     */
    void switchNextMap() {
        int minIndex = Math.min(9, mMapCandidates.size() - 1);
        mLastMapIndex = mMapIndex;
        mMapIndex = mMapIndex != minIndex ? mMapIndex + 1 : 0;
        List<Tile> tileList = getMapCandidates().get(mMapIndex).tileSet;
        mMap = MapBuilder.buildMap(sWidth, sHeight, mTileWidth, mTileHeight, mHeightMap, tileList, mWeather);
        if (mRenderer != null)
            mRenderer.setMap(mMap.tiledMap);
        else
            mRenderer = new OrthogonalTiledMapRenderer(this.mMap.tiledMap, mGame.mBatch);
        if (mPlayer != null)
            mPlayer.setTileList(tileList);
        mRenderer.setView(mCamera);
    }

    /**
     * Switch the previous mMap.
     */
    void switchPreviousMap() {
        int minIndex = Math.min(9, mMapCandidates.size() - 1);
        mLastMapIndex = mMapIndex;
        mMapIndex = mMapIndex == 0 ? minIndex : mMapIndex - 1;
        List<Tile> tileList = getMapCandidates().get(mMapIndex).tileSet;
        mMap = MapBuilder.buildMap(sWidth, sHeight, mTileWidth, mTileHeight, mHeightMap, tileList, mWeather);
        if (mRenderer != null)
            mRenderer.setMap(mMap.tiledMap);
        else
            mRenderer = new OrthogonalTiledMapRenderer(this.mMap.tiledMap, mGame.mBatch);
        if (mPlayer != null)
            mPlayer.setTileList(tileList);
        mRenderer.setView(mCamera);
    }

    /**
     * Method called when a level is selected. Starts the mGame play.
     */
    void playMap() {
        mState = GameState.PLAYING;
        int objectiveCount = 0;
        TextureAtlas enemyAtlas = new TextureAtlas(Gdx.files.internal("sprites/enemy.atlas"));
        resetGrid(mLastMapIndex);
        List<Tile> tileList = getMapCandidates().get(mMapIndex).tileSet;
        Vector2 startPos = new Vector2();
        for (Tile tile : tileList) {
            switch (tile.tileState) {
                case START:
                    startPos = new Vector2(tile.position[0], tile.position[1]);
                    break;
                case END:
                    break;
                case ITEM:
                    mHeightMap.grid.getNode(tile.position[0], tile.position[1]).walkable = false;
                    break;
                case OBSTACLE:
                    mHeightMap.grid.getNode(tile.position[0], tile.position[1]).walkable = false;
                    break;
                case ENEMY:
                    ((TiledMapTileLayer) mMap.tiledMap.getLayers().get(1)).setCell(tile.position[0], tile.position[1], null);
                    mEnemies.add(new Enemy(mGame.mBatch, mHeightMap.grid, enemyAtlas, new Vector2(tile.position[0] * mTileWidth, tile.position[1] * mTileHeight)));
                    break;
                case OBJECTIVE:
                    objectiveCount++;
                    mHeightMap.grid.getNode(tile.position[0], tile.position[1]).walkable = false;
                    break;
            }
        }
        if (mGameUI != null) mInputMultiplexer.removeProcessor(mGameUI.getStage());
        mCamera.zoom = 0.25f;
        mPlayer = new Player(this, mGame.mBatch, mHeightMap.grid, tileList, mMap, 10);
        mPlayer.setPosition(startPos);
        mController = new Controller(mGame.mBatch, mPlayer);
        mGameUI = new GameUI(mGame, this, mGame.mBatch, objectiveCount, mPlayer.getHP());
        mInputMultiplexer.removeProcessor(mUi.getStage());
        mInputMultiplexer.removeProcessor(mMapCameraController);
        mInputMultiplexer.addProcessor(mController.getStage());
        mInputMultiplexer.addProcessor(mController);
    }


    /**
     * Called when the screen is first shown.
     * Construct the UI and Controller.
     */
    @Override
    public void show() {
        mUi = new LevelSelectUI(mGame, this);
        mInputMultiplexer.addProcessor(mUi.getStage());
        switchMap(mMapIndex);
        mPlayer = new Player(this, mGame.mBatch, mHeightMap.grid, mMapCandidates.get(0).tileSet, mMap, 10);
        mController = new Controller(mGame.mBatch, mPlayer);
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
        //Update the mCamera
        mCamera.update();
        //Render the mMap
        mRenderer.setView(mCamera);
        mRenderer.render();
        if (delta > 0.01667) delta = 0.0166f; //Limit the time step.
        if (mMap.particleEffect != null) { //If there's an active particle effect, update and render it.
            mMap.particleEffect.update(delta);
            mGame.mBatch.setProjectionMatrix(mCamera.combined);
            mGame.mBatch.begin();
            mMap.particleEffect.draw(mGame.mBatch);
            mGame.mBatch.end();
        }
        switch (mState) {
            case PLAYING: {
                if (mPlayer.getHP() <= 0) {
                    mState = GameState.DEAD;
                    mInputMultiplexer.removeProcessor(mController.getStage());
                    mInputMultiplexer.removeProcessor(mController);
                    mGameUI.setGameOverText("You died.");
                    mTimer = Stopwatch.createStarted();
                }
                if (mGameUI.getNumOfObjectives() <= 0) {
                    mState = GameState.LEVEL_COMPLETE;
                    mGameUI.setGameOverText("You win!");
                    mGameUI.switchToWinUI();
                    mInputMultiplexer.removeProcessor(mController.getStage());
                    mInputMultiplexer.removeProcessor(mController);
                    mInputMultiplexer.addProcessor(mGameUI.getStage());
                }
                mCamera.position.set(mPlayer.mPosition.x, mPlayer.mPosition.y, 0);
                fixCamBounds();
                mPlayer.update(delta, mEnemies);
                Iterator<Enemy> enemyIterator = mEnemies.iterator();
                while (enemyIterator.hasNext()) {
                    Enemy next = enemyIterator.next();
                    next.update(delta, mPlayer);
                    next.render(delta, mCamera);
                }
                mPlayer.render(delta, mCamera);
                mController.draw(delta);
                mGameUI.draw();
                break;
            }
            case LEVEL_SELECT:
                mUi.draw();
                break;
            case DEAD: {
                if (mTimer.elapsed(TimeUnit.SECONDS) > 6) {
                    mTimer.stop();
                    mTimer.reset();
                    mState = GameState.LEVEL_SELECT;
                    resetCamera();
                    mMapCameraController = new GestureDetector(new CameraController2D(mCamera, Math.min(mScaleX, mScaleY), mScaleX, mScaleY));
                    mInputMultiplexer.addProcessor(mMapCameraController);
                    show();
                }
                for (Enemy enemy : mEnemies) {
                    enemy.render(delta, mCamera);
                }
                mController.draw(delta);
                mGameUI.draw();
                break;
            }
            case LEVEL_COMPLETE:
                for (Enemy enemy : mEnemies) {
                    enemy.render(delta, mCamera);
                }
                mPlayer.render(delta, mCamera);
                mGameUI.draw();
                break;
        }
    }

    @Override
    public void resize(int width, int height) {
        mCamera.viewportWidth = width;
        mCamera.viewportHeight = height;
        mCamera.update();
        if (mUi != null) mUi.resize(width, height);
        if (mGameUI != null) mGameUI.resize(width, height);
        if (mController != null) mController.resize(width, height);
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
        if (mMap != null) mMap.dispose();
        if (mUi != null) mUi.dispose();
        if (mPlayer != null) mPlayer.dispose();
        if (mGameUI != null) mGameUI.dispose();
        if (mController != null) mController.dispose();
    }

    public List<MapCandidate> getMapCandidates() {
        return mMapCandidates;
    }

    public int getMapIndex() {
        return mMapIndex;
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
        mScaleX = (float) sWidth / (Gdx.graphics.getWidth() / mTileWidth);
        mScaleY = (float) sHeight / (Gdx.graphics.getHeight() / mTileWidth);
        WeatherClient weatherClient = new WeatherClient(mGame.mApiUrl, mGame.mApiKey);
        double[] latLong = mGame.getLocationService().getLatLong();
        if (latLong != null)
            mWeather = weatherClient.getWeather(latLong[0], latLong[1]);
        //Set level constraints
        Constraints constraints = readConstraints(mGame.mProperties);
        //Generate Level
        GAPopulationGen populationGen = new GAPopulationGen(constraints, mWeather);
        try {
            mMapCandidates = populationGen.populate();
        } catch (LevelGenerationException e) {
            Gdx.app.error("Level Creation", "Unrecoverable exception thrown.", e);
            mGame.returnToMenu(); //Go back to main menu.
        }
        mHeightMap = populationGen.getHeightMap();
        mMap = MapBuilder.buildMap(sWidth, sHeight, mTileWidth, mTileHeight, mHeightMap, mMapCandidates.get(0).tileSet, mWeather);
        CameraController2D cameraInputController = new CameraController2D(mCamera, Math.min(mScaleX, mScaleY), mScaleX, mScaleY);
        mMapCameraController = new GestureDetector(cameraInputController);
        mInputMultiplexer.addProcessor(mMapCameraController);
        Gdx.input.setInputProcessor(mInputMultiplexer);
        return true;
    }

    /**
     * Utility method to read the mGame's configuration.
     *
     * @param properties A mMap of name value pairs.
     * @return Returns the constraints with the values from the mProperties.
     */
    private Constraints readConstraints(ObjectMap<String, String> properties) {
        Constraints constraints = new Constraints();
        String value = properties.get("constraints.populationSize", "100");
        constraints.setPopulationSize(Integer.valueOf(value));
        value = properties.get("constraints.maxGenerations", "50");
        constraints.setMaxGenerations(Integer.valueOf(value));
        constraints.setMapHeight(sHeight);
        constraints.setMapWidth(sWidth);
        constraints.setNoiseWidth(mNoiseWidth);
        constraints.setNoiseHeight(mNoiseHeight);
        value = properties.get("constraints.objectivesEnabled", "true");
        constraints.setObjectivesEnabled(Boolean.parseBoolean(value));
        constraints.setDifficulty(mDifficulty);
        if (mDebugEnabled)
            constraints.setSeed(DEBUG_SEED);
        return constraints;
    }

    /**
     * Utility method to constrain the mCamera to the scene.
     */
    private void fixCamBounds() {
        float scrollLimitX = mCamera.viewportWidth * mScaleX;
        float scrollLimitY = mCamera.viewportHeight * mScaleY;
        //Constrain mCamera from scrolling outside of mMap
        if (mCamera.position.x - (mCamera.viewportWidth * mCamera.zoom) / 2 < 0)
            mCamera.position.x = (mCamera.viewportWidth * mCamera.zoom) / 2;
        else if (mCamera.position.x + (mCamera.viewportWidth * mCamera.zoom) / 2 > scrollLimitX)
            mCamera.position.x = scrollLimitX - (mCamera.viewportWidth * mCamera.zoom) / 2;
        if (mCamera.position.y - (mCamera.viewportHeight * mCamera.zoom) / 2 < 0)
            mCamera.position.y = (mCamera.viewportHeight * mCamera.zoom) / 2;
        else if (mCamera.position.y + (mCamera.viewportHeight * mCamera.zoom) / 2 > scrollLimitY)
            mCamera.position.y = scrollLimitY - (mCamera.viewportHeight * mCamera.zoom) / 2;
        mCamera.update();
    }

    /**
     * Utility method to reset the mCamera to default mPosition.
     */
    private void resetCamera() {
        if (mCamera != null) {
            this.mCamera.viewportWidth = Gdx.graphics.getWidth();
            this.mCamera.viewportHeight = Gdx.graphics.getHeight();
            this.mCamera.position.set(mCamera.viewportWidth / 2.f, mCamera.viewportHeight / 2.f, 0);
            this.mCamera.update();
        }
    }

    /**
     * Reset the mGame grid.
     *
     * @param lastMapIndex Index of the mLast mMap used.
     */
    private void resetGrid(int lastMapIndex) {
        List<Tile> tileList = getMapCandidates().get(lastMapIndex).tileSet;
        for (Tile tile : tileList) {
            mHeightMap.grid.getNode(tile.position[0], tile.position[1]).walkable = true;
        }
    }


    /**
     * Enum specifying the mGame mState.
     */
    private enum GameState {
        PLAYING,
        LEVEL_SELECT,
        DEAD,
        LEVEL_COMPLETE
    }
}

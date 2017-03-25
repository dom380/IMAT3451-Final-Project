package dmu.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;

import java.io.IOException;

/**
 * The main game class.
 */
public class MyGdxGame extends Game {
    private BitmapFont mFont;
    private MainMenuScreen mMainMenuScreen;
    private LocationService mLocationService;
    SpriteBatch mBatch;
    String mApiUrl;
    String mApiKey;
    ObjectMap<String, String> mProperties = new ObjectMap<>();

    /**
     * Constructor.
     *
     * @param mLocationService Implementation of the location service.
     * @param mApiUrl          The Weather API URL.
     * @param mApiKey          The Weather API key.
     */
    MyGdxGame(LocationService mLocationService, String mApiUrl, String mApiKey) {
        this.mLocationService = mLocationService;
        this.mApiUrl = mApiUrl;
        this.mApiKey = mApiKey;
    }

    /**
     * Initialisation method. Called on start up.
     */
    @Override
    public void create() {
        mBatch = new SpriteBatch();
        mFont = new BitmapFont();
        mMainMenuScreen = new MainMenuScreen(this);
        try {
            FileHandle configFile = Gdx.files.internal("config.mProperties");
            if (configFile.exists())
                PropertiesUtils.load(mProperties, configFile.reader());
            else setDefaultProperties(mProperties);
        } catch (IOException e) {
            Gdx.app.log("Error", e.getMessage());
            setDefaultProperties(mProperties);
        }
        this.setScreen(mMainMenuScreen);
    }

    /**
     * Disposes the current screen and switches to the main menu.
     */
    public void returnToMenu() {
        if (this.getScreen() != null)
            this.getScreen().dispose();

        mMainMenuScreen = new MainMenuScreen(this);
        this.setScreen(mMainMenuScreen);
    }

    /**
     * Disposes the current screen and switches to the specified one.
     *
     * @param screen The screen to switch to.
     */
    public void switchScreen(Screen screen) {
        if (this.getScreen() != null)
            this.getScreen().dispose();
        this.setScreen(screen);
    }

    /**
     * @return the location service.
     */
    public LocationService getLocationService() {
        return mLocationService;
    }

    public void render() {
        super.render();
    }

    public void dispose() {
        mBatch.dispose();
        mFont.dispose();
        mMainMenuScreen.dispose();
        this.getScreen().dispose();
    }

    /**
     * Sets the default level generation mProperties.
     *
     * @param properties The object to hold the mProperties.
     */
    private void setDefaultProperties(ObjectMap<String, String> properties) {
        properties.put("constraints.populationSize", "100");
        properties.put("constraints.maxGenerations", "50");
        properties.put("constraints.mapWidth", "80");
        properties.put("constraints.mapHeight", "50");
        properties.put("constraints.objectivesEnabled", "true");
        properties.put("constraints.difficulty", "5");
        properties.put("constraints.noiseWidth", "4");
        properties.put("constraints.noiseHeight", "4");
    }
}

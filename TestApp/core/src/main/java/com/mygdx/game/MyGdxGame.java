package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PropertiesUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class MyGdxGame extends Game {
    SpriteBatch batch;
    private BitmapFont font;
    private MainMenuScreen mainMenuScreen;
    private LocationService locationService;
    String apiUrl;
    String apiKey;

    ObjectMap<String, String> properties = new ObjectMap<>();

    MyGdxGame(LocationService locationService, String apiUrl, String apiKey) {
        this.locationService = locationService;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        mainMenuScreen = new MainMenuScreen(this);
        try {
            FileHandle configFile = Gdx.files.internal("config.properties");
            if (configFile.exists())
                PropertiesUtils.load(properties, configFile.reader());
            else setDefaultProperties(properties);
        } catch (IOException e) {
            Gdx.app.log("Error", e.getMessage());
            setDefaultProperties(properties);
        }
        this.setScreen(mainMenuScreen);
    }

    public void returnToMenu() {
        if (this.getScreen() != null)
            this.getScreen().dispose();

        mainMenuScreen = new MainMenuScreen(this);
        this.setScreen(mainMenuScreen);
    }

    public void switchScreen(Screen screen) {
        if (this.getScreen() != null)
            this.getScreen().dispose();
        this.setScreen(screen);
    }

    public LocationService getLocationService() {
        return locationService;
    }

    public void render() {
        super.render();
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
        mainMenuScreen.dispose();
        this.getScreen().dispose();
    }

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

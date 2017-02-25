package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MyGdxGame extends Game {
    SpriteBatch batch;
    private BitmapFont font;
    private MainMenuScreen mainMenuScreen;
    private LocationService locationService;
    String apiUrl;
    String apiKey;

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
        this.setScreen(mainMenuScreen);
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
}

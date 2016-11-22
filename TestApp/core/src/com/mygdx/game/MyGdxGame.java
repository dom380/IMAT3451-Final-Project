package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MyGdxGame extends Game {
	SpriteBatch batch;
	BitmapFont font;
	MainMenuScreen mainMenuScreen;

	@Override
	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		mainMenuScreen = new MainMenuScreen(this);
		this.setScreen(mainMenuScreen);
	}

	public void render () {
		super.render();
	}

	public void dispose () {
		batch.dispose();
		font.dispose();
		mainMenuScreen.dispose();
	}
}

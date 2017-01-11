package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Very basic menu screen
 *
 * Created by Dom on 18/11/2016.
 */

public class MainMenuScreen implements Screen {

    private final MyGdxGame game;
    private OrthographicCamera camera;
    protected Stage stage;
    private Viewport viewport;
    private TextureAtlas atlas;
    protected Skin skin;

    public MainMenuScreen(MyGdxGame game){
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false,800,480);
        atlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("uiskin.json"),atlas);
        viewport = new StretchViewport(800,480);
        viewport.apply();
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();

        stage = new Stage(viewport, game.batch);

        //Stage should controll input:
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {
        //Create Table
        Table mainTable = new Table();
        //Set table to fill stage
        mainTable.setFillParent(true);
        //Set alignment of contents in the table.
        mainTable.center();
        Label noiseWidthLabel = new Label("Noise Width:", skin);
        final TextField noiseWidthField = new TextField("",skin);
        noiseWidthField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        Label noiseHeightLabel = new Label("Noise Height:", skin);
        final TextField noiseHeightField = new TextField("",skin);
        noiseHeightField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        mainTable.add(noiseWidthLabel);
        mainTable.add(noiseWidthField);
        mainTable.row();
        mainTable.add(noiseHeightLabel);
        mainTable.add(noiseHeightField);
        mainTable.row();

        //Add buttons to table
        TextButton playButton = new TextButton("Generate Level", skin);
        playButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int noiseWidth = noiseWidthField.getText().isEmpty() ? 1 : Integer.parseInt(noiseWidthField.getText());
                int noiseHeight = noiseHeightField.getText().isEmpty() ? 1 : Integer.parseInt(noiseHeightField.getText());
                ((Game)Gdx.app.getApplicationListener()).setScreen(new LevelGenScreen(game, noiseWidth,noiseHeight));

            }
        });
        mainTable.row();
        mainTable.add(playButton);
        mainTable.row();
        //Add table to stage
        stage.addActor(mainTable);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.1f, .12f, .16f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
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
        stage.dispose();
        skin.dispose();
    }
}

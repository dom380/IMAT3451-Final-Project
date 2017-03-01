package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Very basic menu screen
 * <p>
 * Created by Dom on 18/11/2016.
 */

public class MainMenuScreen implements Screen {

    private final MyGdxGame game;
    private OrthographicCamera camera;
    private Stage stage;
    private Viewport viewport;
    private TextureAtlas atlas;
    private Skin skin;

    public MainMenuScreen(MyGdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        atlas = new TextureAtlas(Gdx.files.internal("sprites/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("sprites/uiskin.json"), atlas);
        viewport = new StretchViewport(800, 480);
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
        Label difficultyLabel = new Label("Difficulty", skin);
        final Label difficultyValue = new Label("1.0", skin);
        mainTable.add(difficultyLabel);
        final Slider difficultySlider = new Slider(1.0f, 10.0f, 1.0f, false, skin);
        difficultySlider.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                difficultyValue.setText(Float.toString(difficultySlider.getValue()));
            }
        });
        mainTable.add(difficultySlider);
        mainTable.add(difficultyValue);
        mainTable.row();
        Label noiseWidthLabel = new Label("Noise Width:", skin);
        final TextField noiseWidthField = new TextField("", skin);
        noiseWidthField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        Label noiseHeightLabel = new Label("Noise Height:", skin);
        final TextField noiseHeightField = new TextField("", skin);
        noiseHeightField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        mainTable.add(noiseWidthLabel);
        mainTable.add(noiseWidthField);
        mainTable.row();
        mainTable.add(noiseHeightLabel);
        mainTable.add(noiseHeightField);
        mainTable.row();

        //Add buttons to table
        final CheckBox debugButton = new CheckBox("Debug Seed", skin);
        TextButton playButton = new TextButton("Generate Level", skin);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int diff = (int) difficultySlider.getValue();
                int noiseWidth = noiseWidthField.getText().isEmpty() ? 1 : Integer.parseInt(noiseWidthField.getText());
                int noiseHeight = noiseHeightField.getText().isEmpty() ? 1 : Integer.parseInt(noiseHeightField.getText());
                boolean debugEnabled = debugButton.isChecked();
                ((Game) Gdx.app.getApplicationListener()).setScreen(new LoadingScreen(game, noiseWidth, noiseHeight, diff, debugEnabled));

            }
        });
        mainTable.row();
        mainTable.add(playButton);
        mainTable.row();
        mainTable.add(debugButton);
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
        atlas.dispose();
    }
}

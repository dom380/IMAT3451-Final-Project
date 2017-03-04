package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Dom on 27/02/2017.
 */

public class LevelSelectUI {
    private OrthographicCamera camera;
    private Stage stage;
    private Viewport viewport;
    private TextureAtlas atlas;
    private Skin skin;
    private SpriteBatch batch;
    private int currentIndx = 0;


    public LevelSelectUI(final MyGdxGame game, final LevelGenScreen screen) {
        camera = new OrthographicCamera();
        atlas = new TextureAtlas(Gdx.files.internal("sprites/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("sprites/uiskin.json"), atlas);
        viewport = new StretchViewport(800, 480, camera);
        viewport.apply();
        batch = game.batch;
        stage = new Stage(viewport, batch);

        Table table = new Table();
        table.setFillParent(true);
        table.left().bottom();
        table.pad(0.0f, 0.0f, 5.0f, 0.0f);

        final Label mapLabel = new Label("Map 1/10", skin);
        TextButton previousButton = new TextButton("Previous Map", skin);
        previousButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentIndx = currentIndx != 0 ? currentIndx - 1 : 9;
                mapLabel.setText("Map " + (currentIndx + 1) + "/10");
                screen.switchMap(screen.getMapCandidates().get(currentIndx).tileSet);
            }
        });
        TextButton nextButton = new TextButton("Next Map", skin);
        nextButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                currentIndx = currentIndx != 9 ? currentIndx + 1 : 0;
                mapLabel.setText("Map " + (currentIndx + 1) + "/10");
                screen.switchMap(screen.getMapCandidates().get(currentIndx).tileSet);
            }
        });
//        nextButton.pad(0.0f,5.0f,0.0f,0.0f);
        TextButton menuButton = new TextButton("Main Menu", skin);
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.returnToMenu();
            }
        });
        table.add(menuButton).pad(0.0f, 5.0f, 0.0f, 0.0f);
        table.add(previousButton).pad(0.0f, 5.0f, 0.0f, 0.0f);
        table.add();
        table.add(nextButton).pad(0.0f, 5.0f, 0.0f, 0.0f);
        table.add();
        table.add(mapLabel).pad(0.0f, 5.0f, 0.0f, 0.0f);
        stage.addActor(table);
    }

    public void draw() {
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
        atlas.dispose();
        skin.dispose();
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public Stage getStage() {
        return stage;
    }
}


package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Dom on 08/03/2017.
 */

public class GameUI {
    private OrthographicCamera camera;
    private Stage stage;
    private Viewport viewport;
    private TextureAtlas atlas;
    private Skin skin;
    private SpriteBatch batch;
    private Label beaconLabel, hpLabel;
    private int numOfObjectives, hp;

    public GameUI(SpriteBatch batch, int numOfObjectives, int playerHP) {
        this.batch = batch;
        this.numOfObjectives = numOfObjectives;
        this.hp = playerHP;
        camera = new OrthographicCamera();
        atlas = new TextureAtlas(Gdx.files.internal("sprites/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("sprites/uiskin.json"), atlas);
        viewport = new StretchViewport(800, 480, camera);
        viewport.apply();
        stage = new Stage(viewport, batch);

        Table table = new Table();
        table.setFillParent(true);
        table.top();

        beaconLabel = new Label("Beacons to light: " + numOfObjectives, skin);
        hpLabel = new Label("HP: " + hp, skin);

        table.add(beaconLabel).padLeft(25.0f).padRight(25.0f).left();
        table.add(hpLabel).padLeft(25.0f).padRight(25.0f);

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

    public void updateObjectiveCount(){
        numOfObjectives--;
        beaconLabel.setText("Beacons to light: " + numOfObjectives);
    }

    public void updateHP(int amount){
        hp += amount;
        hpLabel.setText("HP: " + hp);
    }

}

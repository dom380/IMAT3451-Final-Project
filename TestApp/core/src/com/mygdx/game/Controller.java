package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Dom on 03/03/2017.
 */

public class Controller {
    private OrthographicCamera camera;
    private Stage stage;
    private Viewport viewport;
    private TextureAtlas atlas;
    private Skin skin;
    private SpriteBatch batch;
    private Skin touchpadSkin;
    private Player player;

    public Controller(SpriteBatch batch, final Player player) {
        this.player = player;
        camera = new OrthographicCamera();
        atlas = new TextureAtlas(Gdx.files.internal("sprites/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("sprites/uiskin.json"), atlas);
        viewport = new StretchViewport(800, 480, camera);
        viewport.apply();
        this.batch = batch;
        stage = new Stage(viewport, batch);
        Table table = new Table();
        table.setFillParent(true);
        table.left().center();
        table.pad(0.0f, 0.0f, 5.0f, 0.0f);

        touchpadSkin = new Skin();
        touchpadSkin.add("touchBackground", new Texture("controller/joystickBackground.png"));
        touchpadSkin.add("touchKnob", new Texture("controller/joystickKnob.png"));
        Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
        touchpadStyle.background = touchpadSkin.getDrawable("touchBackground");
        touchpadStyle.background.setMinHeight(50);
        touchpadStyle.background.setMinWidth(50);
        touchpadStyle.knob = touchpadSkin.getDrawable("touchKnob");
        touchpadStyle.knob.setMinHeight(35);
        touchpadStyle.knob.setMinWidth(35);
        Touchpad touchpad = new Touchpad(10, touchpadStyle);
        touchpad.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Touchpad tp = (Touchpad) actor;
                float dx = tp.getKnobPercentX();
                float dy = tp.getKnobPercentY();
                if (dx == 0.0 && dy == 0.0) {
                    player.setMoving(null);
                    return;
                }
                double angle = Math.atan2(dy, dx);
                int quad = (int) ((4 * angle / (2 * Math.PI) + 4.5) % 4); //get the nearest 4-direction
                if (quad == 0) { //Right
                    dy = 0.0f;
                    dx = 1.0f;
                } else if (quad == 1) { //Up
                    dy = 1.0f;
                    dx = 0.0f;
                } else if (quad == 2) { //Left
                    dy = 0.0f;
                    dx = -1.0f;
                } else if (quad == 3) { //Down
                    dy = -1.0f;
                    dx = 0.0f;
                }

                player.setMoving(new Vector2(dx, dy));
            }
        });
        table.add(touchpad).width(75).height(75).pad(0.0f, 0.0f, 5.0f, 0.0f).fill();
        stage.addActor(table);
    }

    public void draw(float delta) {
        stage.act(delta);
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
        atlas.dispose();
        skin.dispose();
        touchpadSkin.dispose();
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public Stage getStage() {
        return stage;
    }

}

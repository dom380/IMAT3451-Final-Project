package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import dmu.project.utils.Grid;

/**
 * Created by Dom on 03/03/2017.
 */

public class Player extends Movable {

    private float animTime = 0.0f;
    private Animation idle, run, up, down;
    private TextureAtlas textureAtlas;
    private SpriteBatch spriteBatch;
    private Vector2 tilePos, direction;
    private boolean flip = false;

    private static float TILE_WIDTH = 16.0f;
    private static float TILE_HEIGHT = 16.0f;

    private GridMovement gridMovement;

    public Player(SpriteBatch batch, Grid grid) {
        super(new Vector2(20 * TILE_WIDTH, 20 * TILE_HEIGHT), new Vector2());
        spriteBatch = batch;
        textureAtlas = new TextureAtlas(Gdx.files.internal("sprites/pc.atlas"));
        idle = new Animation(0.33f, textureAtlas.findRegions("erika_idle"));
        idle.setPlayMode(Animation.PlayMode.LOOP);
        run = new Animation(0.15f, textureAtlas.findRegions("erika_run"));
        run.setPlayMode(Animation.PlayMode.LOOP);
        up = new Animation(0.15f, textureAtlas.findRegions("erika_up"));
        up.setPlayMode(Animation.PlayMode.LOOP);
        down = new Animation(0.15f, textureAtlas.findRegions("erika_down"));
        down.setPlayMode(Animation.PlayMode.LOOP);
        tilePos = new Vector2(20, 20);

        gridMovement = new GridMovement(this, grid);
        gridMovement.setSpeed(new Vector2(75f, 75f));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        gridMovement.update(delta);
    }

    public void render(float delta, Camera camera) {
        animTime += delta;
        TextureRegion keyFrame = null;
        //set animation
        if (!gridMovement.isMoving()) { //If not moving, use idle
            keyFrame = idle.getKeyFrame(animTime);
        } else { //Else character still moving
            if (direction.x < 0.0) {  //Check last known direction
                keyFrame = run.getKeyFrame(animTime);
                flip = false;
            } else if (direction.x > 0.0) {
                keyFrame = run.getKeyFrame(animTime);
                flip = true;
            } else if (direction.y > 0.0) {
                keyFrame = up.getKeyFrame(animTime);
            } else if (direction.y < 0.0) {
                keyFrame = down.getKeyFrame(animTime);
            }
        }
        if (keyFrame != null) { //Render the animation frame
            spriteBatch.begin();
            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.draw(keyFrame, flip ? (position.x) + keyFrame.getRegionWidth() : position.x, position.y, flip ? -keyFrame.getRegionWidth() : keyFrame.getRegionWidth(), keyFrame.getRegionHeight());
            spriteBatch.end();
        }
        if (animTime > 1.0f) animTime = 0.0f;
    }

    public void setMoving(Vector2 dir) {
        if (dir != null) //Controller passed a direction
            direction = dir; //Store for animation reference.

        gridMovement.setDirection(dir); //Pass direction to movement controller.
    }

    public void dispose() {
        this.textureAtlas.dispose();
    }

}

package com.mygdx.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import dmu.project.utils.Grid;

/**
 * Created by Dom on 08/03/2017.
 */

public class Enemy extends TileMovable {

    private Animation idle, run, up, down;
    private TextureAtlas textureAtlas;
    private SpriteBatch spriteBatch;
    private float animTime = 0.0f;
    private boolean flip = false;

    public Enemy(SpriteBatch batch, Grid grid, TextureAtlas textureAtlas, Vector2 position) {
        super(position, new Vector2(0,0));
        spriteBatch = batch;
        this.textureAtlas = textureAtlas;
        idle = new Animation(0.15f,this.textureAtlas.findRegions("enemy_idle"));
        idle.setPlayMode(Animation.PlayMode.LOOP);
        run = new Animation(0.15f, this.textureAtlas.findRegions("enemy_run"));
        run.setPlayMode(Animation.PlayMode.LOOP);
        up = new Animation(0.15f, this.textureAtlas.findRegions("enemy_up"));
        up.setPlayMode(Animation.PlayMode.LOOP);
        down = new Animation(0.15f, this.textureAtlas.findRegions("enemy_down"));
        down.setPlayMode(Animation.PlayMode.LOOP);
        gridMovement = new GridMovement(this, grid);
        gridMovement.setSpeed(new Vector2(35f, 35f));
    }

    @Override
    public void update(float delta) {
        super.update(delta);

    }

    public void render(float delta, Camera camera){
        animTime += delta;
        //set animation
        TextureRegion keyFrame = null;
        if (!gridMovement.isMoving()) { //If not moving, use idle
            keyFrame = idle.getKeyFrame(animTime);
        } else { //Else character still moving
            if (direction.x < 0.0) {  //Check last known direction
                keyFrame = run.getKeyFrame(animTime);
            } else if (direction.x > 0.0) {
                keyFrame = run.getKeyFrame(animTime);
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
}

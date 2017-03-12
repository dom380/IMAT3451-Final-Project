package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Dom on 04/03/2017.
 */

public abstract class TileMovable {

    protected Vector2 position, velocity, last, direction;
    protected static float TILE_WIDTH = 16.0f;
    protected static float TILE_HEIGHT = 16.0f;
    protected GridMovement gridMovement;

    public TileMovable(Vector2 position, Vector2 velocity) {
        this.position = position;
        this.velocity = velocity;
        this.last = position.cpy();
    }

    public void update(float delta) {
        last = position.cpy();
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }


}

package dmu.mygdx.game;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Dom on 04/03/2017.
 * <p>
 * Base abstract class for objects that move on a grid.
 */

public abstract class TileMovable {

    protected Vector2 mPosition, mVelocity, mLast, mDirection;
    protected static float TILE_WIDTH = 16.0f;
    protected static float TILE_HEIGHT = 16.0f;
    protected GridMovement mGridMovement;

    /**
     * Constructor
     *
     * @param mPosition The position of the entity.
     * @param mVelocity The velocity of the entity.
     */
    public TileMovable(Vector2 mPosition, Vector2 mVelocity) {
        this.mPosition = mPosition;
        this.mVelocity = mVelocity;
        this.mLast = mPosition.cpy();
    }

    /**
     * Updates the entities position based on the time step.
     *
     * @param delta The current time step.
     */
    public void update(float delta) {
        mLast = mPosition.cpy();
        mPosition.x += mVelocity.x * delta;
        mPosition.y += mVelocity.y * delta;
    }

    public Vector2 getPosition() {
        return mPosition;
    }

    public Vector2 getVelocity() {
        return mVelocity;
    }

    public void setPosition(Vector2 position) {
        this.mPosition = position;
    }

    public void setVelocity(Vector2 velocity) {
        this.mVelocity = velocity;
    }


}

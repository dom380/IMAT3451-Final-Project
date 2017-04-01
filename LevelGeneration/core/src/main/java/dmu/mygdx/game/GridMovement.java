package dmu.mygdx.game;

import com.badlogic.gdx.math.Vector2;

import dmu.project.utils.Grid;

/**
 * Created by Dom on 04/03/2017.
 * Class to handle smooth mGrid based movement.
 */

public class GridMovement {

    private Vector2 mDirection, mLastMove, mDestination, mLastTile, mSpeed;
    private TileMovable mEntity;
    private Grid mGrid;
    private static final float TILE_WIDTH = 16;
    private static final float TILE_HEIGHT = 16;

    /**
     * Constructor.
     *
     * @param tileMovable The mEntity to control the movement of.
     * @param mGrid       The mGrid to move on.
     */
    public GridMovement(TileMovable tileMovable, Grid mGrid) {
        mEntity = tileMovable;
        this.mGrid = mGrid;
    }

    /**
     * Update method called every frame. Updates the entities movement.
     *
     * @param delta time step.
     */
    public void update(float delta) {
        boolean isMoving = mDestination != null;
        boolean reachedDest = reachedDest();
        //Not moving and no input, just return
        if(mDirection == null && !isMoving){
            return;
        }
        //Reached destination and no new input, so stop
        if (reachedDest && mDirection == null) {
            stopMoving();
        } // Stop moving on collision
        else if (isMoving && reachedDest && !canMoveDirectionFromTile(mDestination.x, mDestination.y, mDirection)) {
            stopMoving();
        } else if (isMoving && !reachedDest) {
            setVelocityByTile((int) mDestination.x, (int) mDestination.y);
        }
        // Dest reached with input
        else if (isMoving && reachedDest && mDirection != null && canMoveDirectionFromTile(mDestination.x, mDestination.y, mDirection)) {
            changeDirectionAndContinueMoving(mDirection);
        }
        //Start moving
        else if (!isMoving && mDirection != null) {
            Vector2 currTile = getCurrentTile();
            if (canMoveDirectionFromTile((int) currTile.x, (int) currTile.y, mDirection)) {
                startMoving(mDirection);
            }
        }
    }

    Vector2 getSpeed() {
        return mSpeed;
    }

    void setSpeed(Vector2 speed) {
        this.mSpeed = speed;
    }

    Vector2 getDirection() {
        return mDirection;
    }

    void setDirection(Vector2 direction) {
        this.mLastMove = this.mDirection;
        this.mDirection = direction != null ? direction.cpy() : null;
    }

    /////////////////////////////
    //Private Utility Methods //
    ////////////////////////////

    private Vector2 getCurrentTile() {
        float tileX = mEntity.mPosition.x / TILE_WIDTH;
        float tileY = mEntity.mPosition.y / TILE_HEIGHT;
        return new Vector2(tileX, tileY);
    }


    private Vector2 getTileAdjacentToTile(float tileX, float tileY, Vector2 direction) {
        return new Vector2(tileX + direction.x, tileY + direction.y);
    }

    private void startMoving(Vector2 direction) {
        // Get current tile mPosition.
        Vector2 currTile = getCurrentTile();
        // Get new mDestination.
        mDestination = getTileAdjacentToTile(currTile.x, currTile.y, direction);
        // Move.
        setVelocityByTile((int) mDestination.x, (int) mDestination.y);
    }

    private void changeDirectionAndContinueMoving(Vector2 newDirection) {
        // Method only called when at mDestination, so snap to it now.
        snapToTile((int) mDestination.x, (int) mDestination.y);
        // Get new mDestination.
        mDestination = getTileAdjacentToTile(mDestination.x, mDestination.y, newDirection);
        // Move.
        setVelocityByTile((int) mDestination.x, (int) mDestination.y);
    }

    private void stopMoving() {
        // Method only called when at mDestination, so snap to it now.
        snapToTile((int) mDestination.x, (int) mDestination.y);
        // We are already at the mDestination.
        mDestination = null;
        // Stop.
        mEntity.mVelocity.x = mEntity.mVelocity.y = 0;
        mDirection = null;
    }

    protected void snapToTile(int x, int y) {
        mEntity.mPosition.set(x * TILE_WIDTH, y * TILE_HEIGHT);
    }


    public boolean isMoving() {
        return mDestination != null;
    }

    public boolean reachedDest() {
        if (mDestination != null) {
            Vector2 pixelDest = new Vector2(mDestination.x * TILE_WIDTH, mDestination.y * TILE_HEIGHT);
            return pixelDest.dst(mEntity.getPosition()) < 2.f;
        } else {
            return false;
        }
    }

    private boolean canMoveDirectionFromTile(float tileX, float tileY, Vector2 direction) {
        return canMoveDirectionFromTile((int) tileX, (int) tileY, direction);
    }

    private boolean canMoveDirectionFromTile(int tileX, int tileY, Vector2 direction) {
        Vector2 newPos = getTileAdjacentToTile(tileX, tileY, direction);
        return mGrid.walkable((int) newPos.x, (int) newPos.y);
    }

    private void setVelocityByTile(int tileX, int tileY) {
        float tileCenterX = tileX * TILE_WIDTH + TILE_WIDTH / 2;
        float tileCenterY = tileY * TILE_HEIGHT + TILE_HEIGHT / 2;
        float entityCenterX = mEntity.mPosition.x + TILE_WIDTH / 2;
        float entityCenterY = mEntity.mPosition.y + TILE_HEIGHT / 2;
        mEntity.mVelocity.x = mEntity.mVelocity.y = 0;
        if (entityCenterX > tileCenterX)
            mEntity.mVelocity.x = -mSpeed.x;
        else if (entityCenterX < tileCenterX)
            mEntity.mVelocity.x = mSpeed.x;
        else if (entityCenterY > tileCenterY)
            mEntity.mVelocity.y = -mSpeed.y;
        else if (entityCenterY < tileCenterY)
            mEntity.mVelocity.y = mSpeed.y;
    }

    public Grid getGrid() {
        return mGrid;
    }

    public Vector2 getDestination() {
        return mDestination;
    }

    public void setDestination(Vector2 destination) {
        this.mDestination = destination;
    }
}

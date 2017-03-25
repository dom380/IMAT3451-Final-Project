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
     * @param mGrid        The mGrid to move on.
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
        boolean isMoving = isMoving(), reachedDest = justReachedDestination();
        // Stop if at mDestination
        if (isMoving && reachedDest && mDirection == null) {
            stopMoving();
        }
        // Stop moving on collision
        else if (isMoving && reachedDest && mDirection != null && !canMoveDirectionFromTile(mDestination.x, mDestination.y, mDirection)) {
            stopMoving();
        }
        // Dest reached, but control still pressed so continue
        else if (isMoving && reachedDest && mDirection != null && (canMoveDirectionFromTile(mDestination.x, mDestination.y, mDirection)) && mDirection.epsilonEquals(mLastMove, 0.001f)) {
            continueMovingFromDestination();
        }
        // Dest reached but changing mDirection
        else if (isMoving && mDirection != null && (canMoveDirectionFromTile(mDestination.x, mDestination.y, mDirection)) && !mDirection.epsilonEquals(mLastMove, 0.001f)) {
            changeDirectionAndContinueMoving(mDirection);
        }
        // Dest not reached, continue
        else if (isMoving && !reachedDest) {
            continueMovingToDestination();
        }
        // Not moving, start moving
        else if (!isMoving && mDirection != null && (canMoveDirectionFromCurrentTile(mDirection))) {
            startMoving(mDirection);
        }
        mLastTile = mDestination;
        mLastMove = mDirection;
    }

    void collision() {
        if (mLastTile != null)
            snapToTile((int) mLastTile.x, (int) mLastTile.y);
        mEntity.mVelocity.set(0f, 0f);
        mDirection = null;
        mDestination = null;
        mLastMove = null;
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


    private void continueMovingToDestination() {
        // Move.
        setVelocityByTile((int) mDestination.x, (int) mDestination.y);
    }


    private void continueMovingFromDestination() {
        // Get new mDestination.
        mDestination = getTileAdjacentToTile(mDestination.x, mDestination.y, mLastMove);
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

    protected boolean justReachedDestination() {
        if (mDestination == null) return false;
        float destinationX = mDestination.x * TILE_WIDTH;
        float destinationY = mDestination.y * TILE_HEIGHT;
        return (mEntity.mPosition.x >= destinationX && mEntity.mLast.x < destinationX) ||
                (mEntity.mPosition.x <= destinationX && mEntity.mLast.x > destinationX) ||
                (mEntity.mPosition.y >= destinationY && mEntity.mLast.y < destinationY) ||
                (mEntity.mPosition.y <= destinationY && mEntity.mLast.y > destinationY);

    }

    public boolean isMoving() {
        return mDestination != null;
    }

    private boolean canMoveDirectionFromTile(float tileX, float tileY, Vector2 direction) {
        return canMoveDirectionFromTile((int) tileX, (int) tileY, direction);
    }

    private boolean canMoveDirectionFromTile(int tileX, int tileY, Vector2 direction) {
        Vector2 newPos = getTileAdjacentToTile(tileX, tileY, direction);
        return mGrid.walkable((int) newPos.x, (int) newPos.y);
    }

    private boolean canMoveDirectionFromCurrentTile(Vector2 direction) {
        Vector2 currTile = getCurrentTile();
        return canMoveDirectionFromTile((int) currTile.x, (int) currTile.y, direction);
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

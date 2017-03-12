package dmu.mygdx.game;

import com.badlogic.gdx.math.Vector2;

import dmu.project.utils.Grid;

/**
 * Created by Dom on 04/03/2017.
 */

public class GridMovement {

    private Vector2 direction, lastMove, destination, lastTile, speed;
    private TileMovable entity;
    private Grid grid;
    private static final float TILE_WIDTH = 16;
    private static final float TILE_HEIGHT = 16;

    public GridMovement(TileMovable tileMovable, Grid grid) {
        entity = tileMovable;
        this.grid = grid;
    }

    public void update(float delta) {
        boolean isMoving = isMoving(), reachedDest = justReachedDestination();
        // Stop if at destination
        if (isMoving && reachedDest && direction == null) {
            stopMoving();
        }
        // Stop moving on collision
        else if (isMoving && reachedDest && direction != null && !canMoveDirectionFromTile(destination.x, destination.y, direction)) {
            stopMoving();
        }
        // Dest reached, but control still pressed so continue
        else if (isMoving && reachedDest && direction != null && (canMoveDirectionFromTile(destination.x, destination.y, direction)) && direction.epsilonEquals(lastMove, 0.001f)) {
            continueMovingFromDestination();
        }
        // Dest reached but changing direction
        else if (isMoving  && direction != null && (canMoveDirectionFromTile(destination.x, destination.y, direction)) && !direction.epsilonEquals(lastMove, 0.001f)) {
            changeDirectionAndContinueMoving(direction);
        }
        // Dest not reached, continue
        else if (isMoving && !reachedDest) {
            continueMovingToDestination();
        }
        // Not moving, start moving
        else if (!isMoving && direction != null && (canMoveDirectionFromCurrentTile(direction))) {
            startMoving(direction);
        }
        lastTile = destination;
        lastMove = direction;
    }

    void collision() {
        if (lastTile != null)
            snapToTile((int) lastTile.x, (int) lastTile.y);
        entity.velocity.set(0f, 0f);
        direction = null;
        destination = null;
        lastMove = null;
    }

    Vector2 getSpeed() {
        return speed;
    }

    void setSpeed(Vector2 speed) {
        this.speed = speed;
    }

    Vector2 getDirection() {
        return direction;
    }

    void setDirection(Vector2 direction) {
        this.direction = direction != null ? direction.cpy() : null;
    }

    private Vector2 getCurrentTile() {
        float tileX = entity.position.x / TILE_WIDTH;
        float tileY = entity.position.y / TILE_HEIGHT;
        return new Vector2(tileX, tileY);
    }


    private Vector2 getTileAdjacentToTile(float tileX, float tileY, Vector2 direction) {
        return new Vector2(tileX + direction.x, tileY + direction.y);
    }

    private void startMoving(Vector2 direction) {
        // Get current tile position.
        Vector2 currTile = getCurrentTile();
        // Get new destination.
        destination = getTileAdjacentToTile(currTile.x, currTile.y, direction);
        // Move.
        setVelocityByTile((int) destination.x, (int) destination.y);
    }


    private void continueMovingToDestination() {
        // Move.
        setVelocityByTile((int) destination.x, (int) destination.y);
    }


    private void continueMovingFromDestination() {
        // Get new destination.
        destination = getTileAdjacentToTile(destination.x, destination.y, lastMove);
        // Move.
        setVelocityByTile((int) destination.x, (int) destination.y);
    }

    private void changeDirectionAndContinueMoving(Vector2 newDirection) {
        // Method only called when at destination, so snap to it now.
        snapToTile((int) destination.x, (int) destination.y);
        // Get new destination.
        destination = getTileAdjacentToTile(destination.x, destination.y, newDirection);
        // Move.
        setVelocityByTile((int) destination.x, (int) destination.y);
    }

    private void stopMoving() {
        // Method only called when at destination, so snap to it now.
        snapToTile((int) destination.x, (int) destination.y);
        // We are already at the destination.
        destination = null;
        // Stop.
        entity.velocity.x = entity.velocity.y = 0;
        direction = null;
    }

    protected void snapToTile(int x, int y) {
        entity.position.set(x * TILE_WIDTH, y * TILE_HEIGHT);
    }

    protected boolean justReachedDestination() {
        if (destination == null) return false;
        float destinationX = destination.x * TILE_WIDTH;
        float destinationY = destination.y * TILE_HEIGHT;
        return (entity.position.x >= destinationX && entity.last.x < destinationX) ||
                (entity.position.x <= destinationX && entity.last.x > destinationX) ||
                (entity.position.y >= destinationY && entity.last.y < destinationY) ||
                (entity.position.y <= destinationY && entity.last.y > destinationY);

    }

    public boolean isMoving() {
        return destination != null;
    }

    private boolean canMoveDirectionFromTile(float tileX, float tileY, Vector2 direction) {
        return canMoveDirectionFromTile((int) tileX, (int) tileY, direction);
    }

    private boolean canMoveDirectionFromTile(int tileX, int tileY, Vector2 direction) {
        Vector2 newPos = getTileAdjacentToTile(tileX, tileY, direction);
        return grid.walkable((int) newPos.x, (int) newPos.y);
    }

    private boolean canMoveDirectionFromCurrentTile(Vector2 direction) {
        Vector2 currTile = getCurrentTile();
        return canMoveDirectionFromTile((int) currTile.x, (int) currTile.y, direction);
    }

    private void setVelocityByTile(int tileX, int tileY) {
        float tileCenterX = tileX * TILE_WIDTH + TILE_WIDTH / 2;
        float tileCenterY = tileY * TILE_HEIGHT + TILE_HEIGHT / 2;
        float entityCenterX = entity.position.x + TILE_WIDTH / 2;
        float entityCenterY = entity.position.y + TILE_HEIGHT / 2;
        entity.velocity.x = entity.velocity.y = 0;
        if (entityCenterX > tileCenterX)
            entity.velocity.x = -speed.x;
        else if (entityCenterX < tileCenterX)
            entity.velocity.x = speed.x;
        else if (entityCenterY > tileCenterY)
            entity.velocity.y = -speed.y;
        else if (entityCenterY < tileCenterY)
            entity.velocity.y = speed.y;
    }

    public Grid getGrid() {
        return grid;
    }

    public Vector2 getDestination() {
        return destination;
    }

    public void setDestination(Vector2 destination) {
        this.destination = destination;
    }
}

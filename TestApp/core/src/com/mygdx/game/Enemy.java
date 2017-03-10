package com.mygdx.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import dmu.project.levelgen.Heuristics;
import dmu.project.utils.Grid;
import dmu.project.utils.Node;
import dmu.project.utils.PathFinder;

/**
 * Created by Dom on 08/03/2017.
 */

public class Enemy extends TileMovable {

    private Animation idle, run, up, down;
    private TextureAtlas textureAtlas;
    private SpriteBatch spriteBatch;
    private float animTime = 0.0f;
    private boolean flip = false;
    private List<Node> path = null;
    private int pathIndex = 0;

    public Enemy(SpriteBatch batch, Grid grid, TextureAtlas textureAtlas, Vector2 position) {
        super(position, new Vector2(0, 0));
        spriteBatch = batch;
        this.textureAtlas = textureAtlas;
        idle = new Animation(0.15f, this.textureAtlas.findRegions("enemy_idle"));
        idle.setPlayMode(Animation.PlayMode.LOOP);
        run = new Animation(0.15f, this.textureAtlas.findRegions("enemy_run"));
        run.setPlayMode(Animation.PlayMode.LOOP);
        up = new Animation(0.15f, this.textureAtlas.findRegions("enemy_up"));
        up.setPlayMode(Animation.PlayMode.LOOP);
        down = new Animation(0.15f, this.textureAtlas.findRegions("enemy_down"));
        down.setPlayMode(Animation.PlayMode.LOOP);
        gridMovement = new GridMovement(this, grid);
        gridMovement.setSpeed(new Vector2(35f, 35f));
        direction = new Vector2(0.0f, 0.0f);
    }

    public void update(float delta, Player player) {
        super.update(delta);
        //Crappy impl
        int playerX = (int) ((int) player.position.x / TILE_WIDTH), playerY = (int) (player.position.y / TILE_HEIGHT), x = (int) (position.x / TILE_WIDTH), y = (int) (position.y / TILE_HEIGHT);
        double dist = Heuristics.manhatDist(playerX, playerY, x, y);
        if (dist <= 10) {
            int dx = playerX - x;
            int dy = playerY - y;
            if(Math.abs(dx) > Math.abs(dy)){
                direction.set((float) (1.0 * Integer.signum(dx)), 0.0f);
            } else {
                direction.set(0.0f, (float) (1.0 * Integer.signum(dy)));
            }
            gridMovement.setDirection(direction);
        }
        //Path finding impl. Doesn't work D:
//        if (path == null && dist < 10) { //If we're not already following a path and hte player is near find path to them.
//            path = PathFinder.findPathAStar(new int[]{x, y}, new int[]{playerX, playerY}, gridMovement.getGrid());
//        }
//        if (path != null) { //If path not null, follow it
//            if (gridMovement.isMoving() && gridMovement.justReachedDestination() && pathIndex < path.size()) {
//                pathIndex++;
//                if (pathIndex >= path.size()) {
//                    pathIndex = 0;
//                    path = null;
//                    gridMovement.update(delta);
//                    return;
//                }
//                Node nextNode = path.get(pathIndex);
//                int dx = (int) Math.signum(nextNode.position.getX() - x);
//                int dy = (int) Math.signum(nextNode.position.getY() - y);
//                gridMovement.setDirection(direction.set(dx, dy));
//
//            } else if(pathIndex == 0) {
//                Node nextNode = path.get(pathIndex);
//                int dx = (int) Math.signum(nextNode.position.getX() - x);
//                int dy = (int) Math.signum(nextNode.position.getY() - y);
//                gridMovement.setDirection(direction.set(dx, dy));
//                pathIndex++;
//            }
//            Node endNode = path.get(path.size() - 1);
//            if (Heuristics.manhatDist(playerX, playerY, endNode.position.getX(), endNode.position.getY()) > 5) { // Our path is out of date.
//                path = null;
//                pathIndex = 0;
//            }
//        }
        gridMovement.update(delta);
    }

    public void render(float delta, Camera camera) {
        animTime += delta;
        //set animation
        TextureRegion keyFrame = null;
        flip = direction.x > 0.0;
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

    public boolean hasPath() {
        return path != null;
    }
}

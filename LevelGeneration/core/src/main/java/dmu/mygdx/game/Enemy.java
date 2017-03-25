package dmu.mygdx.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

import dmu.project.levelgen.Heuristics;
import dmu.project.utils.Grid;
import dmu.project.utils.Node;

/**
 * Created by Dom on 08/03/2017.
 * Class to handle the games enemies.
 */

public class Enemy extends TileMovable {

    private Animation mIdle, mRun, mUp, mDown;
    private TextureAtlas mTextureAtlas;
    private SpriteBatch mSpriteBatch;
    private float mAnimTime = 0.0f;
    private boolean mFlip = false;
    private List<Node> mPath = null;

    /**
     * Constructor.
     *
     * @param batch        The sprite mBatch to draw this enemy.
     * @param grid         The grid to move on.
     * @param mTextureAtlas The texture atlas containing this enemies sprites.
     * @param position     The mPosition of the enemy.
     */
    public Enemy(SpriteBatch batch, Grid grid, TextureAtlas mTextureAtlas, Vector2 position) {
        super(position, new Vector2(0, 0));
        mSpriteBatch = batch;
        this.mTextureAtlas = mTextureAtlas;
        mIdle = new Animation(0.15f, this.mTextureAtlas.findRegions("enemy_idle"));
        mIdle.setPlayMode(Animation.PlayMode.LOOP);
        mRun = new Animation(0.15f, this.mTextureAtlas.findRegions("enemy_run"));
        mRun.setPlayMode(Animation.PlayMode.LOOP);
        mUp = new Animation(0.15f, this.mTextureAtlas.findRegions("enemy_up"));
        mUp.setPlayMode(Animation.PlayMode.LOOP);
        mDown = new Animation(0.15f, this.mTextureAtlas.findRegions("enemy_down"));
        mDown.setPlayMode(Animation.PlayMode.LOOP);
        mGridMovement = new GridMovement(this, grid);
        mGridMovement.setSpeed(new Vector2(35f, 35f));
        mDirection = new Vector2(0.0f, 0.0f);
    }

    /**
     * Updates the enemies AI and movement.
     *
     * @param delta  Time step.
     * @param player The player, used for AI calculations.
     */
    public void update(float delta, Player player) {
        super.update(delta);
        //Just move to the player if near by.
        int playerX = (int) ((int) player.mPosition.x / TILE_WIDTH), playerY = (int) (player.mPosition.y / TILE_HEIGHT), x = (int) (mPosition.x / TILE_WIDTH), y = (int) (mPosition.y / TILE_HEIGHT);
        double dist = Heuristics.manhatDist(playerX, playerY, x, y);
        if (dist <= 10) {
            int dx = playerX - x;
            int dy = playerY - y;
            if (Math.abs(dx) > Math.abs(dy)) {
                mDirection.set((float) (1.0 * Integer.signum(dx)), 0.0f);
            } else {
                mDirection.set(0.0f, (float) (1.0 * Integer.signum(dy)));
            }
            mGridMovement.setDirection(mDirection);
        } else {
            mGridMovement.setDirection(null);
        }
        //Path finding impl. Doesn't work
//        if (mPath == null && dist < 10) { //If we're not already following a mPath and hte player is near find mPath to them.
//            mPath = PathFinder.findPathAStar(new int[]{x, y}, new int[]{playerX, playerY}, mGridMovement.getGrid());
//        }
//        if (mPath != null) { //If mPath not null, follow it
//            if (mGridMovement.isMoving() && mGridMovement.justReachedDestination() && pathIndex < mPath.size()) {
//                pathIndex++;
//                if (pathIndex >= mPath.size()) {
//                    pathIndex = 0;
//                    mPath = null;
//                    mGridMovement.update(delta);
//                    return;
//                }
//                Node nextNode = mPath.get(pathIndex);
//                int dx = (int) Math.signum(nextNode.mPosition.getX() - x);
//                int dy = (int) Math.signum(nextNode.mPosition.getY() - y);
//                mGridMovement.setDirection(mDirection.set(dx, dy));
//
//            } else if(pathIndex == 0) {
//                Node nextNode = mPath.get(pathIndex);
//                int dx = (int) Math.signum(nextNode.mPosition.getX() - x);
//                int dy = (int) Math.signum(nextNode.mPosition.getY() - y);
//                mGridMovement.setDirection(mDirection.set(dx, dy));
//                pathIndex++;
//            }
//            Node endNode = mPath.get(mPath.size() - 1);
//            if (Heuristics.manhatDist(playerX, playerY, endNode.mPosition.getX(), endNode.mPosition.getY()) > 5) { // Our mPath is out of date.
//                mPath = null;
//                pathIndex = 0;
//            }
//        }
        mGridMovement.update(delta);
    }

    /**
     * Render the enemy sprite.
     *
     * @param delta  time step.
     * @param camera the scene's camera.
     */
    public void render(float delta, Camera camera) {
        mAnimTime += delta;
        //set animation
        TextureRegion keyFrame = null;
        mFlip = mDirection.x > 0.0;
        if (!mGridMovement.isMoving()) { //If not moving, use mIdle
            keyFrame = mIdle.getKeyFrame(mAnimTime);
        } else { //Else character still moving
            if (mDirection.x < 0.0) {  //Check mLast known mDirection
                keyFrame = mRun.getKeyFrame(mAnimTime);
            } else if (mDirection.x > 0.0) {
                keyFrame = mRun.getKeyFrame(mAnimTime);
            } else if (mDirection.y > 0.0) {
                keyFrame = mUp.getKeyFrame(mAnimTime);
            } else if (mDirection.y < 0.0) {
                keyFrame = mDown.getKeyFrame(mAnimTime);
            }
        }
        if (keyFrame != null) { //Render the animation frame
            mSpriteBatch.begin();
            mSpriteBatch.setProjectionMatrix(camera.combined);
            mSpriteBatch.draw(keyFrame, mFlip ? (mPosition.x) + keyFrame.getRegionWidth() : mPosition.x, mPosition.y, mFlip ? -keyFrame.getRegionWidth() : keyFrame.getRegionWidth(), keyFrame.getRegionHeight());
            mSpriteBatch.end();
        }
        if (mAnimTime > 1.0f) mAnimTime = 0.0f;
    }

    public boolean hasPath() {
        return mPath != null;
    }
}

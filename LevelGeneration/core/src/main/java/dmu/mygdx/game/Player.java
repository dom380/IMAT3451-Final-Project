package dmu.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Stopwatch;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dmu.project.levelgen.Tile;
import dmu.project.levelgen.TileState;
import dmu.project.utils.Grid;

/**
 * Created by Dom on 03/03/2017.
 * Class representing the Player character.
 */

public class Player extends TileMovable {

    private float mAnimTime = 0.0f, mAttackAnimTime = 0.0f;
    private Animation mIdle, mRun, mUp, mDown, mAttack;
    private TextureAtlas mTextureAtlas, mAttackAtlas;
    private SpriteBatch mSpriteBatch;
    private boolean mFlip = false, mAttacking = false;
    private List<Tile> mTileList;
    private static float ATTACK_TIME = 0.25f;
    private MapBuilder.Map mMap;
    private LevelGenScreen mScreen;
    private Stopwatch mTimer;
    private int mHp;


    /**
     * Constructor.
     *
     * @param mScreen   The current mScreen.
     * @param batch     The sprite mBatch to render the player with.
     * @param grid      The grid to move the player on.
     * @param mTileList The list of all game objects in the level.
     * @param mMap      The current mMap.
     * @param mHp       The players max mHp.
     */
    public Player(LevelGenScreen mScreen, SpriteBatch batch, Grid grid, List<Tile> mTileList, MapBuilder.Map mMap, int mHp) {
        super(new Vector2(20 * TILE_WIDTH, 20 * TILE_HEIGHT), new Vector2());
        this.mTileList = mTileList;
        this.mScreen = mScreen;
        mSpriteBatch = batch;
        mTextureAtlas = new TextureAtlas(Gdx.files.internal("sprites/pc.atlas"));
        mAttackAtlas = new TextureAtlas(Gdx.files.internal("sprites/attack2.atlas"));
        mIdle = new Animation(0.33f, mTextureAtlas.findRegions("erika_idle"));
        mIdle.setPlayMode(Animation.PlayMode.LOOP);
        mRun = new Animation(0.15f, mTextureAtlas.findRegions("erika_run"));
        mRun.setPlayMode(Animation.PlayMode.LOOP);
        mUp = new Animation(0.15f, mTextureAtlas.findRegions("erika_up"));
        mUp.setPlayMode(Animation.PlayMode.LOOP);
        mDown = new Animation(0.15f, mTextureAtlas.findRegions("erika_down"));
        mDown.setPlayMode(Animation.PlayMode.LOOP);
        mAttack = new Animation(ATTACK_TIME, mAttackAtlas.findRegion("attack_left"));
        mGridMovement = new GridMovement(this, grid);
        mGridMovement.setSpeed(new Vector2(75f, 75f));
        mDirection = new Vector2(-1.0f, 0.0f);
        mTimer = Stopwatch.createUnstarted();
        this.mMap = mMap;
        this.mHp = mHp;
    }

    /**
     * Sets the player into mAttack state.
     */
    public void attack() {
        if (!mAttacking) {
            Gdx.app.log("Button Press", "Player mAttack");
            mAttacking = true;
            if (mDirection.x < 0.0) {
                mAttack = new Animation(ATTACK_TIME, mAttackAtlas.findRegion("attack_left"));
            } else if (mDirection.x > 0.0) {
                mAttack = new Animation(ATTACK_TIME, mAttackAtlas.findRegion("attack_right"));
            } else if (mDirection.y > 0.0) {
                mAttack = new Animation(ATTACK_TIME, mAttackAtlas.findRegion("attack_up"));
            } else if (mDirection.y < 0.0) {
                mAttack = new Animation(ATTACK_TIME, mAttackAtlas.findRegion("attack_down"));
            }
        }
    }

    /**
     * Interact with object in front of the player.
     * If it's objective, activate it. If it's an item, gain mHp.
     */
    public void interact() {
        for (Tile tile : mTileList) {
            if (tile.tileState != TileState.ITEM && tile.tileState != TileState.OBJECTIVE)
                continue;
            int tileX = (int) (mPosition.x / TILE_WIDTH);
            int tileY = (int) (mPosition.y / TILE_HEIGHT);
            if (tile.position[0] == tileX + mDirection.x && tile.position[1] == tileY + mDirection.y) {
                Gdx.app.log("Interact", "At pos " + tile.position[0] + "," + tile.position[1]);
                if (tile.tileState == TileState.ITEM && tile.active) {
                    mGridMovement.getGrid().getNode(tile.position[0], tile.position[1]).walkable = true;
                    ((TiledMapTileLayer) mMap.tiledMap.getLayers().get(1)).setCell(tile.position[0], tile.position[1], null);
                    tile.active = false;
                    mHp += 3;
                    mScreen.mGameUI.updateHP(3);
                    if (mHp > 10) {
                        int diff = 10 - mHp;
                        mHp = 10;
                        mScreen.mGameUI.updateHP(diff);
                    }
                    break;
                } else if (tile.tileState == TileState.OBJECTIVE && tile.active) {
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(new StaticTiledMapTile(TextureRegion.split(mMap.spriteTexture, 16, 16)[2][1]));
                    ((TiledMapTileLayer) mMap.tiledMap.getLayers().get(1)).setCell(tile.position[0], tile.position[1], cell);
                    tile.active = false;
                    mScreen.mGameUI.updateObjectiveCount();
                }
            }
        }
    }

    /**
     * Update the player's movement and collisions.
     *
     * @param delta   The time step.
     * @param enemies The list of enemies.
     */
    public void update(float delta, List<Enemy> enemies) {
        super.update(delta);
        if (mTimer.isRunning() && mTimer.elapsed(TimeUnit.MILLISECONDS) > 75) { //If the touchpad hasn't been released for x ms, read as a move instead of change facing dir
            mGridMovement.setDirection(mDirection);
            mTimer.stop();
        }
        mGridMovement.update(delta);

        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            int tileX = (int) (mPosition.x / TILE_WIDTH);
            int tileY = (int) (mPosition.y / TILE_HEIGHT);
            int enemyX = (int) (enemy.mPosition.x / TILE_WIDTH);
            int enemyY = (int) (enemy.mPosition.y / TILE_HEIGHT);
            if (mAttacking) { //IF in mAttack state
                if (enemyX == tileX + mDirection.x && enemyY == tileY + mDirection.y) { //If there's an enemy in front of us, kill them.
                    iterator.remove();
                    break;
                }
            }
            if (enemyX == tileX && enemyY == tileY) { //If an enemy is on the same tile as us, lose health and get knocked back.
                mHp--;
                mScreen.mGameUI.updateHP(-1);
                if (mGridMovement.getGrid().walkable(tileX + (int) (mDirection.x * -2), tileY + (int) (mDirection.y * -2))) {
                    setMoving(null);
                    mGridMovement.snapToTile(tileX + (int) (mDirection.x * -2), tileY + (int) (mDirection.y * -2));
                    mGridMovement.setDestination(null);
                } else {
                    setMoving(null);
                    mGridMovement.snapToTile(tileX + (int) (mDirection.x * -1), tileY + (int) (mDirection.y * -1));
                    mGridMovement.setDestination(null);
                }
                mVelocity.set(0.0f, 0.0f);
                break;
            }
        }
    }

    /**
     * Render the player's sprite
     *
     * @param delta  the time step.
     * @param camera the camera to use.
     */
    public void render(float delta, Camera camera) {
        mAnimTime += delta;
        TextureRegion keyFrame = null, attackFrame = null;
        mFlip = mDirection.x > 0.0;
        if (mAttacking) {
            mAttackAnimTime += delta;
            if (mAttack.isAnimationFinished(mAttackAnimTime)) {
                mAttacking = false;
                keyFrame = mIdle.getKeyFrame(mAnimTime);
                mAttackAnimTime = 0.0f;
            } else {
                attackFrame = mAttack.getKeyFrame(mAttackAnimTime, false);
                if (mDirection == null) {
                    keyFrame = mIdle.getKeyFrames()[0];
                } else if (mDirection.x < 0.0) {  //Check mLast known mDirection
                    keyFrame = mRun.getKeyFrames()[0];
                } else if (mDirection.x > 0.0) {
                    keyFrame = mRun.getKeyFrames()[0];
                } else if (mDirection.y > 0.0) {
                    keyFrame = mUp.getKeyFrames()[0];
                } else if (mDirection.y < 0.0) {
                    keyFrame = mDown.getKeyFrames()[0];
                }

            }
        }
        //set animation
        else if (!mGridMovement.isMoving()) { //If not moving, use mIdle
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
            if (attackFrame != null) {
                mSpriteBatch.draw(attackFrame, mDirection.x < 0.0 ? mPosition.x - (TILE_WIDTH) : mPosition.x, mDirection.y < 0.0 ? mPosition.y - TILE_HEIGHT : mPosition.y, attackFrame.getRegionWidth(), attackFrame.getRegionHeight());
            }
            mSpriteBatch.end();
        }
        if (mAnimTime > 1.0f) mAnimTime = 0.0f;
    }

    /**
     * Start moving the player in the specified mDirection.
     *
     * @param dir 2D vector specifying the mDirection to move in.
     */
    public void setMoving(Vector2 dir) {
        if (dir != null) { //Controller passed a mDirection
            mDirection = dir; //Set the player's mDirection
            if (mTimer.isRunning()) //Start a mTimer, to count how long the touchpad is held
                return;
            mTimer.reset();
            mTimer.start();
        } else {
            mGridMovement.setDirection(dir); //Touchpad released, stop movement
            if (mTimer.isRunning())
                mTimer.stop();
            mTimer.reset();
        }
    }

    public void setDirection(Vector2 dir) {
        mDirection = dir;
    }

    public List<Tile> getTileList() {
        return mTileList;
    }

    public void setTileList(List<Tile> tileList) {
        this.mTileList = tileList;
    }

    public void dispose() {
        this.mTextureAtlas.dispose();
        this.mAttackAtlas.dispose();
    }

    @Override
    public void setPosition(Vector2 position) {
        super.setPosition(new Vector2(position.x * TILE_WIDTH, position.y * TILE_HEIGHT));
    }

    public MapBuilder.Map getMap() {
        return mMap;
    }

    public void setMap(MapBuilder.Map map) {
        this.mMap = map;
    }

    public boolean isAttacking() {
        return mAttacking;
    }

    public int getHP() {
        return mHp;
    }

    public void setHP(int HP) {
        this.mHp = HP;
    }
}

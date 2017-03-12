package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
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
 */

public class Player extends TileMovable {

    private float animTime = 0.0f, attAnimTime = 0.0f;
    private Animation idle, run, up, down, attack;
    private TextureAtlas textureAtlas, attackAtlas;
    private SpriteBatch spriteBatch;
    private boolean flip = false, attacking = false;
    private List<Tile> tileList;
    private static float ATTACK_TIME = 0.25f;
    private MapBuilder.Map map;
    private LevelGenScreen screen;
    private Stopwatch timer;
    private int HP;


    public Player(LevelGenScreen screen, SpriteBatch batch, Grid grid, List<Tile> tileList, MapBuilder.Map map, int HP) {
        super(new Vector2(20 * TILE_WIDTH, 20 * TILE_HEIGHT), new Vector2());
        this.tileList = tileList;
        this.screen = screen;
        spriteBatch = batch;
        textureAtlas = new TextureAtlas(Gdx.files.internal("sprites/pc.atlas"));
        attackAtlas = new TextureAtlas(Gdx.files.internal("sprites/attack2.atlas"));
        idle = new Animation(0.33f, textureAtlas.findRegions("erika_idle"));
        idle.setPlayMode(Animation.PlayMode.LOOP);
        run = new Animation(0.15f, textureAtlas.findRegions("erika_run"));
        run.setPlayMode(Animation.PlayMode.LOOP);
        up = new Animation(0.15f, textureAtlas.findRegions("erika_up"));
        up.setPlayMode(Animation.PlayMode.LOOP);
        down = new Animation(0.15f, textureAtlas.findRegions("erika_down"));
        down.setPlayMode(Animation.PlayMode.LOOP);
        attack = new Animation(ATTACK_TIME, attackAtlas.findRegion("attack_left"));
        gridMovement = new GridMovement(this, grid);
        gridMovement.setSpeed(new Vector2(75f, 75f));
        direction = new Vector2(-1.0f, 0.0f);
        timer = Stopwatch.createUnstarted();
        this.map = map;
        this.HP = HP;
    }

    public void attack() {
        if (!attacking) {
            Gdx.app.log("Button Press", "Player attack");
            attacking = true;
            if (direction.x < 0.0) {
                attack = new Animation(ATTACK_TIME, attackAtlas.findRegion("attack_left"));
            } else if (direction.x > 0.0) {
                attack = new Animation(ATTACK_TIME, attackAtlas.findRegion("attack_right"));
            } else if (direction.y > 0.0) {
                attack = new Animation(ATTACK_TIME, attackAtlas.findRegion("attack_up"));
            } else if (direction.y < 0.0) {
                attack = new Animation(ATTACK_TIME, attackAtlas.findRegion("attack_down"));
            }
        }
    }

    public void interact() {
        for (Tile tile : tileList) {
            if (tile.tileState != TileState.ITEM && tile.tileState != TileState.OBJECTIVE)
                continue;
            int tileX = (int) (position.x / TILE_WIDTH);
            int tileY = (int) (position.y / TILE_HEIGHT);
            if (tile.position[0] == tileX + direction.x && tile.position[1] == tileY + direction.y) {
                Gdx.app.log("Interact", "At pos " + tile.position[0] + "," + tile.position[1]);
                if (tile.tileState == TileState.ITEM && tile.active) {
                    gridMovement.getGrid().getNode(tile.position[0], tile.position[1]).walkable = true;
                    ((TiledMapTileLayer) map.tiledMap.getLayers().get(1)).setCell(tile.position[0], tile.position[1], null);
                    tile.active = false;
                    HP+=3;
                    screen.gameUI.updateHP(3);
                    if(HP > 10){
                        int diff = 10-HP;
                        HP = 10;
                        screen.gameUI.updateHP(diff);
                    }
                    break;
                } else if (tile.tileState == TileState.OBJECTIVE && tile.active) {
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(new StaticTiledMapTile(TextureRegion.split(map.spriteTexture, 16, 16)[2][1]));
                    ((TiledMapTileLayer) map.tiledMap.getLayers().get(1)).setCell(tile.position[0], tile.position[1], cell);
                    tile.active = false;
                    screen.gameUI.updateObjectiveCount();
                }
            }
        }
    }

    public void update(float delta, List<Enemy> enemies) {
        super.update(delta);
        if (timer.isRunning() && timer.elapsed(TimeUnit.MILLISECONDS) > 75) { //If the touchpad hasn't been released for x ms, read as a move instead of change facing dir
            gridMovement.setDirection(direction);
            timer.stop();
        }
        gridMovement.update(delta);

        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            int tileX = (int) (position.x / TILE_WIDTH);
            int tileY = (int) (position.y / TILE_HEIGHT);
            int enemyX = (int) (enemy.position.x / TILE_WIDTH);
            int enemyY = (int) (enemy.position.y / TILE_HEIGHT);
            if (attacking) {
                if (enemyX == tileX + direction.x && enemyY == tileY + direction.y) {
                    iterator.remove();
                    break;
                }
            }
            if (enemyX == tileX && enemyY == tileY) {
                HP--;
                screen.gameUI.updateHP(-1);
                if (gridMovement.getGrid().walkable(tileX + (int) (direction.x * -2), tileY + (int) (direction.y * -2))) {
                    setMoving(null);
                    gridMovement.snapToTile(tileX + (int) (direction.x * -2), tileY + (int) (direction.y * -2));
                    gridMovement.setDestination(null);
                } else {
                    setMoving(null);
                    gridMovement.snapToTile(tileX + (int) (direction.x * -1), tileY + (int) (direction.y * -1));
                    gridMovement.setDestination(null);
                }
                velocity.set(0.0f, 0.0f);
                break;
            }
        }
    }

    public void render(float delta, Camera camera) {
        animTime += delta;
        TextureRegion keyFrame = null, attackFrame = null;
        flip = direction.x > 0.0;
        if (attacking) {
            attAnimTime += delta;
            if (attack.isAnimationFinished(attAnimTime)) {
                attacking = false;
                keyFrame = idle.getKeyFrame(animTime);
                attAnimTime = 0.0f;
            } else {
                attackFrame = attack.getKeyFrame(attAnimTime, false);
                if (direction == null) {
                    keyFrame = idle.getKeyFrames()[0];
                } else if (direction.x < 0.0) {  //Check last known direction
                    keyFrame = run.getKeyFrames()[0];
                } else if (direction.x > 0.0) {
                    keyFrame = run.getKeyFrames()[0];
                } else if (direction.y > 0.0) {
                    keyFrame = up.getKeyFrames()[0];
                } else if (direction.y < 0.0) {
                    keyFrame = down.getKeyFrames()[0];
                }

            }
        }
        //set animation
        else if (!gridMovement.isMoving()) { //If not moving, use idle
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
            if (attackFrame != null) {
                spriteBatch.draw(attackFrame, direction.x < 0.0 ? position.x - (TILE_WIDTH) : position.x, direction.y < 0.0 ? position.y - TILE_HEIGHT : position.y, attackFrame.getRegionWidth(), attackFrame.getRegionHeight());
            }
            spriteBatch.end();
        }
        if (animTime > 1.0f) animTime = 0.0f;
    }

    public void setMoving(Vector2 dir) {
        if (dir != null) { //Controller passed a direction
            direction = dir; //Set the player's direction
            if (timer.isRunning()) //Start a timer, to count how long the touchpad is held
                return;
            timer.reset();
            timer.start();
        } else {
            gridMovement.setDirection(dir); //Touchpad released, stop movement
            if (timer.isRunning())
                timer.stop();
            timer.reset();
        }
    }

    public void setDirection(Vector2 dir) {
        direction = dir;
    }

    public List<Tile> getTileList() {
        return tileList;
    }

    public void setTileList(List<Tile> tileList) {
        this.tileList = tileList;
    }

    public void dispose() {
        this.textureAtlas.dispose();
        this.attackAtlas.dispose();
    }

    @Override
    public void setPosition(Vector2 position) {
        super.setPosition(new Vector2(position.x * TILE_WIDTH, position.y * TILE_HEIGHT));
    }

    public MapBuilder.Map getMap() {
        return map;
    }

    public void setMap(MapBuilder.Map map) {
        this.map = map;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public int getHP() {
        return HP;
    }
}

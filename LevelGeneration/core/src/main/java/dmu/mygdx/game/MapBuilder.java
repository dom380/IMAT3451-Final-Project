package dmu.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;

import java.util.List;

import dmu.project.levelgen.HeightMap;
import dmu.project.levelgen.Tile;
import dmu.project.weather.WeatherResponse;

/**
 * Created by Dom on 25/02/2017.#
 * Utility class to aid in building the map tile set.
 */

public class MapBuilder {

    /**
     * Builds the specified map.
     *
     * @param width           The width of the map.
     * @param height          The height of the map.
     * @param tileWidth       The width of each individual tile in pixels.
     * @param tileHeight      The height of each individual tile in pixels.
     * @param heightMap       The heightmap to use.
     * @param mapObjects      The list of objects on the map.
     * @param weatherResponse Optional, the current weather.
     * @return The built map.
     */
    public static Map buildMap(int width, int height, int tileWidth, int tileHeight, HeightMap heightMap, List<Tile> mapObjects, WeatherResponse weatherResponse) {
        Map map = new Map();
        map.spriteTexture = new Texture(Gdx.files.internal("sprites/spritesv2.png"));
        map.spriteTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        if (weatherResponse != null) {
            float temp = weatherResponse.getMain().getTemp();
            WeatherResponse.ConditionCode conditionCode = weatherResponse.getWeather().get(0).getId();
            weatherResponse.getWeather().get(0).getId();
            if (temp < -1.0f || conditionCode == WeatherResponse.ConditionCode.LIGHT_SNOW ||
                    conditionCode == WeatherResponse.ConditionCode.HEAVY_SNOW ||
                    conditionCode == WeatherResponse.ConditionCode.SNOW) {
                return buildSnowMap(map, width, height, tileWidth, tileHeight, heightMap, mapObjects);
            } else if (conditionCode == WeatherResponse.ConditionCode.EXTREME_RAIN || conditionCode == WeatherResponse.ConditionCode.MODERATE_RAIN || conditionCode == WeatherResponse.ConditionCode.DRIZZLE) {
                return buildSwampMap(map, width, height, tileWidth, tileHeight, heightMap, mapObjects);
            } else if (temp >= 30.0f) {
                return buildDesertMap(map, width, height, tileWidth, tileHeight, heightMap, mapObjects);
            }
        }
        //No weather data available, default to grass map
        return buildGrassMap(map, width, height, tileWidth, tileHeight, heightMap, mapObjects);
    }


    /**
     * Class representing the map.
     */
    public static class Map {
        public Map() { //Empty constructor
        }

        public Texture spriteTexture;
        public Texture tileTexture;
        public TiledMap tiledMap = new TiledMap();
        public ParticleEffect particleEffect;

        /**
         * Clean up resources.
         */
        public void dispose() {
            spriteTexture.dispose();
            tileTexture.dispose();
            tiledMap.dispose();
        }
    }


    /////////////////////////////
    //Private Utility Methods //
    ////////////////////////////

    /**
     * Builds the map using the snow tile set.
     *
     * @param width      The width of the map.
     * @param height     The height of the map.
     * @param tileWidth  The width of each individual tile in pixels.
     * @param tileHeight The height of each individual tile in pixels.
     * @param heightMap  The heightmap to use.
     * @param mapObjects The list of objects on the map.
     * @return The built map.
     */
    private static Map buildSnowMap(Map map, int width, int height, int tileWidth, int tileHeight, HeightMap heightMap, List<Tile> mapObjects) {
        map.particleEffect = new ParticleEffect();
        map.particleEffect.load(Gdx.files.internal("effects/snow_particle.p"), Gdx.files.internal("effects"));
        map.particleEffect.setPosition(0, Gdx.graphics.getHeight() + 10);
        map.tileTexture = new Texture(Gdx.files.internal("sprites/iceGradient.png"));
        map.tileTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        TextureRegion[][] splitTiles = TextureRegion.split(map.tileTexture, tileWidth, tileHeight);
        TextureRegion[][] splitSprites = TextureRegion.split(map.spriteTexture, 16, 16);
        //Construct TileMap
        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
        TiledMapTileLayer spriteLayer = addSprites(width, height, tileWidth, tileHeight, mapObjects, splitSprites[1]);
        for (int x = 0; x < width; x++) { //Set each tile to the correct sprite based on elevation.
            for (int y = 0; y < height; y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                int tx, ty;
                double scalar;
                if (heightMap.elevation[x][y] < heightMap.waterLevel) {
                    tx = 0;
                    scalar = heightMap.elevation[x][y] / heightMap.waterLevel;
                } else if (heightMap.elevation[x][y] < 0.65) {
                    tx = 1;
                    scalar = (heightMap.elevation[x][y] - (heightMap.waterLevel - 0.01)) / (0.65 - heightMap.waterLevel);
                } else {
                    tx = 2;
                    scalar = (heightMap.elevation[x][y] - 0.64) / (1.0 - 0.65);
                }
                ty = (int) (64 * scalar);
                if (ty > 63) {
                    ty = 63;
                }
                cell.setTile(new StaticTiledMapTile(splitTiles[tx][ty]));
                layer.setCell(x, y, cell);
            }
        }
        map.tiledMap.getLayers().add(layer);
        map.tiledMap.getLayers().add(spriteLayer);
        return map;
    }

    /**
     * Builds the map using the swamp tile set.
     *
     * @param width      The width of the map.
     * @param height     The height of the map.
     * @param tileWidth  The width of each individual tile in pixels.
     * @param tileHeight The height of each individual tile in pixels.
     * @param heightMap  The heightmap to use.
     * @param mapObjects The list of objects on the map.
     * @return The built map.
     */
    private static Map buildSwampMap(Map map, int width, int height, int tileWidth, int tileHeight, HeightMap heightMap, List<Tile> mapObjects) {
        map.particleEffect = new ParticleEffect();
        map.particleEffect.load(Gdx.files.internal("effects/rain_particle.p"), Gdx.files.internal("effects"));
        map.particleEffect.setPosition(0, Gdx.graphics.getHeight() + 10);
        return buildGrassMap(map, width, height, tileWidth, tileHeight, heightMap, mapObjects);
    }

    /**
     * Builds the map using the desert tile set.
     *
     * @param width      The width of the map.
     * @param height     The height of the map.
     * @param tileWidth  The width of each individual tile in pixels.
     * @param tileHeight The height of each individual tile in pixels.
     * @param heightMap  The heightmap to use.
     * @param mapObjects The list of objects on the map.
     * @return The built map.
     */
    private static Map buildDesertMap(Map map, int width, int height, int tileWidth, int tileHeight, HeightMap heightMap, List<Tile> mapObjects) {
        map.tileTexture = new Texture(Gdx.files.internal("sprites/sandGradient.png"));
        map.tileTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        TextureRegion[][] splitTiles = TextureRegion.split(map.tileTexture, tileWidth, tileHeight);
        TextureRegion[][] splitSprites = TextureRegion.split(map.spriteTexture, 16, 16);
        //Construct TileMap
        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
        TiledMapTileLayer spriteLayer = addSprites(width, height, tileWidth, tileHeight, mapObjects, splitSprites[3]);
        for (int x = 0; x < width; x++) { //Set each tile to the correct sprite based on elevation.
            for (int y = 0; y < height; y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                int tx, ty;
                double scalar;
                if (heightMap.elevation[x][y] < heightMap.waterLevel) {
                    tx = 0;
                    scalar = heightMap.elevation[x][y] / heightMap.waterLevel;
                } else if (heightMap.elevation[x][y] < 0.65) {
                    tx = 1;
                    scalar = (heightMap.elevation[x][y] - (heightMap.waterLevel - 0.01)) / (0.65 - heightMap.waterLevel);
                } else {
                    tx = 2;
                    scalar = (heightMap.elevation[x][y] - 0.64) / (1.0 - 0.65);
                }
                ty = (int) (64 * scalar);
                if (ty > 63) {
                    ty = 63;
                }
                cell.setTile(new StaticTiledMapTile(splitTiles[tx][ty]));
                layer.setCell(x, y, cell);
            }
        }
        map.tiledMap.getLayers().add(layer);
        map.tiledMap.getLayers().add(spriteLayer);
        return map;
    }

    /**
     * Builds the map using the grass lands tile set.
     *
     * @param width      The width of the map.
     * @param height     The height of the map.
     * @param tileWidth  The width of each individual tile in pixels.
     * @param tileHeight The height of each individual tile in pixels.
     * @param heightMap  The heightmap to use.
     * @param mapObjects The list of objects on the map.
     * @return The built map.
     */
    private static Map buildGrassMap(Map map, int width, int height, int tileWidth, int tileHeight, HeightMap heightMap, List<Tile> mapObjects) {

        map.tileTexture = new Texture(Gdx.files.internal("sprites/grassGradient.png"));
        map.tileTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        TextureRegion[][] splitTiles = TextureRegion.split(map.tileTexture, tileWidth, tileHeight);
        TextureRegion[][] splitSprites = TextureRegion.split(map.spriteTexture, 16, 16);
        //Construct TileMap
        TiledMapTileLayer layer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
        TiledMapTileLayer spriteLayer = addSprites(width, height, tileWidth, tileHeight, mapObjects, splitSprites[0]);
        for (int x = 0; x < width; x++) { //Set each tile to the correct sprite based on elevation.
            for (int y = 0; y < height; y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                int tx, ty;
                double scalar;
                if (heightMap.elevation[x][y] < heightMap.waterLevel) {
                    tx = 0;
                    scalar = heightMap.elevation[x][y] / heightMap.waterLevel;
                } else if (heightMap.elevation[x][y] < 0.35) {
                    tx = 1;
                    scalar = (heightMap.elevation[x][y] - (heightMap.waterLevel - 0.01f)) / (0.35 - heightMap.waterLevel);
                } else if (heightMap.elevation[x][y] < 0.55) {
                    tx = 2;
                    scalar = (heightMap.elevation[x][y] - 0.34) / (0.55 - 0.35);
                } else {
                    tx = 3;
                    scalar = (heightMap.elevation[x][y] - 0.54) / (1.0 - 0.55);
                }
                ty = (int) (64 * scalar);
                if (ty > 63) {
                    ty = 63;
                }
                cell.setTile(new StaticTiledMapTile(splitTiles[tx][ty]));
                layer.setCell(x, y, cell);
            }
        }

        map.tiledMap.getLayers().add(layer);
        map.tiledMap.getLayers().add(spriteLayer);
        return map;
    }

    /**
     * Utility method to build the object sprite layer.
     *
     * @param width        The width of the map.
     * @param height       The height of the map.
     * @param tileWidth    The width of each individual tile in pixels.
     * @param tileHeight   The height of each individual tile in pixels.
     * @param mapObjects   The list of objects on the map.
     * @param splitSprites The tile set's object sprites.
     * @return The object sprite layer.
     */
    private static TiledMapTileLayer addSprites(int width, int height, int tileWidth, int tileHeight, List<Tile> mapObjects, TextureRegion[] splitSprites) {
        TiledMapTileLayer spriteLayer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
        for (Tile tile : mapObjects) { //For each level object set correct sprite.
            int tx = 0;
            switch (tile.tileState) {
                case START:
                    tx = 4;
                    break;
                case OBJECTIVE:
                    tx = 1;
                    break;
                case ITEM:
                    tx = 0;
                    break;
                case OBSTACLE:
                    tx = 3;
                    break;
                case ENEMY:
                    tx = 2;
                    break;
            }
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(new StaticTiledMapTile(splitSprites[tx]));
            spriteLayer.setCell(tile.position[0], tile.position[1], cell);
        }
        return spriteLayer;
    }
}

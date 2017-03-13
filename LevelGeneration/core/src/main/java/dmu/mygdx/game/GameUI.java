package dmu.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Dom on 08/03/2017.
 *
 * Class that handles the Game's UI.
 */

public class GameUI {
    private MyGdxGame game;
    private LevelGenScreen screen;
    private OrthographicCamera camera;
    private Stage stage;
    private Viewport viewport;
    private TextureAtlas atlas;
    private Skin skin;
    private SpriteBatch batch;
    private Label beaconLabel, hpLabel, gameOverLabel;
    private int numOfObjectives, hp;

    /**
     * Constructor.
     * @param game The main game.
     * @param screen The game screen.
     * @param batch The sprite batch to render the UI with.
     * @param numOfObjectives The number of objects.
     * @param playerHP The player's maximum HP.
     */
    public GameUI(MyGdxGame game, LevelGenScreen screen, SpriteBatch batch, int numOfObjectives, int playerHP) {
        this.game = game;
        this.screen = screen;
        this.batch = batch;
        this.numOfObjectives = numOfObjectives;
        this.hp = playerHP;
        camera = new OrthographicCamera();
        atlas = new TextureAtlas(Gdx.files.internal("sprites/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("sprites/uiskin.json"), atlas);
        viewport = new StretchViewport(800, 480, camera);
        viewport.apply();
        stage = new Stage(viewport, batch);

        Table table = new Table();
        table.setFillParent(true);
        table.top();

        beaconLabel = new Label("Beacons to light: " + numOfObjectives, skin);
        hpLabel = new Label("HP: " + hp, skin);
        gameOverLabel = new Label("", skin);
        gameOverLabel.setColor(1.0f, 0.0f, 0.0f, 1.0f);
        gameOverLabel.setFontScale(3.0f, 3.0f);

        table.add(beaconLabel).padLeft(25.0f).padRight(25.0f).left();
        table.add(hpLabel).padLeft(25.0f).padRight(25.0f);
        table.row();
        table.add(gameOverLabel).center().padTop(50.0f);

        stage.addActor(table);
    }

    /**
     * Render the UI.
     */
    public void draw() {
        stage.draw();
    }

    /**
     * Clean up assets.
     */
    public void dispose() {
        stage.dispose();
        atlas.dispose();
        skin.dispose();
    }

    /**
     * Resize the UI. Called on screen resize event.
     * @param width The new width of the screen.
     * @param height The new height of the screen.
     */
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    /**
     * Updates the remaining objectives text.
     */
    public void updateObjectiveCount() {
        numOfObjectives--;
        beaconLabel.setText("Beacons to light: " + numOfObjectives);
    }

    /**
     * Updates the Player's HP text.
     *
     * @param amount The amount to change the value by.
     */
    public void updateHP(int amount) {
        hp += amount;
        hpLabel.setText("HP: " + hp);
    }

    /**
     * Sets the game over text to display.
     * @param text Text to display.
     */
    public void setGameOverText(String text) {
        gameOverLabel.setText(text);
    }

    /**
     * @return Returns the number of objectives.
     */
    public int getNumOfObjectives() {
        return numOfObjectives;
    }

    /**
     * Changes the UI to the level complete version.
     */
    public void switchToWinUI(){
        stage = new Stage(viewport, batch);

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(gameOverLabel);
        table.row();
        table.row();
        TextButton mainMenu = new TextButton("Return to Main Menu", skin);
        mainMenu.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.returnToMenu();
            }
        });
        table.add(mainMenu);
        TextButton nextLevel = new TextButton("Play next level", skin);
        nextLevel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screen.switchNextMap();
                screen.playMap();
            }
        });
        table.add(nextLevel).padLeft(15.0f);
        stage.addActor(table);
    }

    /**
     * @return returns the scene2D stage holding the UI elements.
     */
    public Stage getStage() {
        return stage;
    }
}

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
 * <p>
 * Class that handles the Game's UI.
 */

public class GameUI {
    private MyGdxGame mGame;
    private LevelGenScreen mScreen;
    private OrthographicCamera mCamera;
    private Stage mStage;
    private Viewport mViewport;
    private TextureAtlas mAtlas;
    private Skin mSkin;
    private SpriteBatch mBatch;
    private Label mBeaconLabel, mHpLabel, mGameOverLabel;
    private int mNumOfObjectives, mHp;

    /**
     * Constructor.
     *
     * @param mGame            The main mGame.
     * @param mScreen          The mGame mScreen.
     * @param mBatch           The sprite mBatch to render the UI with.
     * @param mNumOfObjectives The number of objects.
     * @param playerHP         The player's maximum HP.
     */
    public GameUI(MyGdxGame mGame, LevelGenScreen mScreen, SpriteBatch mBatch, int mNumOfObjectives, int playerHP) {
        this.mGame = mGame;
        this.mScreen = mScreen;
        this.mBatch = mBatch;
        this.mNumOfObjectives = mNumOfObjectives;
        this.mHp = playerHP;
        mCamera = new OrthographicCamera();
        mAtlas = new TextureAtlas(Gdx.files.internal("sprites/uiskin.atlas"));
        mSkin = new Skin(Gdx.files.internal("sprites/uiskin.json"), mAtlas);
        mViewport = new StretchViewport(800, 480, mCamera);
        mViewport.apply();
        mStage = new Stage(mViewport, mBatch);

        Table table = new Table();
        table.setFillParent(true);
        table.top();

        mBeaconLabel = new Label("Beacons to light: " + mNumOfObjectives, mSkin);
        mHpLabel = new Label("HP: " + mHp, mSkin);
        mGameOverLabel = new Label("", mSkin);
        mGameOverLabel.setColor(1.0f, 0.0f, 0.0f, 1.0f);
        mGameOverLabel.setFontScale(3.0f, 3.0f);

        table.add(mBeaconLabel).padLeft(25.0f).padRight(25.0f).left();
        table.add(mHpLabel).padLeft(25.0f).padRight(25.0f);
        table.row();
        table.add(mGameOverLabel).center().padTop(50.0f);

        mStage.addActor(table);
    }

    /**
     * Render the UI.
     */
    public void draw() {
        mStage.draw();
    }

    /**
     * Clean up assets.
     */
    public void dispose() {
        mStage.dispose();
        mAtlas.dispose();
        mSkin.dispose();
    }

    /**
     * Resize the UI. Called on mScreen resize event.
     *
     * @param width  The new width of the mScreen.
     * @param height The new height of the mScreen.
     */
    public void resize(int width, int height) {
        mViewport.update(width, height);
    }

    /**
     * Updates the remaining objectives text.
     */
    public void updateObjectiveCount() {
        mNumOfObjectives--;
        mBeaconLabel.setText("Beacons to light: " + mNumOfObjectives);
    }

    /**
     * Updates the Player's HP text.
     *
     * @param amount The amount to change the value by.
     */
    public void updateHP(int amount) {
        mHp += amount;
        mHpLabel.setText("HP: " + mHp);
    }

    /**
     * Sets the mGame over text to display.
     *
     * @param text Text to display.
     */
    public void setGameOverText(String text) {
        mGameOverLabel.setText(text);
    }

    /**
     * @return Returns the number of objectives.
     */
    public int getNumOfObjectives() {
        return mNumOfObjectives;
    }

    /**
     * Changes the UI to the level complete version.
     */
    public void switchToWinUI() {
        mStage = new Stage(mViewport, mBatch);

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(mGameOverLabel);
        table.row();
        table.row();
        TextButton mainMenu = new TextButton("Return to Main Menu", mSkin);
        mainMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mGame.returnToMenu();
            }
        });
        table.add(mainMenu);
        TextButton nextLevel = new TextButton("Play next level", mSkin);
        nextLevel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mScreen.switchNextMap();
                mScreen.playMap();
            }
        });
        table.add(nextLevel).padLeft(15.0f);
        mStage.addActor(table);
    }

    /**
     * @return returns the scene2D mStage holding the UI elements.
     */
    public Stage getStage() {
        return mStage;
    }
}

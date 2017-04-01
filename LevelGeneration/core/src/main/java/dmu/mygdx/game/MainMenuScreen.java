package dmu.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Dom on 18/11/2016.
 * <p>
 * Class implementing the LibGDX Screen interface.
 */

public class MainMenuScreen implements Screen {

    private final MyGdxGame mGame;
    private OrthographicCamera mCamera;
    private Stage mStage;
    private Viewport mViewport;
    private TextureAtlas mAtlas;
    private Skin mSkin;

    /**
     * Constructor.
     *
     * @param mGame The main mGame object.
     */
    public MainMenuScreen(MyGdxGame mGame) {
        this.mGame = mGame;
        mCamera = new OrthographicCamera();
        mCamera.setToOrtho(false, 800, 480);
        mAtlas = new TextureAtlas(Gdx.files.internal("sprites/uiskin.atlas"));
        mSkin = new Skin(Gdx.files.internal("sprites/uiskin.json"), mAtlas);
        mViewport = new StretchViewport(800, 480);
        mViewport.apply();
        mCamera.position.set(mCamera.viewportWidth / 2, mCamera.viewportHeight / 2, 0);
        mCamera.update();
        mStage = new Stage(mViewport, mGame.mBatch);

        //Stage should control input:
        Gdx.input.setInputProcessor(mStage);
    }

    /**
     * Called when screen first shown.
     * Creates the Menu UI.
     */
    @Override
    public void show() {
        //Create Table
        Table mainTable = new Table();
        //Set table to fill mStage
        mainTable.setFillParent(true);
        //Set alignment of contents in the table.
        mainTable.center();
        Table titleTable = new Table();
        titleTable.setFillParent(true);
        titleTable.top();
        Label title = new Label("IMAT3451 Project: Procedural Content Generation", mSkin);
        title.setFontScale(1.2f, 1.2f);
        titleTable.add(title).padTop(125.f).center();

        Label difficultyLabel = new Label("Difficulty", mSkin);
        final Label difficultyValue = new Label("1.0", mSkin);
        mainTable.add(difficultyLabel);
        final Slider difficultySlider = new Slider(1.0f, 10.0f, 1.0f, false, mSkin);
        difficultySlider.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                difficultyValue.setText(Float.toString(difficultySlider.getValue()));
            }
        });
        mainTable.add(difficultySlider);
        mainTable.add(difficultyValue);
        mainTable.row();
        Label noiseWidthLabel = new Label("Noise Width:", mSkin);
        final TextField noiseWidthField = new TextField("", mSkin);
        noiseWidthField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        Label noiseHeightLabel = new Label("Noise Height:", mSkin);
        final TextField noiseHeightField = new TextField("", mSkin);
        noiseHeightField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        mainTable.add(noiseWidthLabel);
        mainTable.add(noiseWidthField);
        mainTable.row();
        mainTable.add(noiseHeightLabel);
        mainTable.add(noiseHeightField);
        mainTable.row();

        //Add buttons to table
        final CheckBox debugButton = new CheckBox("Debug Seed", mSkin);
        TextButton playButton = new TextButton("Generate Level", mSkin);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int diff = (int) difficultySlider.getValue();
                int noiseWidth = noiseWidthField.getText().isEmpty() ? 4 : Integer.parseInt(noiseWidthField.getText());
                int noiseHeight = noiseHeightField.getText().isEmpty() ? 4 : Integer.parseInt(noiseHeightField.getText());
                boolean debugEnabled = debugButton.isChecked();
                ((Game) Gdx.app.getApplicationListener()).setScreen(new LoadingScreen(mGame, noiseWidth, noiseHeight, diff, debugEnabled));

            }
        });
        mainTable.row();
        mainTable.add(playButton);
        mainTable.add(debugButton);
        mainTable.row();
        //Add table to mStage
        mStage.addActor(titleTable);
        mStage.addActor(mainTable);
    }

    /**
     * Renders the GUI elements.
     *
     * @param delta The current time step.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.1f, .12f, .16f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mStage.act();
        mStage.draw();
    }

    /**
     * Resize the mViewport and recenter the mCamera.
     *
     * @param width  The new width of the screen in pixels.
     * @param height The new height of the screen in pixels.
     */
    @Override
    public void resize(int width, int height) {
        mViewport.update(width, height);
        mCamera.position.set(mCamera.viewportWidth / 2, mCamera.viewportHeight / 2, 0);
        mCamera.update();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        mStage.dispose();
        mSkin.dispose();
        mAtlas.dispose();
    }
}

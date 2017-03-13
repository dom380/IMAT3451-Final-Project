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
 * Created by Dom on 27/02/2017.
 * Class to handle the level selection UI.
 */

public class LevelSelectUI {
    private OrthographicCamera camera;
    private Stage stage;
    private Viewport viewport;
    private TextureAtlas atlas;
    private Skin skin;
    private SpriteBatch batch;


    /**
     * Constructor
     *
     * @param game The main game object.
     * @param screen The current screen.
     */
    public LevelSelectUI(final MyGdxGame game, final LevelGenScreen screen) {
        camera = new OrthographicCamera();
        atlas = new TextureAtlas(Gdx.files.internal("sprites/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("sprites/uiskin.json"), atlas);
        viewport = new StretchViewport(800, 480, camera);
        viewport.apply();
        batch = game.batch;
        stage = new Stage(viewport, batch);

        Table table = new Table();
        table.setFillParent(true);
        table.left().bottom();
        table.pad(0.0f, 0.0f, 5.0f, 0.0f);

        final Label mapLabel = new Label("Map 1/10", skin);
        TextButton previousButton = new TextButton("Previous Map", skin);
        previousButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screen.switchPreviousMap();
                mapLabel.setText("Map " + (screen.getMapIndex()+1) + "/10");
            }
        });
        TextButton nextButton = new TextButton("Next Map", skin);
        nextButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screen.switchNextMap();
                mapLabel.setText("Map " + (screen.getMapIndex()+1) + "/10");
            }
        });
        TextButton menuButton = new TextButton("Main Menu", skin);
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.returnToMenu();
            }
        });

        TextButton playButton = new TextButton("Play", skin);
        playButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screen.playMap();
            }
        });
        table.add(menuButton).pad(0.0f, 5.0f, 0.0f, 0.0f);
        table.add(previousButton).pad(0.0f, 5.0f, 0.0f, 0.0f);
        table.add();
        table.add(nextButton).pad(0.0f, 5.0f, 0.0f, 0.0f);
        table.add();
        table.add(mapLabel).pad(0.0f, 5.0f, 0.0f, 0.0f);
        table.add();
        table.add(playButton).right().padRight(25.0f).padLeft(25.0f).expandX();
        stage.addActor(table);
    }

    /**
     * Render the UI.
     */
    public void draw() {
        stage.draw();
    }

    /**
     * Dispose of the assets.
     */
    public void dispose() {
        stage.dispose();
        atlas.dispose();
        skin.dispose();
    }

    /**
     * Resize the UI.
     * @param width The new width of the screen.
     * @param height The new height of the screen.
     */
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    /**
     * @return Returns the scene2D stage holding the UI elements.
     */
    public Stage getStage() {
        return stage;
    }
}


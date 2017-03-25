package dmu.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Dom on 03/03/2017.
 * Class to handle input to control the mPlayer character and display the on screen controls.
 */

public class Controller extends InputAdapter {
    private OrthographicCamera mCamera;
    private Stage mStage;
    private Viewport mViewport;
    private TextureAtlas mAtlas;
    private Skin mSkin;
    private SpriteBatch mBatch;
    private Skin mTouchpadSkin, mButtonSkin;
    private Player mPlayer;

    /**
     * Constructor. Initialises the UI layout.
     *
     * @param batch   The sprite mBatch to use when rendering the UI
     * @param mPlayer The mPlayer character to control.
     */
    public Controller(SpriteBatch batch, final Player mPlayer) {
        this.mPlayer = mPlayer;
        mCamera = new OrthographicCamera();
        mAtlas = new TextureAtlas(Gdx.files.internal("sprites/uiskin.atlas"));
        mSkin = new Skin(Gdx.files.internal("sprites/uiskin.json"), mAtlas);
        mViewport = new StretchViewport(800, 480, mCamera);
        mViewport.apply();
        this.mBatch = batch;
        mStage = new Stage(mViewport, batch);
        Table table = new Table();
        table.setFillParent(true);
        table.bottom();
        table.pad(0.0f, 0.0f, 5.0f, 0.0f);

        mTouchpadSkin = new Skin();
        mTouchpadSkin.add("touchBackground", new Texture("controller/joystickBackground.png"));
        mTouchpadSkin.add("touchKnob", new Texture("controller/joystickKnob.png"));
        Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
        touchpadStyle.background = mTouchpadSkin.getDrawable("touchBackground");
        touchpadStyle.background.setMinHeight(50);
        touchpadStyle.background.setMinWidth(50);
        touchpadStyle.knob = mTouchpadSkin.getDrawable("touchKnob");
        touchpadStyle.knob.setMinHeight(40);
        touchpadStyle.knob.setMinWidth(40);
        Touchpad touchpad = new Touchpad(5, touchpadStyle);
        touchpad.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Touchpad tp = (Touchpad) actor;
                float dx = tp.getKnobPercentX();
                float dy = tp.getKnobPercentY();
                if (dx == 0.0 && dy == 0.0) {
                    mPlayer.setMoving(null);
                    return;
                }
                double angle = Math.atan2(dy, dx);
                int quad = (int) ((4 * angle / (2 * Math.PI) + 4.5) % 4); //get the nearest 4-mDirection
                if (quad == 0) { //Right
                    dy = 0.0f;
                    dx = 1.0f;
                } else if (quad == 1) { //Up
                    dy = 1.0f;
                    dx = 0.0f;
                } else if (quad == 2) { //Left
                    dy = 0.0f;
                    dx = -1.0f;
                } else if (quad == 3) { //Down
                    dy = -1.0f;
                    dx = 0.0f;
                }
                mPlayer.setMoving(new Vector2(dx, dy));
            }
        });
        table.add(touchpad).width(75).height(75).pad(0.0f, 10.0f, 5.0f, 0.0f).fill().left().expandX();

        mButtonSkin = new Skin();
        mButtonSkin.add("AButton", new Texture(Gdx.files.internal("controller/AButton.png")));
        mButtonSkin.add("AButton_Pressed", new Texture(Gdx.files.internal("controller/AButton_Pressed.png")));
        mButtonSkin.add("BButton", new Texture(Gdx.files.internal("controller/BButton.png")));
        mButtonSkin.add("BButton_Pressed", new Texture(Gdx.files.internal("controller/BButton_Pressed.png")));

        Button.ButtonStyle AButtonStyle = new Button.ButtonStyle();
        AButtonStyle.up = mButtonSkin.getDrawable("AButton");
        AButtonStyle.down = mButtonSkin.getDrawable("AButton_Pressed");
        Button aButton = new Button(AButtonStyle);
        aButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mPlayer.attack();
            }
        });
        table.add(aButton).right();

        Button.ButtonStyle BButtonStyle = new Button.ButtonStyle();
        BButtonStyle.up = mButtonSkin.getDrawable("BButton");
        BButtonStyle.down = mButtonSkin.getDrawable("BButton_Pressed");
        Button bButton = new Button(BButtonStyle);
        bButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mPlayer.interact();
            }
        });
        table.add(bButton).right().padRight(10.0f).padLeft(10.0f);

        mStage.addActor(table);
    }

    /**
     * Render the UI elements.
     *
     * @param delta the time since the mLast frame.
     */
    public void draw(float delta) {
        mStage.act(delta);
        mStage.draw();
    }

    /**
     * Clean up assets.
     */
    public void dispose() {
        mStage.dispose();
        mAtlas.dispose();
        mSkin.dispose();
        mTouchpadSkin.dispose();
    }


    /**
     * Called on screen resize events.
     *
     * @param width  new width of screen.
     * @param height new height of screen.
     */
    public void resize(int width, int height) {
        mViewport.update(width, height);
    }

    /**
     * @return The scene2D mStage holding the UI elements.
     */
    public Stage getStage() {
        return mStage;
    }


    /**
     * Handle key press events.
     *
     * @param keycode The LibGDX key code for the key pressed.
     * @return True if the event was handled, false if not.
     */
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.UP:
                mPlayer.setMoving(new Vector2(0, 1));
                return true;
            case Input.Keys.LEFT:
                mPlayer.setMoving(new Vector2(-1, 0));
                return true;
            case Input.Keys.DOWN:
                mPlayer.setMoving(new Vector2(0, -1));
                return true;
            case Input.Keys.RIGHT:
                mPlayer.setMoving(new Vector2(1, 0));
                return true;
            default:
                return false;
        }
    }

    /**
     * Key Released event.
     *
     * @param keycode The LibGDX key code for the key pressed.
     * @return True if the event was handled, false if not.
     */
    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.UP:
            case Input.Keys.LEFT:
            case Input.Keys.DOWN:
            case Input.Keys.RIGHT:
                mPlayer.setMoving(null);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean keyTyped(char character) {
        return super.keyTyped(character);
    }
}

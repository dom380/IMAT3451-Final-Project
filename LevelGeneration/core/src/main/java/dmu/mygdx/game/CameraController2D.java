package dmu.mygdx.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

/**
 * Class to control the level mCamera.
 * <p>
 * Touch to pan around. Pinch to zoom in and out.
 * <p>
 * Created by Dom on 23/11/2016.
 */

public class CameraController2D implements GestureDetector.GestureListener {

    private OrthographicCamera mCamera;
    private Vector2 mLastTouch = new Vector2();
    private float mInitialScale = 1;
    private float mMaxScale;
    private Vector2 mScrollLimit = new Vector2();


    /**
     * Constructor
     *
     * @param mCamera The mCamera to control input for.
     * @param zoom    The initial zoom. Value between 0.0-1.0
     * @param scaleX  The scene's width divided by the viewport width.
     * @param scaleY  The scene's height divided by the viewport height.
     */
    public CameraController2D(OrthographicCamera mCamera, float zoom, float scaleX, float scaleY) {
        this.mCamera = mCamera;
        mCamera.zoom = zoom;
        mMaxScale = Math.min(scaleX, scaleY);
        this.mCamera.position.x = (mCamera.viewportWidth * scaleX) / 2;
        this.mCamera.position.y = (mCamera.viewportHeight * scaleY) / 2;
        this.mScrollLimit = new Vector2(mCamera.viewportWidth * scaleX, mCamera.viewportHeight * scaleY); //new Vector2(mCamera.viewportWidth, mCamera.viewportHeight); //mScrollLimit;
    }

    /**
     * Response to touch event.
     *
     * @param x       X mPosition of touch event.
     * @param y       Y mPosition of touch event.
     * @param pointer Index of the pointer that triggered the event.
     * @param button
     * @return True if the event was handled. False if event should continue to pass to listeners.
     */
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        mLastTouch.set(x, y);
        mInitialScale = mCamera.zoom;
        return false;
    }

    //no-op
    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    //no-op
    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    //no-op
    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        mCamera.translate(-deltaX, deltaY, 0);
        constrainCamera();
        return false;
    }

    //no-op
    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }


    /**
     * Zoom in or out the mCamera while keeping it constrained to the scene.
     *
     * @param initialDistance The initial zoom value.
     * @param distance        The new zoom value/
     * @return True if the event was handled. False if event should continue to pass to listeners.
     */
    @Override
    public boolean zoom(float initialDistance, float distance) {
        float ratio = (initialDistance / distance) * mInitialScale;
        mCamera.zoom = ratio > mMaxScale ? mMaxScale : ratio;
        constrainCamera();
        return true;
    }

    //no-op
    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {
        //no-op
    }

    /////////////////////////////
    //Private Utility Methods //
    ////////////////////////////

    /**
     * Constrains the mCamera so it doesn't scroll outside the scene.
     */
    private void constrainCamera() {
        if (mCamera.position.x - (mCamera.viewportWidth * mCamera.zoom) / 2 < 0)
            mCamera.position.x = (mCamera.viewportWidth * mCamera.zoom) / 2;
        else if (mCamera.position.x + (mCamera.viewportWidth * mCamera.zoom) / 2 > mScrollLimit.x)
            mCamera.position.x = mScrollLimit.x - (mCamera.viewportWidth * mCamera.zoom) / 2;
        if (mCamera.position.y - (mCamera.viewportHeight * mCamera.zoom) / 2 < 0)
            mCamera.position.y = (mCamera.viewportHeight * mCamera.zoom) / 2;
        else if (mCamera.position.y + (mCamera.viewportHeight * mCamera.zoom) / 2 > mScrollLimit.y)
            mCamera.position.y = mScrollLimit.y - (mCamera.viewportHeight * mCamera.zoom) / 2;
        mCamera.update();
    }
}

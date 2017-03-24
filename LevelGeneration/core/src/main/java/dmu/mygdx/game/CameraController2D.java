package dmu.mygdx.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

/**
 * Class to control the level camera.
 * <p>
 * Touch to pan around. Pinch to zoom in and out.
 * <p>
 * Created by Dom on 23/11/2016.
 */

public class CameraController2D implements GestureDetector.GestureListener {

    private OrthographicCamera camera;
    private Vector2 lastTouch = new Vector2();
    private float initialScale = 1;
    private float maxScale;
    private Vector2 scrollLimit = new Vector2();

    public CameraController2D(OrthographicCamera camera) {
        this(camera, 1.0f, 1.0f, 1.0f);
    }

    /**
     * @param camera The camera to control input for.
     * @param zoom   The initial zoom. Value between 0.0-1.0
     * @param scaleX The scene's width divided by the viewport width.
     * @param scaleY The scene's height divided by the viewport height.
     */
    public CameraController2D(OrthographicCamera camera, float zoom, float scaleX, float scaleY) {
        this.camera = camera;
        camera.zoom = zoom;
        maxScale = Math.min(scaleX, scaleY);
        this.camera.position.x = (camera.viewportWidth * scaleX) / 2;
        this.camera.position.y = (camera.viewportHeight * scaleY) / 2;
        this.scrollLimit = new Vector2(camera.viewportWidth * scaleX, camera.viewportHeight * scaleY); //new Vector2(camera.viewportWidth, camera.viewportHeight); //scrollLimit;
    }

    /**
     * Response to touch event.
     *
     * @param x       X position of touch event.
     * @param y       Y position of touch event.
     * @param pointer Index of the pointer that triggered the event.
     * @param button
     * @return True if the event was handled. False if event should continue to pass to listeners.
     */
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        lastTouch.set(x, y);
        initialScale = camera.zoom;
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
        camera.translate(-deltaX, deltaY, 0);
        constrainCamera();
        return false;
    }

    //no-op
    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }


    /**
     * Zoom in or out the camera while keeping it constrained to the scene.
     *
     * @param initialDistance The initial zoom value.
     * @param distance        The new zoom value/
     * @return True if the event was handled. False if event should continue to pass to listeners.
     */
    @Override
    public boolean zoom(float initialDistance, float distance) {
        float ratio = (initialDistance / distance) * initialScale;
        camera.zoom = ratio > maxScale ? maxScale : ratio;
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
     * Constrains the camera so it doesn't scroll outside the scene.
     */
    private void constrainCamera() {
        if (camera.position.x - (camera.viewportWidth * camera.zoom) / 2 < 0)
            camera.position.x = (camera.viewportWidth * camera.zoom) / 2;
        else if (camera.position.x + (camera.viewportWidth * camera.zoom) / 2 > scrollLimit.x)
            camera.position.x = scrollLimit.x - (camera.viewportWidth * camera.zoom) / 2;
        if (camera.position.y - (camera.viewportHeight * camera.zoom) / 2 < 0)
            camera.position.y = (camera.viewportHeight * camera.zoom) / 2;
        else if (camera.position.y + (camera.viewportHeight * camera.zoom) / 2 > scrollLimit.y)
            camera.position.y = scrollLimit.y - (camera.viewportHeight * camera.zoom) / 2;
        camera.update();
    }
}

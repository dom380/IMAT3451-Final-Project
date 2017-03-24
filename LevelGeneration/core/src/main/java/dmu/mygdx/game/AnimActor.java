package dmu.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by Dom on 03/03/2017.
 * Extends LibGDX scene2D actor to use LibGDX animation.
 */

public class AnimActor extends Actor {

    private Animation animation;
    private TextureRegion currentRegion;

    private boolean reCentre;

    private float time = 0.0f;

    /**
     * Constructor
     *
     * @param animation The animation this actor should control.
     */
    public AnimActor(Animation animation) {
        this(animation, false);
    }

    /**
     * Constructor
     *
     * @param animation The animation this actor should control.
     * @param reCentre  Boolean flag signaling whether to render the animation from its centre if true.
     */
    public AnimActor(Animation animation, boolean reCentre) {
        this.animation = animation;
        this.reCentre = reCentre;
    }

    /**
     * Override of act method. Called by scene2D stage. Sets the current key frame of animation.
     *
     * @param delta The time in seconds since the last update.
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;

        currentRegion = animation.getKeyFrame(time, true);
    }

    /**
     * Draws the current key frame using the specified batch.
     *
     * @param batch       The batch to render the frame with.
     * @param parentAlpha The alpha value of this actors parent.
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (reCentre)
            batch.draw(currentRegion, getX() - currentRegion.getRegionWidth() / 2, getY() - currentRegion.getRegionHeight() / 2);
        else
            batch.draw(currentRegion, getX(), getY());
    }

}

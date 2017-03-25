package dmu.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by Dom on 03/03/2017.
 * Extends LibGDX scene2D actor to use LibGDX mAnimation.
 */

public class AnimActor extends Actor {

    private Animation mAnimation;
    private TextureRegion mCurrentRegion;
    private boolean mReCentre;
    private float mTime = 0.0f;

    /**
     * Constructor
     *
     * @param mAnimation The mAnimation this actor should control.
     */
    public AnimActor(Animation mAnimation) {
        this(mAnimation, false);
    }

    /**
     * Constructor
     *
     * @param mAnimation The mAnimation this actor should control.
     * @param mReCentre  Boolean flag signaling whether to render the mAnimation from its centre if true.
     */
    public AnimActor(Animation mAnimation, boolean mReCentre) {
        this.mAnimation = mAnimation;
        this.mReCentre = mReCentre;
    }

    /**
     * Override of act method. Called by scene2D stage. Sets the current key frame of mAnimation.
     *
     * @param delta The mTime in seconds since the mLast update.
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        mTime += delta;

        mCurrentRegion = mAnimation.getKeyFrame(mTime, true);
    }

    /**
     * Draws the current key frame using the specified mBatch.
     *
     * @param batch       The mBatch to render the frame with.
     * @param parentAlpha The alpha value of this actors parent.
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (mReCentre)
            batch.draw(mCurrentRegion, getX() - mCurrentRegion.getRegionWidth() / 2, getY() - mCurrentRegion.getRegionHeight() / 2);
        else
            batch.draw(mCurrentRegion, getX(), getY());
    }

}

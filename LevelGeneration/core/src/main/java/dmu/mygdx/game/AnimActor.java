package dmu.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by Dom on 03/03/2017.
 */

public class AnimActor extends Actor {

    private Animation animation;
    private TextureRegion currentRegion;

    private boolean reCentre;

    private float time = 0.0f;

    public AnimActor(Animation animation) {
        this(animation, false);
    }

    public AnimActor(Animation animation, boolean reCentre) {
        this.animation = animation;
        this.reCentre = reCentre;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;

        currentRegion = animation.getKeyFrame(time, true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (reCentre)
            batch.draw(currentRegion, getX() - currentRegion.getRegionWidth() / 2, getY() - currentRegion.getRegionHeight() / 2);
        else
            batch.draw(currentRegion, getX(), getY());
    }

}

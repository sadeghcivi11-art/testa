package hk.view.render;

import hk.engine.EntityView;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hk.model.npc.Zote;
import hk.view.anim.ZoteAnimationSet;


public class ZoteRenderer extends EntityView<Zote, Zote.State> {

    private static final float SPRITE_HEIGHT = 28f;
    private static final float Y_NUDGE       = 4f;

    private final ZoteAnimationSet animations;

    public ZoteRenderer(SpriteBatch batch, ZoteAnimationSet animations) {
        super(batch);
        this.animations = animations;
    }

    @Override
    protected Zote.State stateOf(Zote zote) {
        return zote.state;
    }

    @Override
    protected Animation<TextureRegion> animationFor(Zote.State state) {
        return animations.get(state);
    }

    @Override
    protected float spriteHeight() {
        return SPRITE_HEIGHT;
    }

    @Override
    protected float footCenterX(Zote zote) {
        return zote.position.x + Zote.WIDTH / 2f;
    }

    @Override
    protected float footY(Zote zote) {
        float cy = zote.position.y + Zote.HEIGHT / 2f;
        return cy - SPRITE_HEIGHT / 2f + Y_NUDGE;
    }

    @Override
    protected boolean facingRight(Zote zote) {
        return zote.facing == Zote.Facing.RIGHT;
    }
}

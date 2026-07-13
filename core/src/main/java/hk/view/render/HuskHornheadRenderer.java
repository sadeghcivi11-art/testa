package hk.view.render;

import hk.engine.EntityView;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hk.model.enemy.EnemyModel;
import hk.model.enemy.concrete.HuskHornhead;
import hk.model.enemy.concrete.HuskHornhead.HuskState;
import hk.view.anim.HuskHornheadAnimationSet;


public class HuskHornheadRenderer extends EntityView<HuskHornhead, HuskState> implements EnemyRenderer {

    private static final float SPRITE_HEIGHT = 32f;

    private final HuskHornheadAnimationSet animations;

    public HuskHornheadRenderer(SpriteBatch batch, HuskHornheadAnimationSet animations) {
        super(batch);
        this.animations = animations;
    }

    @Override
    public void draw(EnemyModel e, float delta) {
        if (e instanceof HuskHornhead h) draw(h, delta);
    }

    @Override
    protected HuskState stateOf(HuskHornhead h) {
        return h.huskState;
    }

    @Override
    protected Animation<TextureRegion> animationFor(HuskState state) {
        return animations.get(state);
    }

    @Override
    protected float spriteHeight() {
        return SPRITE_HEIGHT;
    }

    @Override
    protected float footCenterX(HuskHornhead h) {
        return h.position.x + h.width / 2f;
    }

    @Override
    protected float footY(HuskHornhead h) {
        return h.position.y - 1f;
    }

    @Override
    protected boolean facingRight(HuskHornhead h) {
        return h.facing == EnemyModel.Facing.RIGHT;
    }

    @Override
    protected boolean keepsTimeAcrossTransition(HuskState previous, HuskState next) {
        return previous == HuskState.DYING && next == HuskState.DEAD;
    }
}

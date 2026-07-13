package hk.view.render;

import hk.engine.EntityView;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.List;

import hk.model.boss.FalseKnight;
import hk.model.boss.FalseKnight.State;
import hk.model.combat.ArenaAttack;
import hk.model.enemy.EnemyModel.Facing;
import hk.view.anim.FalseKnightAnimationSet;


public class FalseKnightRenderer extends EntityView<FalseKnight, State> {

    private static final float SPRITE_HEIGHT  = 80f;
    private static final float DRAW_Y_OFFSET   = -5f;
    private static final float PHASE2_ANIM     = 1.4f;
    private static final float SHOCKWAVE_RENDER_SIZE = 20f;

    private final FalseKnightAnimationSet animations;
    private final TextureRegion           shockwaveRegion;

    public FalseKnightRenderer(SpriteBatch batch, FalseKnightAnimationSet animations) {
        super(batch);
        this.animations      = animations;
        this.shockwaveRegion = animations.getShockwaveRegion();
    }

    @Override
    protected State stateOf(FalseKnight boss) {
        return boss.currentState;
    }

    @Override
    protected Animation<TextureRegion> animationFor(State state) {
        return animations.get(state);
    }

    @Override
    protected float spriteHeight() {
        return SPRITE_HEIGHT;
    }

    @Override
    protected float footCenterX(FalseKnight boss) {
        return boss.position.x + boss.width / 2f;
    }

    @Override
    protected float footY(FalseKnight boss) {
        return boss.position.y + DRAW_Y_OFFSET;
    }

    @Override
    protected boolean facingRight(FalseKnight boss) {
        return boss.facing == Facing.RIGHT;
    }

    @Override
    protected float scaleDelta(FalseKnight boss, float delta) {
        return delta * (boss.isPhaseTwo() ? PHASE2_ANIM : 1f);
    }

    public void drawShockwaves(List<ArenaAttack> attacks) {
        if (shockwaveRegion == null) return;
        for (ArenaAttack a : attacks) {
            if (a.velocityX == 0f) continue;
            float s      = SHOCKWAVE_RENDER_SIZE;
            float cx     = a.bounds.x + a.bounds.width / 2f;
            float x      = cx - s / 2f;
            float y      = a.bounds.y;
            boolean flip = a.velocityX < 0f;
            batch.draw(shockwaveRegion, flip ? x + s : x, y, flip ? -s : s, s);
        }
    }
}

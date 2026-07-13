package hk.view.render;

import hk.engine.EntityView;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hk.model.combat.Laser;
import hk.model.enemy.EnemyModel;
import hk.model.enemy.EnemyModel.Facing;
import hk.model.enemy.concrete.CrystalGuardian;
import hk.model.enemy.concrete.CrystalGuardian.State;
import hk.view.anim.CrystalGuardianAnimationSet;

public class CrystalGuardianRenderer extends EntityView<CrystalGuardian, State> implements EnemyRenderer {

    private static final float SPRITE_HEIGHT = 22f;
    private static final float BEAM_H        = 10f;
    private static final float ORB_SIZE      = 12f;

    private final CrystalGuardianAnimationSet animations;

    public CrystalGuardianRenderer(SpriteBatch batch, CrystalGuardianAnimationSet animations) {
        super(batch);
        this.animations = animations;
    }

    @Override
    public void draw(EnemyModel e, float delta) {
        if (e instanceof CrystalGuardian cg) draw(cg, delta);
    }

    @Override
    protected State stateOf(CrystalGuardian cg) {
        return (cg.state == State.ENRAGED && cg.velocity.x == 0f) ? State.IDLE : cg.state;
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
    protected float footCenterX(CrystalGuardian cg) {
        return cg.position.x + cg.width / 2f;
    }

    @Override
    protected float footY(CrystalGuardian cg) {
        return cg.position.y - 1f;
    }

    @Override
    protected boolean facingRight(CrystalGuardian cg) {
        return cg.facing == Facing.RIGHT;
    }

    @Override
    protected boolean keepsTimeAcrossTransition(State previous, State next) {
        return previous == State.DYING && next == State.DEAD;
    }

    @Override
    protected void postProcess(CrystalGuardian cg, State state, float time) {
        if (cg.state != State.LASER) return;

        boolean right      = cg.facing == Facing.RIGHT;
        float emitterX     = right ? cg.position.x + cg.width : cg.position.x;
        float beamCenterY  = cg.position.y + cg.height / 2f;

        drawOrb(emitterX, beamCenterY, time);

        if (cg.laser.active) {
            drawBeam(emitterX, beamCenterY, time, right);
        }
    }

    private void drawOrb(float emitterX, float beamCenterY, float time) {
        TextureRegion orb = animations.getLaserCircle().getKeyFrame(time, true);
        float half = ORB_SIZE / 2f;
        batch.draw(orb, emitterX - half - 2f,
            beamCenterY - half, ORB_SIZE, ORB_SIZE);
    }

    private void drawBeam(float emitterX, float beamCenterY, float time, boolean right) {
        TextureRegion frame = animations.getLaserBeam().getKeyFrame(time, true);

        float tileH = BEAM_H;
        float tileW = tileH * (frame.getRegionWidth() / (float) frame.getRegionHeight());
        float beamY = beamCenterY - tileH / 2f;

        float covered = 0f;
        while (covered < Laser.RANGE) {
            float tileX = right ? emitterX + covered : emitterX - covered - tileW;
            batch.draw(frame, right ? tileX : tileX + tileW, beamY, right ? tileW : -tileW, tileH);
            covered += tileW - 1f;
        }
    }
}

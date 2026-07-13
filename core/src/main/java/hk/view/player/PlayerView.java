package hk.view.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

import hk.model.player.PlayerModel;
import hk.view.anim.KnightAnimationSet;
import hk.view.anim.KnightEffectSet;
import hk.view.anim.KnightEffectSet.Effect;
import hk.view.anim.KnightEffectSet.EffectDef;


public class PlayerView implements Disposable {

    private static final float SPRITE_HEIGHT_TILES = 25f;


    private static final Color SHADE_TINT = new Color(0.12f, 0.06f, 0.18f, 1f);

    private static final int   SPARK_POOL_SIZE  = 24;
    private static final int   SPARKS_PER_SWING = 7;

    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final KnightAnimationSet animations;
    private final KnightEffectSet effects;
    private final Texture sparkTexture;
    private final Spark[] sparks = new Spark[SPARK_POOL_SIZE];


    private Animation<TextureRegion> previousAnim = null;
    private float stateTime = 0f;


    private EffectDef activeEffect = null;
    private float effectTime = 0f;
    private float effectX    = 0f;
    private float effectY    = 0f;

    private PlayerModel.State previousState = null;

    private static final class Spark {
        float x, y, vx, vy, size, life, maxLife;
    }

    public PlayerView(SpriteBatch batch, OrthographicCamera camera,
                          KnightAnimationSet animations, KnightEffectSet effects) {
        this.batch      = batch;
        this.camera     = camera;
        this.animations = animations;
        this.effects    = effects;
        this.sparkTexture = buildSparkTexture();
        for (int i = 0; i < sparks.length; i++) sparks[i] = new Spark();
    }

    private static Texture buildSparkTexture() {
        int size = 16;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float c = (size - 1) / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - c) / c, dy = (y - c) / c;
                float d  = (float) Math.sqrt(dx * dx + dy * dy);
                float a  = MathUtils.clamp(1f - d, 0f, 1f);
                pm.setColor(1f, 1f, 1f, a * a);
                pm.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(pm);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return tex;
    }





    public void draw(PlayerModel knight, float delta) {
        checkEffectTrigger(knight);
        tickEffect(delta);

        boolean shade = knight.isShadowDashing();
        Animation<TextureRegion> anim = shade ? animations.getShadowDash() : animations.get(knight.state);
        if (anim == null) return;

        advanceAnimClock(anim, delta);

        boolean flip = knight.facing == PlayerModel.Facing.RIGHT;
        boolean loop = anim.getPlayMode() != Animation.PlayMode.NORMAL;
        TextureRegion frame = anim.getKeyFrame(stateTime, loop);

        float fh = SPRITE_HEIGHT_TILES;
        float fw = fh * (frame.getRegionWidth() / (float) frame.getRegionHeight());
        float cx = knight.position.x + PlayerModel.WIDTH  / 2f;
        float cy = knight.position.y + PlayerModel.HEIGHT / 2f;
        float x  = cx - fw / 2f;
        float y  = cy - fh / 2f + 3.5f;

        boolean effectInFront = activeEffect != null && activeEffect.foreground;
        if (!effectInFront) drawEffect(knight, flip);


        boolean visible = shade || !knight.isInvincible() || anim.getKeyFrameIndex(stateTime) % 3 != 0;
        if (visible) drawSprite(frame, x, y, fw, fh, flip);
        if (effectInFront) drawEffect(knight, flip);
        updateAndDrawSparks(delta);
    }






    private void startEffect(Effect effect, PlayerModel knight) {
        activeEffect = effects.get(effect);
        effectTime   = 0f;
        boolean flip = knight.facing == PlayerModel.Facing.RIGHT;
        effectX      = resolveEffectX(knight, flip);
        effectY      = knight.position.y + activeEffect.y;
    }


    private float resolveEffectX(PlayerModel knight, boolean flip) {
        return flip
            ? knight.position.x + PlayerModel.WIDTH - activeEffect.x - activeEffect.w
            : knight.position.x + activeEffect.x;
    }


    private void checkEffectTrigger(PlayerModel knight) {

        if (knight.healBurst) {
            knight.healBurst = false;
            startEffect(Effect.FOCUS_GET, knight);
        }

        if (knight.state == previousState) return;
        previousState = knight.state;

        if (knight.state == PlayerModel.State.DASH)        startEffect(Effect.DASH,       knight);
        else if (knight.state == PlayerModel.State.ATTACK)      { startEffect(Effect.SLASH,      knight); spawnSparks(knight.swordBounds()); }
        else if (knight.state == PlayerModel.State.DOWN_ATTACK) { startEffect(Effect.DOWN_SLASH, knight); spawnSparks(knight.downSwordBounds()); }
        else if (knight.state == PlayerModel.State.UP_ATTACK)   { startEffect(Effect.UP_SLASH,   knight); spawnSparks(knight.upSwordBounds()); }
        else if (knight.state == PlayerModel.State.CAST)        startEffect(Effect.CAST_BLAST, knight);
        else if (knight.state == PlayerModel.State.SCREAM)

            startEffect(knight.hasCharm(hk.model.charm.CharmType.VOID_HEART)
                    ? Effect.SCREAM_VOID : Effect.SCREAM, knight);
    }






    private void spawnSparks(Rectangle hitbox) {
        if (hitbox == null) return;
        float cx = hitbox.x + hitbox.width  / 2f;
        float cy = hitbox.y + hitbox.height / 2f;
        int spawned = 0;
        for (Spark s : sparks) {
            if (s.life > 0f) continue;
            s.x = cx + MathUtils.random(-hitbox.width / 2f, hitbox.width / 2f);
            s.y = cy + MathUtils.random(-hitbox.height / 2f, hitbox.height / 2f);
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(18f, 40f);
            s.vx = MathUtils.cos(angle) * speed;
            s.vy = MathUtils.sin(angle) * speed;
            s.size    = MathUtils.random(0.6f, 1.4f);
            s.maxLife = MathUtils.random(0.12f, 0.28f);
            s.life    = s.maxLife;
            if (++spawned >= SPARKS_PER_SWING) return;
        }
    }

    private void updateAndDrawSparks(float delta) {
        boolean any = false;
        for (Spark s : sparks) if (s.life > 0f) { any = true; break; }
        if (!any) return;

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (Spark s : sparks) {
            if (s.life <= 0f) continue;
            s.life -= delta;
            s.x += s.vx * delta;
            s.y += s.vy * delta;
            s.vx *= 0.90f;
            s.vy *= 0.90f;
            if (s.life <= 0f) continue;
            float a = s.life / s.maxLife;
            batch.setColor(1f, 0.95f, 0.8f, a);
            batch.draw(sparkTexture, s.x - s.size / 2f, s.y - s.size / 2f, s.size, s.size);
        }
        batch.setColor(1f, 1f, 1f, 1f);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }


    private void tickEffect(float delta) {
        if (activeEffect == null) return;
        effectTime += delta;
        if (activeEffect.animation.isAnimationFinished(effectTime)) activeEffect = null;
    }


    private void drawEffect(PlayerModel knight, boolean flip) {
        if (activeEffect == null) return;

        float ex = activeEffect.isLocked
                ? resolveEffectX(knight, flip)
                : effectX;
        float ey = activeEffect.isLocked
                ? knight.position.y + activeEffect.y
                : effectY;

        TextureRegion efx = activeEffect.animation.getKeyFrame(effectTime, false);
        float ew = activeEffect.w;
        float eh = activeEffect.h;


        boolean texFlip = activeEffect.facesRight != flip;
        boolean shade   = knight.isShadowDashing();
        if (shade) batch.setColor(SHADE_TINT);
        float drawY = activeEffect.flipVertical ? ey + eh : ey;
        float drawH = activeEffect.flipVertical ? -eh : eh;
        batch.draw(efx, texFlip ? ex + ew : ex, drawY, texFlip ? -ew : ew, drawH);
        if (shade) batch.setColor(1f, 1f, 1f, 1f);
    }






    private void advanceAnimClock(Animation<TextureRegion> anim, float delta) {
        if (anim != previousAnim) {
            stateTime    = 0f;
            previousAnim = anim;
        } else {
            stateTime += delta;
        }
    }


    private void drawSprite(TextureRegion frame, float x, float y, float w, float h, boolean flip) {
        batch.draw(frame, flip ? x + w : x, y, flip ? -w : w, h);
    }

    @Override
    public void dispose() {
        sparkTexture.dispose();
    }
}

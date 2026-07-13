package hk.view.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import java.util.IdentityHashMap;
import java.util.Map;

import hk.model.enemy.EnemyModel;
import hk.model.enemy.concrete.Mosscreep;
import hk.model.enemy.concrete.Mosscreep.State;
import hk.view.anim.MosscreepAnimationSet;


public class MosscreepRenderer implements EnemyRenderer {

    private static final float SPRITE_HEIGHT   = 16f;
    private static final float SHAKE_AMPLITUDE = 1.3f;
    private static final float SHAKE_SPEED     = 55f;

    private final SpriteBatch            batch;
    private final OrthographicCamera     camera;
    private final MosscreepAnimationSet  animations;

    private final Map<Mosscreep, Float> stateTimes     = new IdentityHashMap<>();
    private final Map<Mosscreep, State> previousStates = new IdentityHashMap<>();

    public MosscreepRenderer(SpriteBatch batch, OrthographicCamera camera,
                             MosscreepAnimationSet animations) {
        this.batch      = batch;
        this.camera     = camera;
        this.animations = animations;
    }

    @Override
    public void draw(EnemyModel e, float delta) {
        if (e instanceof Mosscreep m) drawMosscreep(m, delta);
    }

    private void drawMosscreep(Mosscreep m, float delta) {
        Animation<TextureRegion> anim = animations.get(m.state);
        if (anim == null) return;

        State prev = previousStates.get(m);
        float time = stateTimes.getOrDefault(m, 0f);
        if (m.state != prev) {
            time = 0f;
            previousStates.put(m, m.state);
        } else {
            time += delta;
        }
        stateTimes.put(m, time);

        boolean loop  = anim.getPlayMode() != Animation.PlayMode.NORMAL;
        TextureRegion frame = anim.getKeyFrame(time, loop);

        float fh = SPRITE_HEIGHT;
        float fw = fh * (frame.getRegionWidth() / (float) frame.getRegionHeight());
        float cx = m.position.x + m.width / 2f;
        float x  = cx - fw / 2f;
        float y  = m.position.y - 1f;

        if (m.state == State.STARTLE) {
            x += MathUtils.sin(time * SHAKE_SPEED) * SHAKE_AMPLITUDE;
        }


        boolean flip = m.facing == EnemyModel.Facing.RIGHT;
        batch.draw(frame, flip ? x + fw : x, y, flip ? -fw : fw, fh);
    }
}

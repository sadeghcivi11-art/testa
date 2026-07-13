package hk.engine;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class EntityView<E, S> {

    protected final SpriteBatch batch;
    private final StateTimeTracker<E, S> tracker = new StateTimeTracker<>();

    protected EntityView(SpriteBatch batch) {
        this.batch = batch;
    }

    protected abstract S stateOf(E e);
    protected abstract Animation<TextureRegion> animationFor(S state);
    protected abstract float spriteHeight();
    protected abstract float footCenterX(E e);
    protected abstract float footY(E e);
    protected abstract boolean facingRight(E e);

    protected boolean keepsTimeAcrossTransition(S previous, S next) {
        return false;
    }

    protected float scaleDelta(E e, float delta) {
        return delta;
    }

    protected void postProcess(E e, S state, float time) { }

    public final void draw(E e, float delta) {
        if (e == null) return;

        S state = stateOf(e);
        Animation<TextureRegion> anim = animationFor(state);
        if (anim == null) return;

        S prev = tracker.previousState(e);
        boolean keepTime = prev != null && keepsTimeAcrossTransition(prev, state);
        float time = tracker.advance(e, state, scaleDelta(e, delta), keepTime);

        boolean loop = anim.getPlayMode() != Animation.PlayMode.NORMAL;
        TextureRegion frame = anim.getKeyFrame(time, loop);

        SpriteQuad quad = SpriteQuad.centered(frame, spriteHeight(), footCenterX(e), footY(e), facingRight(e));
        quad.draw(batch, frame);

        postProcess(e, state, time);
    }
}

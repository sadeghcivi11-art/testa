package hk.view.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import hk.engine.EntityView;
import hk.model.enemy.CrawlidState;
import hk.model.enemy.CrawlidState.State;
import hk.model.enemy.EnemyModel;
import hk.view.anim.CrawlidAnimationSet;
import hk.view.render.EnemyRenderer;


public class CrawlidView extends EntityView<CrawlidState, State> implements EnemyRenderer {

    private static final float SPRITE_HEIGHT = 24f;

    private final CrawlidAnimationSet animations;

    public CrawlidView(SpriteBatch batch, CrawlidAnimationSet animations) {
        super(batch);
        this.animations = animations;
    }

    @Override
    public void draw(EnemyModel e, float delta) {
        if (e instanceof CrawlidState c) draw(c, delta);
    }

    @Override
    protected State stateOf(CrawlidState c) {
        return c.state;
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
    protected float footCenterX(CrawlidState c) {
        return c.position.x + c.width / 2f;
    }

    @Override
    protected float footY(CrawlidState c) {
        return c.position.y - 1f;
    }

    @Override
    protected boolean facingRight(CrawlidState c) {
        return c.facing == EnemyModel.Facing.RIGHT;
    }
}

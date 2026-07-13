package hk.model.combat;

import com.badlogic.gdx.math.Rectangle;


public class ArenaAttack {

    private static final float NO_VELOCITY     = 0f;
    private static final float NO_ACCELERATION = 0f;

    public final Rectangle bounds;
    public final int        damage;
    public float            duration;
    public float            velocityX;
    public float            accelerationX;
    public boolean          alive = true;

    public ArenaAttack(float x, float y, float width, float height, int damage, float duration) {
        this(x, y, width, height, damage, duration, NO_VELOCITY, NO_ACCELERATION);
    }

    public ArenaAttack(float x, float y, float width, float height, int damage, float duration, float velocityX) {
        this(x, y, width, height, damage, duration, velocityX, NO_ACCELERATION);
    }

    public ArenaAttack(float x, float y, float width, float height, int damage, float duration, float velocityX, float accelerationX) {
        this.bounds        = new Rectangle(x, y, width, height);
        this.damage        = damage;
        this.duration      = duration;
        this.velocityX     = velocityX;
        this.accelerationX = accelerationX;
    }
}

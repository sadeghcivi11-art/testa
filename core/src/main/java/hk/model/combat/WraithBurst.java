package hk.model.combat;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;


public class WraithBurst {

    public static final float WIDTH         = 20f;
    public static final float HEIGHT        = 28f;
    public static final int   TICK_COUNT    = 3;
    public static final float TICK_INTERVAL = 0.15f;

    public final Vector2 position = new Vector2();
    public final int     damage;
    public int   ticksRemaining   = TICK_COUNT;
    public float tickTimer        = 0f;
    public boolean alive          = true;

    public WraithBurst(float x, float y, int damage) {
        position.set(x, y);
        this.damage = damage;
    }

    public Rectangle bounds() {
        return new Rectangle(position.x, position.y, WIDTH, HEIGHT);
    }
}

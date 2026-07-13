package hk.model.combat;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;


public class Projectile {

    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    public final float width;
    public final float height;
    public int     damage;
    public boolean piercing;
    public boolean voidArt;
    public boolean alive = true;
    public float   lifeTimer;

    public Projectile(float x, float y, float vx, float vy,
                      float width, float height,
                      int damage, boolean piercing, float life) {
        position.set(x, y);
        velocity.set(vx, vy);
        this.width    = width;
        this.height   = height;
        this.damage   = damage;
        this.piercing = piercing;
        this.lifeTimer = life;
    }

    public Rectangle bounds() {
        return new Rectangle(position.x, position.y, width, height);
    }
}

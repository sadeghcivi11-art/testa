package hk.physics;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import hk.model.physics.HasPhysic;

public class Body implements HasPhysic {

    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();

    public float width;
    public float height;
    public float gravityScale      = 1f;
    public float maxFallSpeed      = -115f;
    public boolean affectedByGravity = true;

    private boolean onGround = false;
    private final Rectangle boundsRect = new Rectangle();

    public Body(float x, float y, float width, float height) {
        position.set(x, y);
        this.width  = width;
        this.height = height;
    }

    public AABB bounds() {
        return new AABB(position.x, position.y, width, height);
    }

    @Override public Vector2   getPosition()          { return position; }
    @Override public Vector2   getVelocity()          { return velocity; }
    @Override public Rectangle getBounds()            { return boundsRect.set(position.x, position.y, width, height); }
    @Override public boolean   isOnGround()           { return onGround; }
    @Override public void      setOnGround(boolean v) { onGround = v; }
    @Override public boolean   isAffectedByGravity()  { return affectedByGravity; }
    @Override public float     gravityScale()         { return gravityScale; }
    @Override public float     maxFallSpeed()         { return maxFallSpeed; }
}

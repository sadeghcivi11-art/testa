package hk.model.enemy;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import hk.engine.Entity;
import hk.model.player.PlayerModel;
import hk.model.physics.HasPhysic;


public abstract class EnemyModel implements HasPhysic, Entity {

    public int health;
    public final int maxHealth;
    public final Vector2 position      = new Vector2();
    public final Vector2 velocity      = new Vector2();
    public final Vector2 spawnPosition = new Vector2();
    private boolean onGround = false;
    public float  reviveRange;
    public float  knockbackTimer = 0f;
    public float  knockbackVx    = 0f;


    public float width;
    public float height;


    public float weight;

    public enum Facing { LEFT, RIGHT }
    public Facing facing = Facing.RIGHT;


    public float visionRange = 0f;
    public boolean spottedPlayer = false;
    public final Vector2 lastKnownPlayerPos = new Vector2();

    protected EnemyModel(int health, float x, float y, EnemyStats stats) {
        this.health    = health;
        this.maxHealth = health;
        position.set(x, y);
        spawnPosition.set(x, y);
        this.width       = stats.width();
        this.height      = stats.height();
        this.weight      = stats.weight();
        this.reviveRange = stats.reviveRange();
    }



    private final Rectangle boundsRect = new Rectangle();


    @Override public Vector2   getPosition()              { return position; }
    @Override public Vector2   getVelocity()              { return velocity; }
    @Override public Rectangle getBounds()                { return boundsRect.set(position.x, position.y, width, height); }
    @Override public boolean   isOnGround()               { return onGround; }
    @Override public void      setOnGround(boolean v)     { onGround = v; }

    @Override
    public boolean isAlive() { return health > 0; }
    public boolean isDead()  { return health <= 0; }

    public void takeDamage(int amount) {
        if (amount > 0) health = Math.max(0, health - amount);
    }

    public boolean hasLineOfSight(PlayerModel player) {
        return false;
    }
}

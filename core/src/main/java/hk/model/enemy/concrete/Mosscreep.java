package hk.model.enemy.concrete;

import com.badlogic.gdx.math.Vector2;

import hk.model.enemy.EnemyStats;
import hk.model.enemy.FlyingEnemy;


public class Mosscreep extends FlyingEnemy {

    public enum State { FLY, STARTLE, DIVE, RECOVER, DYING, DEAD }

    public static final int   HP           = 3;
    public static final float SPEED        = 42f;
    public static final float DIVE_SPEED   = 175f;
    public static final float VISION       = 110f;
    public static final float REVIVE_RANGE = 700f;

    public static final float STARTLE_DURATION = 3 * 0.14f;
    public static final float DIVE_MAX_TIME    = 1.1f;
    public static final float AGGRO_COOLDOWN   = 1.2f;
    public static final float DYING_DURATION   = 4 * 0.10f;
    public static final float PATROL_RANGE     = 38f;

    public State state = State.FLY;

    public final Vector2 diveVelocity = new Vector2();
    public float diveTimer     = 0f;
    public float dyingTimer    = 0f;
    public float aggroCooldown = 0f;
    public float bobPhase      = 0f;

    public Mosscreep(float x, float y) {
        super(HP, x, y, SPEED, VISION, new EnemyStats(13f, 10f, 0.8f, REVIVE_RANGE));
    }


    @Override
    public boolean isAffectedByGravity() {
        return state == State.DYING || state == State.DEAD;
    }
}
